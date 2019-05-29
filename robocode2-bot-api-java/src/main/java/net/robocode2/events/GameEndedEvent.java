package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.robocode2.BotResults;

import java.util.List;

/** Event occurring when game has just ended */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GameEndedEvent extends Event {
  /** Number of rounds played */
  int numberOfRounds;
  /** Results of the battle */
  List<BotResults> results;

  @Builder
  private GameEndedEvent(int turnNumber, int numberOfRounds, List<BotResults> results) {
    this.turnNumber = turnNumber;
    this.numberOfRounds = numberOfRounds;
    this.results = results;
  }
}
