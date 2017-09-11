package ua.com.juja.slack.command.hanler.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.com.juja.slack.command.hanler.exception.WrongCommandFormatException;
import ua.com.juja.slack.command.hanler.utils.Utils;


import java.util.List;

/**
 * @author Konstantin Sergey
 */
@ToString(exclude = {"slackNamePattern", "logger"})
@EqualsAndHashCode
public class SlackParsedCommand {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String slackNamePattern;
    private UserDTO fromUserDTO;
    private String text;
    private List<UserDTO> usersInText;

    public SlackParsedCommand(UserDTO fromUserDTO, String text, List<UserDTO> usersInText) {
        this.fromUserDTO = fromUserDTO;
        this.text = text;
        this.usersInText = usersInText;
        slackNamePattern = Utils.getProperty(
                "application.properties",
                "slackNamePattern"
        );
        logger.debug("SlackParsedCommand created with parameters: " +
                        "fromSlackName: {} text: {} userCountInText {} users: {}",
                fromUserDTO, text, usersInText.size(), usersInText.toString());
    }

    public List<UserDTO> getAllUsersFromText() {
        return usersInText;
    }

    public UserDTO getFirstUserFromText() {
        if (usersInText.size() == 0) {
            logger.warn("The text: '{}' doesn't contain any slack names", text);
            throw new WrongCommandFormatException(String.format("The text '%s' doesn't contain any slack names", text));
        } else {
            return usersInText.get(0);
        }
    }

    public String getTextWithoutSlackNames() {
        String result = text.replaceAll(slackNamePattern, "");
        result = result.replaceAll("\\s+", " ").trim();
        return result;
    }

    public UserDTO getFromUserDTO() {
        return fromUserDTO;
    }

    public String getText() {
        return text;
    }

    public int getUserCountInText() {
        return usersInText.size();
    }
}