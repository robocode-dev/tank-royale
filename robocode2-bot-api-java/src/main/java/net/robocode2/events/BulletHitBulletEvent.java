package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has collided with another bullet */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BulletHitBulletEvent extends GameEvent {
  /** Bullet that hit another bullet */
  BulletState bullet;
  /** The other bullet that was hit by the bullet */
  BulletState hitBullet;
}
