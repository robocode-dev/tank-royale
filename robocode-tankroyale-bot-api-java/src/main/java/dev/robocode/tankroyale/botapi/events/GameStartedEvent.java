package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import dev.robocode.tankroyale.botapi.GameSetup;

/** Event occurring when game has just started */
@Value
@ToString
public class GameStartedEvent implements Message {
  /** The ID used for identifying your bot in the current battle */
  int myId;
  /** The game setup for the battle just started */
  GameSetup gameSetup;

  @Builder
  @SuppressWarnings("UnusedDeclaration")
  private GameStartedEvent(int myId, GameSetup gameSetup) {
    this.myId = myId;
    this.gameSetup = gameSetup;
  }
}
