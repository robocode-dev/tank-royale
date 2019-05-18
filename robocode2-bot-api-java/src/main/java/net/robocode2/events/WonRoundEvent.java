package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BotState;
import net.robocode2.BulletState;

import java.util.List;

/** Event occurring when a bot has won the round */
@Value
@EqualsAndHashCode(callSuper = true)
public class WonRoundEvent extends GameEvent {

  @Builder
  private WonRoundEvent(int turnNumber) {
    this.turnNumber = turnNumber;
  }
}
