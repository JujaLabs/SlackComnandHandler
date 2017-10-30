package ua.com.juja.slack.command.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.com.juja.slack.command.handler.UserBySlackName;
import ua.com.juja.slack.command.handler.model.SlackParsedCommand;
import ua.com.juja.slack.command.handler.model.UserData;

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
@Slf4j
public class SlackNameHandlerService {

    private UserBySlackName userBySlackName;

    private final String slackNamePattern = "@([a-zA-z0-9\\.\\_\\-]){1,21}";

    @Inject
    public SlackNameHandlerService(UserBySlackName userBySlackName) {
        this.userBySlackName = userBySlackName;
    }

    public  SlackParsedCommand createSlackParsedCommand(String fromUserSlackName, String text) {
        if (!fromUserSlackName.startsWith("@")) {
            fromUserSlackName = "@" + fromUserSlackName;
            log.debug("add '@' to slack name [{}]", fromUserSlackName);
        }
        //todo в нем удаляется фромюзер из списка. И если команда подразумевает что фромюзер может быть в тексте - получается косяк
        Map<String, UserData> usersMap = receiveUsersMap(fromUserSlackName, text);
        UserData fromUserData = usersMap.get(fromUserSlackName);
        usersMap.remove(fromUserSlackName);

        return new SlackParsedCommand(fromUserData, text, new ArrayList<>(usersMap.values()));
    }

    private Map<String, UserData> receiveUsersMap(String fromSlackName, String text) {
        List<String> slackNames = receiveAllSlackNames(text);
        slackNames.add(fromSlackName);
        log.debug("added \"fromSlackName\" slack name to request: [{}]", fromSlackName);
        log.debug("send slack names: {} to user service", slackNames);
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
        log.debug("Recieved slack names: {} from text:", result.toString(), text);
        return result;
    }
}
