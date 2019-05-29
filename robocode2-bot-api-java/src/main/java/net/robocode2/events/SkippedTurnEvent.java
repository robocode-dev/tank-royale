package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
 * for a specific turn
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SkippedTurnEvent extends Event {

  @Builder
  private SkippedTurnEvent(int turnNumber) {
    this.turnNumber = turnNumber;
  }
}
