package ua.com.juja.slack.command.hanler.service;



import ua.com.juja.slack.command.hanler.model.UserDTO;

import java.util.List;

/**
 * @author Nikolay Horushko
 */
public interface UserBySlackName {
    List<UserDTO> findUsersBySlackNames(List<String> slackNames);
}
