package net.robocode2.events;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import net.robocode2.GameSetup;

/** Event occurring when game has just started */
@Value
@ToString
public class GameStartedEvent implements Message {
  /** The ID used for identifying your bot in the current battle */
  int myId;
  /** The game setup for the battle just started */
  GameSetup gameSetup;

  @Builder
  private GameStartedEvent(int myId, GameSetup gameSetup) {
    this.myId = myId;
    this.gameSetup = gameSetup;
  }
}
