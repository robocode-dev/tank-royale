package net.robocode2.model.events;

import net.robocode2.model.Bullet;

public final class BulletMissedEvent implements Event {

	private final Bullet bullet;

	public BulletMissedEvent(Bullet bullet) {
		this.bullet = bullet;
	}

	public Bullet getBullet() {
		return bullet;
	}
}