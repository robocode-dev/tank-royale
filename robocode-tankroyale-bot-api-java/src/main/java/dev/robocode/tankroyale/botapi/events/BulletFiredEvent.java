package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/** Event occurring when a bullet has been fired from a bot */
@SuppressWarnings("unused")
public final class BulletFiredEvent extends Event {

  /** Bullet that was fired */
  private final BulletState bullet;

  public BulletFiredEvent(int turnNumber, BulletState bullet) {
    super(turnNumber);
    this.bullet = bullet;
  }

  /** Returns the bullet that was fired */
  public BulletState getBullet() {
    return bullet;
  }
}
