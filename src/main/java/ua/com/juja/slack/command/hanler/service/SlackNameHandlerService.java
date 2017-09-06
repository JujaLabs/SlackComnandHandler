package ua.com.juja.slack.command.hanler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.slack.command.hanler.model.SlackParsedCommand;
import ua.com.juja.slack.command.hanler.model.UserData;
import ua.com.juja.slack.command.hanler.utils.Utils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Nikolay Horushko
 * @author Konstantin Sergey
 */
@Service
public class SlackNameHandlerService {

    private  final Logger logger = LoggerFactory.getLogger(this.getClass());

    private UserBySlackName userBySlackName;

    private final String slackNamePattern;

    @Inject
    public SlackNameHandlerService(UserBySlackName userBySlackName) {
        this.userBySlackName = userBySlackName;
        slackNamePattern = Utils.getProperty(
                "application.properties",
                "slackNamePattern"
        );
    }

    public SlackParsedCommand createSlackParsedCommand(String fromUserSlackName, String text) {
        if (!fromUserSlackName.startsWith("@")) {
            fromUserSlackName = "@" + fromUserSlackName;
            logger.debug("add '@' to slack name [{}]", fromUserSlackName);
        }
        Map<String, UserData> usersMap = receiveUsersMap(fromUserSlackName, text);
        UserData fromUserData = usersMap.get(fromUserSlackName);
        usersMap.remove(fromUserSlackName);

        return new SlackParsedCommand(fromUserData, text, new ArrayList<>(usersMap.values()));
    }

    private Map<String, UserData> receiveUsersMap(String fromSlackName, String text) {
        List<String> slackNames = receiveAllSlackNames(text);
        slackNames.add(fromSlackName);
        logger.debug("added \"fromSlackName\" slack name to request: [{}]", fromSlackName);
        logger.debug("send slack names: {} to user service", slackNames);
        List<UserData> users = userBySlackName.findUsersBySlackNames(slackNames);
        return users.stream()
                .collect(Collectors.toMap(user -> user.getSlack(), user -> user, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private List<String> receiveAllSlackNames(String text) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(slackNamePattern);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        logger.debug("Recieved slack names: {} from text:", result.toString(), text);
        return result;
    }
}
