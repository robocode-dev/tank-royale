package net.robocode2.model.events;

import net.robocode2.model.Bullet;

public final class BulletHitBotEvent implements Event {

	private final Bullet bullet;
	private final int victimId;
	private final double damage;
	private final double energy;

	public BulletHitBotEvent(Bullet bullet, int victimId, double damage, double energy) {
		this.bullet = bullet;
		this.victimId = victimId;
		this.damage = damage;
		this.energy = energy;
	}

	public Bullet getBullet() {
		return bullet;
	}

	public int getVictimId() {
		return victimId;
	}

	public double getDamage() {
		return damage;
	}

	public double getEnergy() {
		return energy;
	}
}