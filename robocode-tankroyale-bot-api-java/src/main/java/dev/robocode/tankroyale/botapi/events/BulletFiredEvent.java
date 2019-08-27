package dev.robocode.tankroyale.botapi.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import dev.robocode.tankroyale.botapi.BulletState;

/** Event occurring when a bullet has been fired from a bot */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BulletFiredEvent extends Event {
  /** Bullet that was fired */
  BulletState bullet;

  @Builder
  @SuppressWarnings("UnusedDeclaration")
  private BulletFiredEvent(int turnNumber, BulletState bullet) {
    this.turnNumber = turnNumber;
    this.bullet = bullet;
  }
}
