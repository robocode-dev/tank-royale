package dev.robocode.tankroyale.botapi.events;

import java.net.URI;

/** Event occurring when bot gets disconnected from server. */
@SuppressWarnings("unused")
public final class DisconnectedEvent extends ConnectionEvent {

  /** Indication if closing of the connection was initiated by the remote host. */
  private final boolean remote;

  /**
   * Initializes a new instance of the DisconnectedEvent class.
   *
   * @param serverUri is the URI of the server.
   * @param remote is a flag indicating if closing of the connection was initiated by the remote
   *     host.
   */
  public DisconnectedEvent(URI serverUri, boolean remote) {
    super(serverUri);
    this.remote = remote;
  }

  /**
   * Checks if closing the connection was initiated by the remote host.
   *
   * @return {@code true} if closing the connection was initiated by the remote host; {@code false} otherwise.
   */
  public boolean isRemote() {
    return remote;
  }
}
