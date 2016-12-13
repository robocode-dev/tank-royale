package net.robocode2.model;

public final class Bullet {

	private final int botId;
	private final int bulletId;
	private final double power;
	private final Position firePosition;
	private final double direction;
	private final double speed;
	private final int tick; // Used for calculating position with precision

	public Bullet(int botId, int bulletId, double power, Position firePosition, double direction, double speed,
			int tick) {
		this.botId = botId;
		this.bulletId = bulletId;
		this.power = power;
		this.firePosition = firePosition;
		this.direction = direction;
		this.speed = speed;
		this.tick = tick;
	}

	public int getBotId() {
		return botId;
	}

	public int getBulletId() {
		return bulletId;
	}

	public double getPower() {
		return power;
	}

	public Position getFirePosition() {
		return firePosition;
	}

	public double getDirection() {
		return direction;
	}

	public double getSpeed() {
		return speed;
	}

	public int getTick() {
		return tick;
	}
}