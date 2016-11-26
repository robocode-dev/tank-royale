package net.robocode2.model.events;

import net.robocode2.model.Bullet;

public final class BulletHitBotEvent implements Event {

	private final Bullet bullet;
	private final int botId;
	private final double damage;
	private final double energy;

	public BulletHitBotEvent(Bullet bullet, int botId, double damage, double energy) {
		this.bullet = bullet;
		this.botId = botId;
		this.damage = damage;
		this.energy = energy;
	}

	public Bullet getBullet() {
		return bullet;
	}

	public int getBotId() {
		return botId;
	}

	public double getDamage() {
		return damage;
	}

	public double getEnergy() {
		return energy;
	}
}