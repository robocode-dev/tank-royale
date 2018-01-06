package net.robocode2.model.events;

import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet hits another bullet
 * 
 * @author Flemming N. Larsen
 */
public final class BulletHitBulletEvent implements IEvent {

	/** Bullet that hit another bullet */
	private final Bullet bullet;
	/** Bullet that got hit by the bullet */
	private final Bullet hitBullet;

	/**
	 * Creates a bullet hit bullet event
	 * 
	 * @param bullet
	 *            is the bullet that hit another bullet
	 * @param hitBullet
	 *            is the bullet that got hit by the bullet
	 */
	public BulletHitBulletEvent(Bullet bullet, Bullet hitBullet) {
		this.bullet = bullet;
		this.hitBullet = hitBullet;
	}

	/** Returns the bullet that hit another bullet */
	public Bullet getBullet() {
		return bullet;
	}

	/** Returns the bullet that got hit by the bullet */
	public Bullet getHitBullet() {
		return hitBullet;
	}
}