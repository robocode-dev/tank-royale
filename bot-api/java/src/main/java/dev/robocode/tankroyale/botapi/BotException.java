package dev.robocode.tankroyale.botapi;

/**
 * Represents errors that occur with bot execution.
 */
public class BotException extends RuntimeException {

    /**
     * Initializes a new instance of the BotException class with a specified error message.
     *
     * @param message is the message that describes the error.
     */
    public BotException(final String message) {
        super(message);
    }

    /**
     * Initializes a new instance of the BotException class with a specified error message and a
     * reference to the inner exception that is the cause of this exception.
     *
     * @param message is the error message that explains the reason for the exception.
     * @param cause   is the exception that is the cause of the current exception.
     */
    public BotException(final String message, final Exception cause) {
        super(message, cause);
    }
}
