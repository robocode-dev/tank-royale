package dev.robocode.tankroyale.botapi;

/** Bullet state */
@SuppressWarnings("unused")
public final class BulletState {

  /** ID of the bullet */
  private final int bulletId;

  /** ID of the bot that fired the bullet */
  private final int ownerId;

  /** Bullet firepower level */
  private final double power;

  /** X coordinate */
  private final double x;

  /** Y coordinate */
  private final double y;

  /** Direction in degrees */
  private final double direction;

  /** Speed measured in pixels per turn */
  private final double speed;

  public BulletState(int bulletId, int ownerId, double power, double x, double y, double direction, double speed) {
    this.bulletId = bulletId;
    this.ownerId = ownerId;
    this.power = power;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.speed = speed;
  }

  /** Returns the ID of the bullet */
  public int getBulletId() {
    return bulletId;
  }

  /** Returns the ID of the bot that fired the bullet */
  public int getOwnerId() {
    return ownerId;
  }

  /** Returns the bullet firepower level */
  public double getPower() {
    return power;
  }

  /** Returns the X coordinate */
  public double getX() {
    return x;
  }

  /** Returns the Y coordinate */
  public double getY() {
    return y;
  }

  /** Returns the direction in degrees */
  public double getDirection() {
    return direction;
  }

  /** Returns the speed measured in pixels per turn */
  public double getSpeed() {
    return speed;
  }
}
