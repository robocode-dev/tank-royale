package net.robocode2.model.events;

import net.robocode2.model.ImmutableBullet;

public final class BulletMissedEvent implements Event {

	private final ImmutableBullet bullet;

	public BulletMissedEvent(ImmutableBullet bullet) {
		this.bullet = bullet;
	}

	public ImmutableBullet getBullet() {
		return bullet;
	}
}