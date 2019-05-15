package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.GameSetup;

/** Event occurring when game has just started */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class GameStartedEvent extends GameEvent {

  /** The ID used for identifying your bot in the current battle */
  int myId;

  /** The game setup for the battle just started */
  GameSetup gameSetup;
}
