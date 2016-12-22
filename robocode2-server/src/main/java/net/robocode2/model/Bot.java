package net.robocode2.model;

public final class Bot {

	private final int id;
	private final boolean alive;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double gunDirection;
	private final double radarDirection;
	private final double speed;
	private final double gunHeat;
	private final Arc scanArc;
	private final Score score;

	public Bot(int id, boolean alive, double energy, Position position, double direction, double gunDirection,
			double radarDirection, double speed, double gunHeat, Arc scanArc, Score score) {
		this.id = id;
		this.alive = alive;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.gunDirection = gunDirection;
		this.radarDirection = radarDirection;
		this.speed = speed;
		this.gunHeat = gunHeat;
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

	public double getGunDirection() {
		return gunDirection;
	}

	public double getRadarDirection() {
		return radarDirection;
	}

	public double getSpeed() {
		return speed;
	}

	public double getGunHeat() {
		return gunHeat;
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
		private double gunDirection;
		private double radarDirection;
		private double speed;
		private double gunHeat;
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
			gunDirection = bot.gunDirection;
			radarDirection = bot.radarDirection;
			speed = bot.speed;
			gunHeat = bot.gunHeat;
			scanArc = bot.scanArc;
			score = bot.score;
		}

		public Bot build() {
			return new Bot(id, alive, energy, position, direction, gunDirection, radarDirection, speed, gunHeat,
					scanArc, score);
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

		public Builder setGunDirection(double gunDirection) {
			this.gunDirection = gunDirection;
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

		public Builder setGunHeat(double gunHeat) {
			this.gunHeat = gunHeat;
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

		public double getGunDirection() {
			return gunDirection;
		}

		public double getRadarDirection() {
			return radarDirection;
		}

		public double getSpeed() {
			return speed;
		}

		public double getGunHeat() {
			return gunHeat;
		}

		public Arc getScanArc() {
			return scanArc;
		}

		public Score getScore() {
			return score;
		}
	}
}