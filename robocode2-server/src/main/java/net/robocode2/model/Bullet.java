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

	/**
	 * Calculates the current bullet position based on the fire position and current tick.
	 */
	public Position calcPosition() {
		return calcPosition(firePosition, direction, speed, tick);
	}

	public static Position calcPosition(Position firePosition, double direction, double speed, int tick) {
		double angle = Math.toRadians(direction);
		double distance = speed * tick;
		double x = firePosition.getX() + Math.cos(angle) * distance;
		double y = firePosition.getY() + Math.sin(angle) * distance;
		return new Position(x, y);
	}

	public static final class Builder {
		private int botId;
		private int bulletId;
		private double power;
		private Position firePosition;
		private double direction;
		private double speed;
		private int tick;

		public Builder() {
		}

		public Builder(Bullet bullet) {
		}

		public Bullet build() {
			return new Bullet(botId, bulletId, power, firePosition, direction, speed, tick);
		}

		public Builder setBotId(int botId) {
			this.botId = botId;
			return this;
		}

		public Builder setBulletId(int bulletId) {
			this.bulletId = bulletId;
			return this;
		}

		public Builder setPower(double power) {
			this.power = power;
			return this;
		}

		public Builder setFirePosition(Position firePosition) {
			this.firePosition = firePosition;
			return this;
		}

		public Builder setDirection(double direction) {
			this.direction = direction;
			return this;
		}

		public Builder setSpeed(double speed) {
			this.speed = speed;
			return this;
		}

		public Builder setTick(int tick) {
			this.tick = tick;
			return this;
		}

		public Builder incrementTick() {
			this.tick++;
			return this;
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

		public Position calcPosition() {
			return Bullet.calcPosition(firePosition, direction, speed, tick);
		}

		public Position calcNextPosition() {
			return Bullet.calcPosition(firePosition, direction, speed, tick + 1);
		}
	}
}