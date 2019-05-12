package net.robocode2;

import net.robocode2.events.ConnectedEvent;
import net.robocode2.events.ConnectionErrorEvent;
import net.robocode2.events.DisconnectedEvent;

/** Interface for a bot. */
public interface IBot {

  /** Returns the unique id of this bot in the battle. Available when game has started. */
  int getMyId();

  /** Returns the game type, e.g. "melee". Available when game has started. */
  String getGameType();

  /** Returns the width of the arena measured in pixels. Available when game has started. */
  int getArenaWidth();

  /** Returns the height of the arena measured in pixels. Available when game has started. */
  int getArenaHeight();

  /** Returns the number of rounds in a battle. Available when game has started. */
  int getNumberOfRounds();

  /**
   * Returns the gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun
   * is able to fire. Available when game has started.
   */
  double getGunCoolingRate();

  /**
   * Returns the maximum number of inactive turns allowed, where a bot does not take any action
   * before it is zapped by the game. Available when game has started.
   */
  int getInactivityTurns();

  /**
   * Returns turn timeout in milliseconds for sending intent after having received 'tick' message.
   * Available when game has started.
   */
  int getTurnTimeout();

  /**
   * Returns time limit in milliseconds for sending ready message after having received 'new battle'
   * message. Available when game has started.
   */
  int getReadyTimeout();

  /** Event handler triggered when connected to server */
  void onConnected(ConnectedEvent connectedEvent);

  /** Event handler triggered when disconnected from server */
  void onDisconnected(DisconnectedEvent disconnectedEvent);

  /** Event handler triggered when a connection error occurs */
  void onConnectionError(ConnectionErrorEvent connectionErrorEvent);

  /** Event handler triggered when game has started */
  void onGameStarted();
}
