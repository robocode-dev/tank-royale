package dev.robocode.tankroyale.botapi.events;

import java.net.URI;

/** Event occurring when a connection error occurs. */
@SuppressWarnings("unused")
public final class ConnectionErrorEvent extends ConnectionEvent {

  /** The exception causing the error. */
  private final Exception exception;

  /**
   * Initializes a new instance of the ConnectionErrorEvent class.
   *
   * @param serverUri is the URI of the server.
   * @param exception is the exception causing the error.
   */
  public ConnectionErrorEvent(URI serverUri, Exception exception) {
    super(serverUri);
    this.exception = exception;
  }

  /**
   * Returns the exception causing the error.
   *
   * @return The exception causing the error.
   */
  public Exception getException() {
    return exception;
  }
}
