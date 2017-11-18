package ua.com.juja.slack.command.handler;

import ua.com.juja.slack.command.handler.model.UserData;

import java.util.List;

/**
 * @author Nikolay Horushko
 */
public interface UserBySlackName {
    List<UserData> findUsersBySlackNames(List<String> slackNames);
}
