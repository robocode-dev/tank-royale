package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/** Event occurring when the bot has hit a wall */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BotHitWallEvent extends Event {
  /** ID of the victim bot that hit the wall */
  int victimId;

  @Builder
  private BotHitWallEvent(int turnNumber, int victimId) {
    this.turnNumber = turnNumber;
    this.victimId = victimId;
  }
}
