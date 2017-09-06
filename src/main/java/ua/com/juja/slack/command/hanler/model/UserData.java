package ua.com.juja.slack.command.hanler.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Nikolay Horushko
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UserData {
    private String uuid;
    private String slack;
}
