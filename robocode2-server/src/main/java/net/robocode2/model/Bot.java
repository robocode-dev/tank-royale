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

	public Bot(int id, double energy, Position position, double direction, double turretDiretion, double radarDirection,
			double speed, Arc scanArc, Score score) {
		this.id = id;
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

	private Bot(BotBuilder builder) {
		this.id = builder.id;
		this.energy = builder.energy;
		this.position = builder.position;
		this.direction = builder.direction;
		this.turretDirection = builder.turretDirection;
		this.radarDirection = builder.radarDirection;
		this.speed = builder.speed;
		this.scanArc = builder.scanArc;
		this.score = builder.score;
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

		public Bot build() {
			return new Bot(this);
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
	}
}