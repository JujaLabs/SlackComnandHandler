package ua.com.juja.slack.command.handler.exception;

/**
 * @author Nikolay Horushko
 */
public class ParseSlackCommandException extends RuntimeException {
    public ParseSlackCommandException(String message) {
        super(message);
    }
}