package ua.com.juja.slack.command.handler.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.com.juja.slack.command.handler.UserBySlackUserId;
import ua.com.juja.slack.command.handler.model.SlackParsedCommand;
import ua.com.juja.slack.command.handler.model.UserDTO;

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
    private UserBySlackUserId userBySlackUserId;

    @Inject
    public SlackCommandHandlerService(UserBySlackUserId userBySlackUserId) {
        this.userBySlackUserId = userBySlackUserId;
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
            UserDTO fromUser;
            List<UserDTO> usersInText;

            if (slackCommand.isHasFromUserIdInText()) {
                usersInText = receiveUsersBySlackUserId(slackCommand.getAllSlackUserId());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackUserIdInText());
                fromUser = getFromUser(usersInText, slackCommand.getFromUserSlackUserId());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            } else {
                List<UserDTO> allUsers = receiveUsersBySlackUserId(slackCommand.getAllSlackUserId());
                fromUser = getFromUser(allUsers, slackCommand.getFromUserSlackUserId());
                usersInText = deleteFromUser(allUsers, slackCommand.getFromUserSlackUserId());
                sortUsersByOrderInText(usersInText, slackCommand.getSlackUserIdInText());
                return new SlackParsedCommand(fromUser, slackCommand.getText(), usersInText);
            }
        }

        private List<UserDTO> receiveUsersBySlackUserId(Set<String> allSlackUserId) {

            log.debug("send slack names: {} to user service", allSlackUserId);
            List<UserDTO> result = userBySlackUserId.findUsersBySlackUserId(new ArrayList<>(allSlackUserId));
            checkReceivedUsers(allSlackUserId, result);

            return result;
        }

        private void checkReceivedUsers(Set<String> expectedSlackUserId, List<UserDTO> receivedUsers){

            if(expectedSlackUserId.size() != receivedUsers.size()){
                throw new IllegalArgumentException(String.format("Error. Sent [%d] slackUsersId to UserService, " +
                        "but received [%d] users [%s]", expectedSlackUserId.size(), receivedUsers.size(),
                        receivedUsers.toString()));
            }

            Set<String> actualSlackUserId = receivedUsers.stream()
                    .map(userData -> userData.getSlackId())
                    .collect(Collectors.toSet());

            for (String slackUserId : expectedSlackUserId) {
                if(!actualSlackUserId.contains(slackUserId)){
                    throw new IllegalArgumentException(String.format("Error. User for slackUserId: [%s] didn't find " +
                            "in the List of Users: %s", slackUserId, receivedUsers.toString()));
                }
            }
        }

        private UserDTO getFromUser(List<UserDTO> usersInText, String fromUserSlackUserId) {
            return usersInText.stream()
                    .filter(user -> user.getSlackId().equals(fromUserSlackUserId))
                    .findFirst()
                    .get();
        }

        private List<UserDTO> deleteFromUser(List<UserDTO> allUsersList, String fromUserSlackUserId) {
            return allUsersList.stream()
                    .filter(user -> user.getSlackId() != fromUserSlackUserId)
                    .collect(Collectors.toList());
        }

        private void sortUsersByOrderInText(List<UserDTO> usersInText, List<String> slackUserIdInText) {
            usersInText.sort(Comparator.comparingInt(user -> slackUserIdInText.indexOf(user.getSlackId())));
        }
    }
}
