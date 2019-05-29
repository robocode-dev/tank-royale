package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.robocode2.GameSetup;

/** Event occurring when game has just started */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GameStartedEvent extends Event {
  /** The ID used for identifying your bot in the current battle */
  int myId;
  /** The game setup for the battle just started */
  GameSetup gameSetup;

  @Builder
  private GameStartedEvent(int turnNumber, int myId, GameSetup gameSetup) {
    this.turnNumber = turnNumber;
    this.myId = myId;
    this.gameSetup = gameSetup;
  }
}
