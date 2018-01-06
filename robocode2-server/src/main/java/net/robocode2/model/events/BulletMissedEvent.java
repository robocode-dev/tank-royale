package net.robocode2.model.events;

import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet has missed (reached the walls)
 * 
 * @author Flemming N. Larsen
 */
public final class BulletMissedEvent implements IEvent {

	/** Bullet that missed */
	private final Bullet bullet;

	/**
	 * Creates a bullet missed event
	 * 
	 * @param bullet
	 *            is the bullet that has missed
	 */
	public BulletMissedEvent(Bullet bullet) {
		this.bullet = bullet;
	}

	/** Returns the bullet that has missed */
	public Bullet getBullet() {
		return bullet;
	}
}