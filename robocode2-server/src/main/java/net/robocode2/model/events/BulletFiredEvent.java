package net.robocode2.model.events;

import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet has fired a bullet
 * 
 * @author Flemming N. Larsen
 */
public final class BulletFiredEvent implements IEvent {

	/** Fired bullet */
	private final Bullet bullet;

	/**
	 * Creates a new bullet fired event
	 * 
	 * @param bullet
	 *            is the bullet that got fired
	 */
	public BulletFiredEvent(Bullet bullet) {
		this.bullet = bullet;
	}

	/** Returns the bullet that got fired */
	public Bullet getBullet() {
		return bullet;
	}
}