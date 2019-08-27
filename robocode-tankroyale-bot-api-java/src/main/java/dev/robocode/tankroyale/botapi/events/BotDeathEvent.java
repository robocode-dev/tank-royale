package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/** Event occurring when a bot has died */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BotDeathEvent extends Event {
  /** ID of the bot that has died */
  int victimId;

  @Builder
  private BotDeathEvent(int turnNumber, int victimId) {
    this.turnNumber = turnNumber;
    this.victimId = victimId;
  }
}
