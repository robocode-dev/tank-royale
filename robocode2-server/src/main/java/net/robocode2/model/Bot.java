package net.robocode2.model;

public final class Bot {

	public static final int WIDTH = 40;
	public static final int HEIGHT = 40;

	private final int id;
	private final boolean alive;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double turretDirection;
	private final double radarDirection;
	private final double speed;
	private final Arc scanArc;
	private final Score score;

	public Bot(int id, boolean alive, double energy, Position position, double direction, double turretDiretion,
			double radarDirection, double speed, Arc scanArc, Score score) {
		this.id = id;
		this.alive = alive;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.turretDirection = turretDiretion;
		this.radarDirection = radarDirection;
		this.speed = speed;
		this.scanArc = scanArc;
		this.score = score;
	}

	public int getId() {
		return id;
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isDead() {
		return !alive;
	}

	public double getEnergy() {
		return energy;
	}

	public boolean isDisabled() {
		return new Double(energy).equals(0.0d);
	}

	public Position getPosition() {
		return position;
	}

	public double getDirection() {
		return direction;
	}

	public double getTurretDirection() {
		return turretDirection;
	}

	public double getRadarDirection() {
		return radarDirection;
	}

	public double getSpeed() {
		return speed;
	}

	public Arc getScanArc() {
		return scanArc;
	}

	public Score getScore() {
		return score;
	}

	public static final class Builder {
		private int id;
		private boolean alive;
		private double energy;
		private Position position;
		private double direction;
		private double turretDirection;
		private double radarDirection;
		private double speed;
		private Arc scanArc;
		private Score score;

		public Builder() {
		}

		public Builder(Bot bot) {
			id = bot.id;
			alive = bot.alive;
			energy = bot.energy;
			position = bot.position;
			direction = bot.direction;
			turretDirection = bot.turretDirection;
			radarDirection = bot.radarDirection;
			speed = bot.speed;
			scanArc = bot.scanArc;
			score = bot.score;
		}

		public Bot build() {
			return new Bot(id, alive, energy, position, direction, turretDirection, radarDirection, speed, scanArc,
					score);
		}

		public Builder setId(int id) {
			this.id = id;
			return this;
		}

		public Builder setAlive(boolean alive) {
			this.alive = alive;
			return this;
		}

		public Builder setEnergy(double energy) {
			this.energy = energy;
			return this;
		}

		public Builder setPosition(Position position) {
			this.position = position;
			return this;
		}

		public Builder setDirection(double direction) {
			this.direction = direction;
			return this;
		}

		public Builder setTurretDirection(double turretDirection) {
			this.turretDirection = turretDirection;
			return this;
		}

		public Builder setRadarDirection(double radarDirection) {
			this.radarDirection = radarDirection;
			return this;
		}

		public Builder setSpeed(double speed) {
			this.speed = speed;
			return this;
		}

		public Builder setScanArc(Arc scanArc) {
			this.scanArc = scanArc;
			return this;
		}

		public Builder setScore(Score score) {
			this.score = score;
			return this;
		}

		public int getId() {
			return id;
		}

		public boolean isAlive() {
			return alive;
		}

		public double getEnergy() {
			return energy;
		}

		public Position getPosition() {
			return position;
		}

		public double getDirection() {
			return direction;
		}

		public double getTurretDirection() {
			return turretDirection;
		}

		public double getRadarDirection() {
			return radarDirection;
		}

		public double getSpeed() {
			return speed;
		}

		public Arc getScanArc() {
			return scanArc;
		}

		public Score getScore() {
			return score;
		}
	}
}