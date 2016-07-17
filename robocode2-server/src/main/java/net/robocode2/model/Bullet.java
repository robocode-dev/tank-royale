package net.robocode2.model;

public final class Bullet {

	private final int botId;
	private final int id;
	private final double power;
	private final Position firePosition;
	private final double direction;
	private final int tick;

	public Bullet(int botId, int id, double power, Position firePosition, double direction, int tick) {
		this.botId = botId;
		this.id = id;
		this.power = power;
		this.firePosition = firePosition;
		this.direction = direction;
		this.tick = tick;
	}

	public int getBotId() {
		return botId;
	}

	public int getId() {
		return id;
	}

	public double getPower() {
		return power;
	}

	public Position getfirePosition() {
		return firePosition;
	}

	public double getDirection() {
		return direction;
	}

	public int getTick() {
		return tick;
	}
}