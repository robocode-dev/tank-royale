package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/**
 * Event occurring when a bullet has hit your bot.
 */
@SuppressWarnings("unused")
public final class HitByBulletEvent extends BotEvent {

    // Bullet that hit the bot.
    private final BulletState bullet;

    // Damage inflicted by the bullet.
    private final double damage;

    // Remaining energy level after the bullet hit.
    private final double energy;

    /**
     * Initializes a new instance of the HitByBulletEvent class.
     *
     * @param turnNumber is the turn number when the bullet has hit a bot.
     * @param bullet     is the bullet that hit the bot.
     * @param damage     is the damage inflicted by the bullet.
     * @param energy     is the remaining energy level of the bot that got hit.
     */
    public HitByBulletEvent(int turnNumber, BulletState bullet, double damage, double energy) {
        super(turnNumber);
        this.bullet = bullet;
        this.damage = damage;
        this.energy = energy;
    }

    /**
     * Returns the bullet that hit your bot.
     *
     * @return The bullet that hit your bot.
     */
    public BulletState getBullet() {
        return bullet;
    }

    /**
     * Returns the damage inflicted by the bullet.
     *
     * @return The damage inflicted by the bullet.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Returns the remaining energy level after the bullet hit.
     *
     * @return The remaining energy level after the bullet hit.
     */
    public double getEnergy() {
        return energy;
    }
}
