package ua.com.juja.slack.command.handler.service;

import lombok.Getter;
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
public class SlackCommandHandlerService {

    private final String slackNamePattern = "@([a-zA-z0-9\\.\\_\\-]){1,21}";
    private UserBySlackName userBySlackName;

    @Inject
    public SlackCommandHandlerService(UserBySlackName userBySlackName) {
        this.userBySlackName = userBySlackName;
    }

    public SlackParsedCommand createSlackParsedCommand(String fromUserSlackName, String text) {
        if (!fromUserSlackName.startsWith("@")) {
            fromUserSlackName = "@" + fromUserSlackName;
            log.debug("add '@' to slack name [{}]", fromUserSlackName);
        }
        SlackCommand slackCommand = new SlackCommand(fromUserSlackName, text);
        return new SlackCommandToSlackParsedCommandConverter().convert(slackCommand);
    }

    @Getter
    private class SlackCommand {
        private String fromUserSlackName;
        private String text;
        private List<String> slackNamesInText;
        private Set<String> allSlackNames;
        private boolean hasFromUserSlackNameInText;

        public SlackCommand(String fromUserSlackName, String text) {
            this.fromUserSlackName = fromUserSlackName;
            this.text = text;
            slackNamesInText = receiveSlackNamesFromText(text);
            hasFromUserSlackNameInText = slackNamesInText.contains(fromUserSlackName);
            allSlackNames = new HashSet<>(slackNamesInText);
            allSlackNames.add(fromUserSlackName);
        }

        private List<String> receiveSlackNamesFromText(String text) {
            List<String> result = new ArrayList<>();
            Pattern pattern = Pattern.compile(slackNamePattern);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                result.add(matcher.group());
            }
            log.debug("Received slack names: {} from text:", result.toString(), text);
            return result;
        }
    }

    private class SlackCommandToSlackParsedCommandConverter {

        private SlackParsedCommand convert(SlackCommand slackCommand) {
            UserData fromUser;
            List<UserData> usersInText;

            if (slackCommand.isHasFromUserSlackNameInText()) {
                usersInText = receiveUsersBySlackNames(slackCommand.getAllSlackNames());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackNamesInText());
                fromUser = getFromUser(usersInText, slackCommand.getFromUserSlackName());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            } else {
                List<UserData> allUsers = receiveUsersBySlackNames(slackCommand.getAllSlackNames());
                fromUser = getFromUser(allUsers, slackCommand.getFromUserSlackName());
                usersInText = deleteFromUser(allUsers, slackCommand.getFromUserSlackName());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackNamesInText());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            }
        }

        private List<UserData> receiveUsersBySlackNames(Set<String> allSlackNames) {

            log.debug("send slack names: {} to user service", allSlackNames);
            return userBySlackName.findUsersBySlackNames(new ArrayList<>(allSlackNames));
        }

        private UserData getFromUser(List<UserData> usersInText, String fromUserSlackName) {
            return usersInText.stream()
                    .filter(user -> user.getSlack().equals(fromUserSlackName))
                    .findFirst()
                    .get();
        }

        private List<UserData> deleteFromUser(List<UserData> allUsersList, String fromUserSlackName) {
            return allUsersList.stream()
                    .filter(user -> user.getSlack() != fromUserSlackName)
                    .collect(Collectors.toList());
        }

        private void sortUsersByOrderInText(List<UserData> usersInText, List<String> slackNameInText) {
            usersInText.sort(Comparator.comparingInt(user -> slackNameInText.indexOf(user.getSlack())));
        }
    }
}
