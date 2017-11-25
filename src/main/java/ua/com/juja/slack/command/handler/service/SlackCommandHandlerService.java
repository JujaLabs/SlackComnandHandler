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

    private final String escapedSlackUserIdAndName = "<@\\w+\\|([a-zA-z0-9._-]){1,21}>";
    private UserBySlackName userBySlackName;

    @Inject
    public SlackCommandHandlerService(UserBySlackName userBySlackName) {
        this.userBySlackName = userBySlackName;
    }

    public SlackParsedCommand createSlackParsedCommand(String fromUserSlackUserId, String text) {
        log.debug("Start create slackParsedCommand fromUserSlackUserId: [{}] text: [{}]", fromUserSlackUserId, text);
        SlackCommand slackCommand = new SlackCommand(fromUserSlackUserId, text);
        SlackParsedCommand result = new SlackCommandToSlackParsedCommandConverter().convert(slackCommand);
        log.debug("created SlackParsedCommand [{}]", result.toString());
        return result;

    }

    @Getter
    private class SlackCommand {
        private String fromUserSlackUserId;
        private String text;
        private List<String> slackUserIdInText;
        private Set<String> allSlackUserId;
        private boolean hasFromUserIdInText;

        public SlackCommand(String fromUserSlackUserId, String text) {
            this.fromUserSlackUserId = fromUserSlackUserId;
            this.text = text;
            slackUserIdInText = receiveSlackUserIdFromText(text);
            hasFromUserIdInText = slackUserIdInText.contains(fromUserSlackUserId);
            allSlackUserId = new HashSet<>(slackUserIdInText);
            allSlackUserId.add(fromUserSlackUserId);
        }

        private List<String> receiveSlackUserIdFromText(String text) {
            List<String> result = new ArrayList<>();
            Pattern pattern = Pattern.compile(escapedSlackUserIdAndName);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String slackUserIdAndName = matcher.group();
                result.add(slackUserIdAndName.substring(slackUserIdAndName.indexOf('@') + 1, slackUserIdAndName.indexOf('|')));
            }
            log.debug("Received slack user id: {} from text:", result.toString(), text);
            return result;
        }
    }

    private class SlackCommandToSlackParsedCommandConverter {

        private SlackParsedCommand convert(SlackCommand slackCommand) {
            UserData fromUser;
            List<UserData> usersInText;

            if (slackCommand.isHasFromUserIdInText()) {
                usersInText = receiveUsersBySlackUserId(slackCommand.getAllSlackUserId());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackUserIdInText());
                fromUser = getFromUser(usersInText, slackCommand.getFromUserSlackUserId());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            } else {
                List<UserData> allUsers = receiveUsersBySlackUserId(slackCommand.getAllSlackUserId());
                fromUser = getFromUser(allUsers, slackCommand.getFromUserSlackUserId());
                usersInText = deleteFromUser(allUsers, slackCommand.getFromUserSlackUserId());
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
