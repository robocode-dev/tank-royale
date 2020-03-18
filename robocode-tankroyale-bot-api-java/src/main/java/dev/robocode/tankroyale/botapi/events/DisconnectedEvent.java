package dev.robocode.tankroyale.botapi.events;

/** Event occurring when bot gets disconnected from server. */
@SuppressWarnings("unused")
public final class DisconnectedEvent {

  /** Indication if closing of the connection was initiated by the remote host. */
  private final boolean remote;

  /**
   * Initializes a new instance of the DisconnectedEvent class.
   *
   * @param remote is a flag indicating if closing of the connection was initiated by the remote
   *     host.
   */
  public DisconnectedEvent(boolean remote) {
    this.remote = remote;
  }

  /**
   * Checks if closing the connection was initiated by the remote host.
   *
   * @return true if closing the connection was initiated by the remote host; false otherwise.
   */
  public boolean isRemote() {
    return remote;
  }
}
