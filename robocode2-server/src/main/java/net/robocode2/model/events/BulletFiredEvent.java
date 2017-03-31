package net.robocode2.model.events;

import net.robocode2.model.ImmutableBullet;

public final class BulletFiredEvent implements IEvent {

	private final ImmutableBullet bullet;

	public BulletFiredEvent(ImmutableBullet bullet) {
		this.bullet = bullet;
	}

	public ImmutableBullet getBullet() {
		return bullet;
	}
}