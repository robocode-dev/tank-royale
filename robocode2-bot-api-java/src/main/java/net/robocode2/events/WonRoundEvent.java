package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/** Event occurring when a bot has won the round */
@Value
@EqualsAndHashCode(callSuper = true)
public class WonRoundEvent extends Event {

  @Builder
  private WonRoundEvent(int turnNumber) {
    this.turnNumber = turnNumber;
  }
}
