package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/** Event occurring when a bullet has hit a wall */
@SuppressWarnings("unused")
public final class BulletHitWallEvent extends Event {

  /** Bullet that has hit a wall */
  private final BulletState bullet;

  public BulletHitWallEvent(int turnNumber, BulletState bullet) {
    super(turnNumber);
    this.bullet = bullet;
  }

  /** Returns the bullet that has hit a wall */
  public BulletState getBullet() {
    return bullet;
  }
}
