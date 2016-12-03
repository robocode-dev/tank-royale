package net.robocode2.model.events;

import net.robocode2.model.Bullet;

public final class BulletFiredEvent implements Event {

	private final Bullet bullet;

	public BulletFiredEvent(Bullet bullet) {
		this.bullet = bullet;
	}

	public Bullet getBullet() {
		return bullet;
	}
}