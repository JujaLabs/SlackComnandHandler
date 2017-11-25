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
    private final String escapedUserInSlackCommand = "<@\\w+\\|([a-zA-z0-9._-]){1,21}>";
    private UserBySlackName userBySlackName;

    @Inject
    public SlackCommandHandlerService(UserBySlackName userBySlackName) {
        this.userBySlackName = userBySlackName;
    }

    public SlackParsedCommand createSlackParsedCommand(String fromUserSlackId, String text) {
        SlackCommand slackCommand = new SlackCommand(fromUserSlackId, text);
        return new SlackCommandToSlackParsedCommandConverter().convert(slackCommand);
    }

    @Getter
    private class SlackCommand {
        private String fromUserSlackId;
        private String text;
        private List<String> slackUserIdInText;
        private Set<String> allSlackUserId;
        private boolean hasFromUserSlackNameInText;

        public SlackCommand(String fromUserSlackId, String text) {
            this.fromUserSlackId = fromUserSlackId;
            this.text = text;
            slackUserIdInText = receiveSlackUserIdFromText(text);
            hasFromUserSlackNameInText = slackUserIdInText.contains(fromUserSlackId);
            allSlackUserId = new HashSet<>(slackUserIdInText);
            allSlackUserId.add(fromUserSlackId);
        }

        private List<String> receiveSlackUserIdFromText(String text) {
            List<String> result = new ArrayList<>();
            Pattern pattern = Pattern.compile(escapedUserInSlackCommand);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String escapedUser = matcher.group();
                result.add(escapedUser.substring(escapedUser.indexOf('@') + 1, escapedUser.indexOf('|')));
            }
            log.debug("Received slack user id: {} from text:", result.toString(), text);
            return result;
        }
    }

    private class SlackCommandToSlackParsedCommandConverter {

        private SlackParsedCommand convert(SlackCommand slackCommand) {
            UserData fromUser;
            List<UserData> usersInText;

            if (slackCommand.isHasFromUserSlackNameInText()) {
                usersInText = receiveUsersBySlackUserId(slackCommand.getAllSlackUserId());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackUserIdInText());
                fromUser = getFromUser(usersInText, slackCommand.getFromUserSlackId());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            } else {
                List<UserData> allUsers = receiveUsersBySlackUserId(slackCommand.getAllSlackUserId());
                fromUser = getFromUser(allUsers, slackCommand.getFromUserSlackId());
                usersInText = deleteFromUser(allUsers, slackCommand.getFromUserSlackId());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackUserIdInText());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            }
        }

        private List<UserData> receiveUsersBySlackUserId(Set<String> allSlackUserId) {

            log.debug("send slack names: {} to user service", allSlackUserId);
            return userBySlackName.findUsersBySlackUserId(new ArrayList<>(allSlackUserId));
        }

        private UserData getFromUser(List<UserData> usersInText, String fromUserSlackUserId) {
            return usersInText.stream()
                    .filter(user -> user.getSlackUserId().equals(fromUserSlackUserId))
                    .findFirst()
                    .get();
        }

        private List<UserData> deleteFromUser(List<UserData> allUsersList, String fromUserSlackUserId) {
            return allUsersList.stream()
                    .filter(user -> user.getSlackUserId() != fromUserSlackUserId)
                    .collect(Collectors.toList());
        }

        private void sortUsersByOrderInText(List<UserData> usersInText, List<String> slackUserIdInText) {
            usersInText.sort(Comparator.comparingInt(user -> slackUserIdInText.indexOf(user.getSlackUserId())));
        }
    }
}
