package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has hit a bot */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BulletHitBotEvent extends GameEvent {
  /** ID of the victim bot that got hit */
  int victimId;
  /** Bullet that hit the bot */
  BulletState bullet;
  /** Damage inflicted by the bullet */
  double damage;
  /** Remaining energy level of the bot that got hit */
  double energy;
}
