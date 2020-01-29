package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.GameSetup;

/** Event occurring when game has just started */
@SuppressWarnings("unused")
public final class GameStartedEvent implements IMessage {

  /** The ID used for identifying your bot in the current battle */
  private final int myId;

  /** The game setup for the battle just started */
  private final GameSetup gameSetup;

  public GameStartedEvent(int myId, GameSetup gameSetup) {
    this.myId = myId;
    this.gameSetup = gameSetup;
  }

  /** Returns the ID used for identifying your bot in the current battle */
  public int getMyId() {
    return myId;
  }

  /** Returns the game setup for the battle just started */
  private GameSetup getGameSetup() {
    return gameSetup;
  }
}
