package net.robocode2.model.events;

import net.robocode2.model.Bullet;

public final class BulletHitBulletEvent implements Event {

	private final Bullet bullet;
	private final Bullet hitBullet;

	public BulletHitBulletEvent(Bullet bullet, Bullet hitBullet) {
		this.bullet = bullet;
		this.hitBullet = hitBullet;
	}

	public Bullet getBullet() {
		return bullet;
	}

	public Bullet getHitBullet() {
		return hitBullet;
	}
}