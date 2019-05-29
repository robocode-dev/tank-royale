package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has hit a wall */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BulletHitWallEvent extends Event {
  /** Bullet that has hit a wall */
  BulletState bullet;

  @Builder
  private BulletHitWallEvent(int turnNumber, BulletState bullet) {
    this.turnNumber = turnNumber;
    this.bullet = bullet;
  }
}
