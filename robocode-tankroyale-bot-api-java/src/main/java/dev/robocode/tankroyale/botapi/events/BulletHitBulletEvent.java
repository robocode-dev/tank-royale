package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import dev.robocode.tankroyale.botapi.BulletState;

/** Event occurring when a bullet has collided with another bullet */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BulletHitBulletEvent extends Event {
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
