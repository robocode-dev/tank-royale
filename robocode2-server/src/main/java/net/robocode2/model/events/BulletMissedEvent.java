package net.robocode2.model.events;

import net.robocode2.model.ImmutableBullet;

/**
 * Event sent when a bullet has missed (reached the walls)
 * 
 * @author Flemming N. Larsen
 */
public final class BulletMissedEvent implements IEvent {

	/** Bullet that missed */
	private final ImmutableBullet bullet;

	/**
	 * Creates a bullet missed event
	 * 
	 * @param bullet
	 *            is the bullet that has missed
	 */
	public BulletMissedEvent(ImmutableBullet bullet) {
		this.bullet = bullet;
	}

	/** Returns the bullet that has missed */
	public ImmutableBullet getBullet() {
		return bullet;
	}
}