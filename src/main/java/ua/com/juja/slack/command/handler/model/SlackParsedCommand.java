package ua.com.juja.slack.command.handler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.juja.slack.command.handler.exception.ParseSlackCommandException;


import java.util.List;

/**
 * @author Konstantin Sergey
 */
@ToString(exclude = {"slackNamePattern", "logger"})
@EqualsAndHashCode
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String slackNamePattern = "@([a-zA-z0-9\\.\\_\\-]){1,21}";
    private UserData fromUserData;
    private String text;
    private List<UserData> usersInText;

    public SlackParsedCommand(UserData fromUserData, String text, List<UserData> usersInText) {
        this.fromUserData = fromUserData;
        this.text = text;
        this.usersInText = usersInText;

        logger.debug("SlackParsedCommand created with parameters: " +
                        "fromSlackName: {} text: {} userCountInText {} users: {}",
                fromUserData, text, usersInText.size(), usersInText.toString());
    }

    public List<UserData> getAllUsersFromText() {
        return usersInText;
    }

    public UserData getFirstUserFromText() {
        if (usersInText.size() == 0) {
            logger.warn("The text: '{}' doesn't contain any slack names", text);
            throw new ParseSlackCommandException(String.format("The text '%s' doesn't contain any slack names", text));
        } else {
            return usersInText.get(0);
        }
    }

    public String getTextWithoutSlackNames() {
        String result = text.replaceAll(slackNamePattern, "");
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    public UserData getFromUser() {
        return fromUserData;
    }

    public String getText() {
        return text;
    }

    public int getUserCountInText() {
        return usersInText.size();
    }

    //https://github.com/ksergey12/keepers-slack-bot/blob/implement-tokens/src/main/java/ua/com/juja/microservices/keepers/slackbot/model/SlackParsedCommand.java
}