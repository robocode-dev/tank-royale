package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/** Event occurring when a bot has collided with another bot */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BotHitBotEvent extends Event {
  /** ID of the victim bot that got hit */
  int victimId;
  /** ID of the bot that hit another bot */
  int botId;
  /** Remaining energy level of the victim bot */
  double energy;
  /** X coordinate of victim bot */
  double x;
  /** Y coordinate of victim bot */
  double y;
  /** Flag specifying, if the victim bot got rammed */
  boolean rammed;

  @Builder
  private BotHitBotEvent(
      int turnNumber, int victimId, int botId, double energy, double x, double y, boolean rammed) {
    this.turnNumber = turnNumber;
    this.victimId = victimId;
    this.botId = botId;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.rammed = rammed;
  }
}
