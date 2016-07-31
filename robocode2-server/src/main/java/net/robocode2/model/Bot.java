package net.robocode2.model;

public final class Bot {

	public static final int WIDTH = 40;
	public static final int HEIGHT = 40;

	private final int id;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double turretDirection;
	private final double radarDirection;
	private final double speed;
	private final Arc scanArc;
	private final Score score;
	private final boolean alive;

	public Bot(int id, double energy, Position position, double direction, double turretDiretion, double radarDirection,
			double speed, Arc scanArc, Score score, boolean alive) {
		this.id = id;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.turretDirection = turretDiretion;
		this.radarDirection = radarDirection;
		this.speed = speed;
		this.scanArc = scanArc;
		this.score = score;
		this.alive = alive;
	}

	public int getId() {
		return id;
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

	public boolean isDisabled() {
		return new Double(energy).equals(0.0d);
	}

	public boolean isAlive() {
		return alive;
	}

	public boolean isDead() {
		return !alive;
	}

	public static final class BotBuilder {
		private int id;
		private double energy;
		private Position position;
		private double direction;
		private double turretDirection;
		private double radarDirection;
		private double speed;
		private Arc scanArc;
		private Score score;
		private boolean alive;

		public Bot build() {
			return new Bot(id, energy, position, direction, turretDirection, radarDirection, speed, scanArc, score,
					alive);
		}

		public BotBuilder setId(int id) {
			this.id = id;
			return this;
		}

		public BotBuilder setEnergy(double energy) {
			this.energy = energy;
			return this;
		}

		public BotBuilder setPosition(Position position) {
			this.position = position;
			return this;
		}

		public BotBuilder setDirection(double direction) {
			this.direction = direction;
			return this;
		}

		public BotBuilder setTurretDirection(double turretDirection) {
			this.turretDirection = turretDirection;
			return this;
		}

		public BotBuilder setRadarDirection(double radarDirection) {
			this.radarDirection = radarDirection;
			return this;
		}

		public BotBuilder setSpeed(double speed) {
			this.speed = speed;
			return this;
		}

		public BotBuilder setScanArc(Arc scanArc) {
			this.scanArc = scanArc;
			return this;
		}

		public BotBuilder setScore(Score score) {
			this.score = score;
			return this;
		}

		public BotBuilder setAlive(boolean alive) {
			this.alive = alive;
			return this;
		}
	}
}