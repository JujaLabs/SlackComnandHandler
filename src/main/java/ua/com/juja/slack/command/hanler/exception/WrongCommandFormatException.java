package ua.com.juja.slack.command.hanler.exception;

/**
 * @author Nikolay Horushko
 */
public class WrongCommandFormatException extends RuntimeException {
    public WrongCommandFormatException(String message) {
        super(message);
    }
}