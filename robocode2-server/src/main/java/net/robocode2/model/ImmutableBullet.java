package net.robocode2.model;

public final class ImmutableBullet implements IBullet {

	private final int botId;
	private final int bulletId;
	private final double power;
	private final Point firePosition;
	private final double direction;
	private final double speed;
	private final int tick; // Used for calculating position with precision

	public ImmutableBullet(int botId, int bulletId, double power, Point firePosition, double direction, double speed,
			int tick) {

		this.botId = botId;
		this.bulletId = bulletId;
		this.power = power;
		this.firePosition = firePosition;
		this.direction = direction;
		this.speed = speed;
		this.tick = tick;
	}

	public ImmutableBullet(IBullet bullet) {
		this(bullet.getBotId(), bullet.getBulletId(), bullet.getPower(), bullet.getFirePosition(),
				bullet.getDirection(), bullet.getSpeed(), bullet.getTick());
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