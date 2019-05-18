package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has been fired from a bot */
@Value
@EqualsAndHashCode(callSuper = true)
public class BulletFiredEvent extends Event {
  /** Bullet that was fired */
  BulletState bullet;

  @Builder
  private BulletFiredEvent(int turnNumber, BulletState bullet) {
    this.turnNumber = turnNumber;
    this.bullet = bullet;
  }
}
