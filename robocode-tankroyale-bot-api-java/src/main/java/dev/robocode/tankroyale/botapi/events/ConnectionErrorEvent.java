package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a connection error occurs */
@SuppressWarnings("unused")
public final class ConnectionErrorEvent {

  /** The exception causing the error */
  private final Exception exception;

  public ConnectionErrorEvent(Exception exception) {
    this.exception = exception;
  }

  /** Returns the exception causing the error */
  public Exception getException() {
    return exception;
  }
}
