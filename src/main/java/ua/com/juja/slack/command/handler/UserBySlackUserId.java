package ua.com.juja.slack.command.handler;

import ua.com.juja.slack.command.handler.model.UserDTO;

import java.util.List;

/**
 * @author Nikolay Horushko
 */
public interface UserBySlackUserId {
    List<UserDTO> findUsersBySlackUserId(List<String> slackNames);
}
