package ua.com.juja.slack.command.handler.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Nikolay Horushko
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public final class UserDTO {
    @JsonProperty
    private final String uuid;
    @JsonProperty
    private final String slackId;
}
