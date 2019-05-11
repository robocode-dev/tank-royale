package net.robocode2;

import net.robocode2.events.ConnectedEvent;
import net.robocode2.events.ConnectionErrorEvent;
import net.robocode2.events.DisconnectedEvent;

/** Interface for a bot. */
public interface IBot {

  /** Event handler triggered when connected to server */
  void onConnected(ConnectedEvent connectedEvent);

  /** Event handler triggered when disconnected from server */
  void onDisconnected(DisconnectedEvent disconnectedEvent);

  /** Event handler triggered when a connection error occurs */
  void onConnectionError(ConnectionErrorEvent connectionErrorEvent);
}
