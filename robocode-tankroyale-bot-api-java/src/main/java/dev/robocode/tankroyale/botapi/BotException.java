package dev.robocode.tankroyale.botapi;

/** Bot exception */
public class BotException extends RuntimeException {

  public BotException(final String message) {
    super(message);
  }

  public BotException(final String message, final Exception cause) {
    super(message, cause);
  }
}
