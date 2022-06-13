package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/**
 * Event occurring when a bullet has been fired from a bot.
 */
@SuppressWarnings("unused")
public final class BulletFiredEvent extends BotEvent {

    // Bullet that was fired.
    private final BulletState bullet;

    /**
     * Initializes a new instance of the BulletFiredEvent class.
     *
     * @param turnNumber is the turn number when the bullet was fired.
     * @param bullet     is the bullet that was fired.
     */
    public BulletFiredEvent(int turnNumber, BulletState bullet) {
        super(turnNumber);
        this.bullet = bullet;
    }

    /**
     * Returns the bullet that was fired.
     *
     * @return The bullet that was fired.
     */
    public BulletState getBullet() {
        return bullet;
    }
}
