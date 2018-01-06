package net.robocode2.model.events;

import net.robocode2.model.Bullet;

/**
 * Event sent when a bot is hit by a bullet.
 * 
 * @author Flemming N. Larsen
 */
public final class HitByBulletEvent implements IEvent {

	/** Bullet that hit the bot */
	private final Bullet bullet;
	/** Damage dealt to the bot */
	private final double damage;
	/** New energy level of the bot after damage */
	private final double energy;

	/**
	 * Creates a new bullet hit bot event
	 * 
	 * @param bullet
	 *            is the bullet that has hit a bot
	 * @param damage
	 *            is the damage dealt to the victim
	 * @param energy
	 *            is the new energy level of the victim after damage
	 */
	public HitByBulletEvent(Bullet bullet, double damage, double energy) {
		this.bullet = bullet;
		this.damage = damage;
		this.energy = energy;
	}

	/** Returns the bullet that hit the bot */
	public Bullet getBullet() {
		return bullet;
	}

	/** Returns the damage dealt to the bot */
	public double getDamage() {
		return damage;
	}

	/** Returns the new energy level of the bot after damage */
	public double getEnergy() {
		return energy;
	}
}