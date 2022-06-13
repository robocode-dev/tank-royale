package dev.robocode.tankroyale.botapi.events;

import dev.robocode.tankroyale.botapi.BulletState;

/**
 * Event occurring when a bullet has collided with another bullet.
 */
@SuppressWarnings("unused")
public final class BulletHitBulletEvent extends BotEvent {

    // Bullet that hit another bullet.
    private final BulletState bullet;

    // The other bullet that was hit by the bullet.
    private final BulletState hitBullet;

    /**
     * Initializes a new instance of the BulletHitBulletEvent class.
     *
     * @param turnNumber is the turn number when the two bullet collided.
     * @param bullet     is the bullet that hit another bullet.
     * @param hitBullet  is the other bullet that was hit by the bullet.
     */
    public BulletHitBulletEvent(int turnNumber, BulletState bullet, BulletState hitBullet) {
        super(turnNumber);
        this.bullet = bullet;
        this.hitBullet = hitBullet;
    }

    /**
     * Returns the bullet that hit another bullet.
     *
     * @return The bullet that hit another bullet.
     */
    public BulletState getBullet() {
        return bullet;
    }

    /**
     * Returns the other bullet that was hit by the bullet.
     *
     * @return The other bullet that was hit by the bullet.
     */
    public BulletState getHitBullet() {
        return hitBullet;
    }
}
