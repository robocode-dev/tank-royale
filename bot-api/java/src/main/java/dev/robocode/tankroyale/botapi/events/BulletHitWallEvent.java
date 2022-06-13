package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/**
 * Event occurring when a bullet has hit a wall.
 */
@SuppressWarnings("unused")
public final class BulletHitWallEvent extends BotEvent {

    // Bullet that has hit a wall.
    private final BulletState bullet;

    /**
     * Initializes a new instance of the BulletHitWallEvent class.
     *
     * @param turnNumber is the turn number when the bullet has hit a wall.
     * @param bullet     is the bullet that has hit a wall.
     */
    public BulletHitWallEvent(int turnNumber, BulletState bullet) {
        super(turnNumber);
        this.bullet = bullet;
    }

    /**
     * Returns the bullet that has hit a wall.
     *
     * @return The bullet that has hit a wall.
     */
    public BulletState getBullet() {
        return bullet;
    }
}
