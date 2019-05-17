package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has collided with another bullet */
@Value
@EqualsAndHashCode(callSuper = true)
public class BulletHitBulletEvent extends GameEvent {
  /** Bullet that hit another bullet */
  BulletState bullet;
  /** The other bullet that was hit by the bullet */
  BulletState hitBullet;

  @Builder
  private BulletHitBulletEvent(int turnNumber, BulletState bullet, BulletState hitBullet) {
    this.turnNumber = turnNumber;
    this.bullet = bullet;
    this.hitBullet = hitBullet;
  }
}
