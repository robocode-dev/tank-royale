package net.robocode2.model;

public final class Bullet {

	private final int botId;
	private final int bulletId;
	private final double power;
	private final Point firePosition;
	private final double direction;
	private final double speed;
	private final int tick; // Used for calculating position with precision

	public Bullet(int botId, int bulletId, double power, Point firePosition, double direction, double speed,
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

	public Point getFirePosition() {
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
	public Point calcPosition() {
		return calcPosition(firePosition, direction, speed, tick);
	}

	public static Point calcPosition(Point firePosition, double direction, double speed, int tick) {
		double angle = Math.toRadians(direction);
		double distance = speed * tick;
		double x = firePosition.x + Math.cos(angle) * distance;
		double y = firePosition.y + Math.sin(angle) * distance;
		return new Point(x, y);
	}

	public static final class Builder {
		private int botId;
		private int bulletId;
		private double power;
		private Point firePosition;
		private double direction;
		private double speed;
		private int tick;

		public Builder() {
		}

		public Builder(Bullet bullet) {
			botId = bullet.getBotId();
			bulletId = bullet.getBulletId();
			power = bullet.getPower();
			firePosition = bullet.getFirePosition();
			direction = bullet.getDirection();
			speed = bullet.getSpeed();
			tick = bullet.getTick();
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

		public Builder setFirePosition(Point firePosition) {
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

		public Point getFirePosition() {
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

		public Point calcPosition() {
			return Bullet.calcPosition(firePosition, direction, speed, tick);
		}

		public Point calcNextPosition() {
			return Bullet.calcPosition(firePosition, direction, speed, tick + 1);
		}
	}
}