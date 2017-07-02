package net.robocode2.model.events;

import net.robocode2.model.ImmutableBullet;

/**
 * Event sent when a bullet has fired a bullet
 * 
 * @author Flemming N. Larsen
 */
public final class BulletFiredEvent implements IEvent {

	/** Fired bullet */
	private final ImmutableBullet bullet;

	/**
	 * Creates a new bullet fired event
	 * 
	 * @param bullet
	 *            is the bullet that got fired
	 */
	public BulletFiredEvent(ImmutableBullet bullet) {
		this.bullet = bullet;
	}

	/** Returns the bullet that got fired */
	public ImmutableBullet getBullet() {
		return bullet;
	}
}