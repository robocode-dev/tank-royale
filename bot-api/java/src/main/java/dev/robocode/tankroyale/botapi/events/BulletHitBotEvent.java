package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/**
 * Event occurring when a bullet has hit a bot.
 */
@SuppressWarnings("unused")
public final class BulletHitBotEvent extends BotEvent {

    // id of the victim bot that got hit.
    private final int victimId;

    // Bullet that hit the bot.
    private final BulletState bullet;

    // Damage inflicted by the bullet.
    private final double damage;

    // Remaining energy level of the bot that got hit.
    private final double energy;

    /**
     * Initializes a new instance of the BulletHitBotEvent class.
     *
     * @param turnNumber is the turn number when the bullet has hit a bot.
     * @param victimId   is the id of the victim bot that got hit.
     * @param bullet     is the bullet that hit the bot.
     * @param damage     is the damage inflicted by the bullet.
     * @param energy     is the remaining energy level of the bot that got hit.
     */
    public BulletHitBotEvent(int turnNumber, int victimId, BulletState bullet, double damage, double energy) {
        super(turnNumber);
        this.victimId = victimId;
        this.bullet = bullet;
        this.damage = damage;
        this.energy = energy;
    }

    /**
     * Returns the id of the victim bot that got hit.
     *
     * @return The id of the victim bot that got hit.
     */
    public int getVictimId() {
        return victimId;
    }

    /**
     * Returns the bullet that hit the bot.
     *
     * @return The bullet that hit the bot.
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
     * Returns the remaining energy level of the bot that got hit.
     *
     * @return The remaining energy level of the bot that got hit.
     */
    public double getEnergy() {
        return energy;
    }
}
