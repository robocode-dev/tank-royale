package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has hit a bot */
@Value
@EqualsAndHashCode(callSuper = true)
public class BulletHitBotEvent extends Event {
  /** ID of the victim bot that got hit */
  int victimId;
  /** Bullet that hit the bot */
  BulletState bullet;
  /** Damage inflicted by the bullet */
  double damage;
  /** Remaining energy level of the bot that got hit */
  double energy;

  @Builder
  private BulletHitBotEvent(
      int turnNumber, int victimId, BulletState bullet, double damage, double energy) {
    this.turnNumber = turnNumber;
    this.victimId = victimId;
    this.bullet = bullet;
    this.damage = damage;
    this.energy = energy;
  }
}
