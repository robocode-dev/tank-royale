package net.robocode2.model.events;

import net.robocode2.model.Bullet;

public final class HitByBulletEvent implements Event {

	private final Bullet bullet;
	private final double damage;

	public HitByBulletEvent(Bullet bullet, double damage) {
		this.bullet = bullet;
		this.damage = damage;
	}

	public Bullet getBullet() {
		return bullet;
	}

	public double getDamage() {
		return damage;
	}
}