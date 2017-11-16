package ua.com.juja.slack.command.handler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.juja.slack.command.handler.UserBySlackName;
import ua.com.juja.slack.command.handler.model.SlackParsedCommand;
import ua.com.juja.slack.command.handler.model.UserData;

import javax.inject.Inject;
import java.util.*;

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

        List<String> slackNamesInText = receiveSlackNamesFromText(text);

        List<String> allSlackNames = new ArrayList<>(slackNamesInText);
        allSlackNames.add(fromUserSlackName);

        List<UserData> allUsers = receiveUsers(allSlackNames);

        UserData fromUser = getFromUser(allUsers, fromUserSlackName);

        List<UserData> usersInText = deleteFromUser(allUsers, fromUser);

        sortUsersByOrderInText(usersInText, slackNamesInText);

        return new SlackParsedCommand(fromUser, text, usersInText);
    }

    private void sortUsersByOrderInText(List<UserData> usersInText, List<String> slackNameInText){
        usersInText.sort(Comparator.comparingInt(user -> slackNameInText.indexOf(user.getSlack())));
    }

    private List<UserData> deleteFromUser(List<UserData> allUsersList, UserData fromUser){
        return allUsersList.stream()
                .filter(user -> user != fromUser)
                .collect(Collectors.toList());
    }

    private UserData getFromUser(List<UserData> users, String fromUserSlackName){
        return users.stream()
                .filter(user -> user.getSlack().equals(fromUserSlackName))
                .findFirst()
                .get();
    }

    private List<UserData> receiveUsers(List<String> allSlackNames) {
        log.debug("send slack names: {} to user service", allSlackNames);
        return userBySlackName.findUsersBySlackNames(allSlackNames);
    }

    private List<String> receiveSlackNamesFromText(String text) {
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
