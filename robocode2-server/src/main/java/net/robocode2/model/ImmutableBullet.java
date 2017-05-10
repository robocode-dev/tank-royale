package net.robocode2.model;

public final class ImmutableBullet implements IBullet {

	private final int botId;
	private final int bulletId;
	private final double power;
	private final Point firePosition;
	private final double direction;
	private final double speed;
	private final int tick; // Used for calculating position with precision

	public ImmutableBullet(IBullet bullet) {
		botId = bullet.getBotId();
		bulletId = bullet.getBulletId();
		power = bullet.getPower();
		firePosition = bullet.getFirePosition();
		direction = bullet.getDirection();
		speed = bullet.getSpeed();
		tick = bullet.getTick();
	}

	@Override
	public int getBotId() {
		return botId;
	}

	@Override
	public int getBulletId() {
		return bulletId;
	}

	@Override
	public double getPower() {
		return power;
	}

	@Override
	public Point getFirePosition() {
		return firePosition;
	}

	@Override
	public double getDirection() {
		return direction;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public int getTick() {
		return tick;
	}
}