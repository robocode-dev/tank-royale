package net.robocode2.model.events;

import net.robocode2.model.ImmutableBullet;

public final class BulletHitBulletEvent implements IEvent {

	private final ImmutableBullet bullet;
	private final ImmutableBullet hitBullet;

	public BulletHitBulletEvent(ImmutableBullet bullet, ImmutableBullet hitBullet) {
		this.bullet = bullet;
		this.hitBullet = hitBullet;
	}

	public ImmutableBullet getBullet() {
		return bullet;
	}

	public ImmutableBullet getHitBullet() {
		return hitBullet;
	}
}