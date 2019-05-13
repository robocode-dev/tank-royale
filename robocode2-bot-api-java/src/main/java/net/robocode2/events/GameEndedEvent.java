package net.robocode2.events;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/** Event occurring when game has just ended */
@Value
@Builder
public class GameEndedEvent {

  /** Number of rounds played */
  int numberOfRounds;

  /** Results of the battle */
  List<BotResults> results;
}
