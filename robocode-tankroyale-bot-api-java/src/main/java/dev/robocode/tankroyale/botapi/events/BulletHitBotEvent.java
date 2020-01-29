package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/** Event occurring when a bullet has hit a bot */
@SuppressWarnings("unused")
public final class BulletHitBotEvent extends Event {

  /** ID of the victim bot that got hit */
  private final int victimId;

  /** Bullet that hit the bot */
  private final BulletState bullet;

  /** Damage inflicted by the bullet */
  private final double damage;

  /** Remaining energy level of the bot that got hit */
  private final double energy;

  public BulletHitBotEvent(
      int turnNumber, int victimId, BulletState bullet, double damage, double energy) {
    super(turnNumber);
    this.victimId = victimId;
    this.bullet = bullet;
    this.damage = damage;
    this.energy = energy;
  }

  /** Returns the ID of the victim bot that got hit */
  public int getVictimId() {
    return victimId;
  }

  /** Returns the bullet that hit the bot */
  public BulletState getBullet() {
    return bullet;
  }

  /** Returns the damage inflicted by the bullet */
  public double getDamage() {
    return damage;
  }

  /** Returns the remaining energy level of the bot that got hit */
  public double getEnergy() {
    return energy;
  }
}
