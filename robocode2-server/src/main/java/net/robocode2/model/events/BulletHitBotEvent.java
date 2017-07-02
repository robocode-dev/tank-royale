package net.robocode2.model.events;

import net.robocode2.model.ImmutableBullet;

/**
 * Event sent when a bullet hits a bot
 * 
 * @author Flemming N. Larsen
 */
public final class BulletHitBotEvent implements IEvent {

	/** Bullet that hit the bot */
	private final ImmutableBullet bullet;
	/** Bot id of the victim that was hit by the bullet */
	private final int victimId;
	/** Damage dealt to the victim */
	private final double damage;
	/** New energy level of the victim after damage */
	private final double energy;

	/**
	 * Creates a new bullet hit bot event
	 * 
	 * @param bullet
	 *            is the bullet that has hit a bot
	 * @param victimId
	 *            is the bot id of the victim that was hit by the bullet
	 * @param damage
	 *            is the damage dealt to the victim
	 * @param energy
	 *            is the new energy level of the victim after damage
	 */
	public BulletHitBotEvent(ImmutableBullet bullet, int victimId, double damage, double energy) {
		this.bullet = bullet;
		this.victimId = victimId;
		this.damage = damage;
		this.energy = energy;
	}

	/** Returns the bullet that hit the bot */
	public ImmutableBullet getBullet() {
		return bullet;
	}

	/** Returns the victim that was hit by the bullet */
	public int getVictimId() {
		return victimId;
	}

	/** Returns the damage dealt to the victim */
	public double getDamage() {
		return damage;
	}

	/** Returns the new energy level of the victim after damage */
	public double getEnergy() {
		return energy;
	}
}