package net.robocode2.model;

import net.robocode2.game.MathUtil;

public final class Bot implements ImmutableBot {

	private final int id;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double gunDirection;
	private final double radarDirection;
	private final double speed;
	private final double gunHeat;
	private final Arc scanArc;
	private final Score score;

	public Bot(int id, double energy, Position position, double direction, double gunDirection, double radarDirection,
			double speed, double gunHeat, Arc scanArc, Score score) {
		this.id = id;
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

	@Override
	public int getId() {
		return id;
	}

	@Override
	public double getEnergy() {
		return energy;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public double getDirection() {
		return direction;
	}

	@Override
	public double getGunDirection() {
		return gunDirection;
	}

	@Override
	public double getRadarDirection() {
		return radarDirection;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public double getGunHeat() {
		return gunHeat;
	}

	@Override
	public Arc getScanArc() {
		return scanArc;
	}

	@Override
	public Score getScore() {
		return score;
	}

	public static final class Builder implements ImmutableBot {
		private int id;
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
			return new Bot(id, energy, position, direction, gunDirection, radarDirection, speed, gunHeat, scanArc,
					score);
		}

		public Builder setId(int id) {
			this.id = id;
			return this;
		}

		public Builder setEnergy(double energy) {
			this.energy = energy;
			return this;
		}

		public Builder setDisabled() {
			this.energy = 0.0;
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

		@Override
		public int getId() {
			return id;
		}

		@Override
		public double getEnergy() {
			return energy;
		}

		public boolean isAlive() {
			return energy >= 0;
		}

		public boolean isDead() {
			return energy < 0;
		}

		public boolean isDisabled() {
			return isAlive() && MathUtil.isNear(energy, 0);
		}

		@Override
		public Position getPosition() {
			return position;
		}

		@Override
		public double getDirection() {
			return direction;
		}

		@Override
		public double getGunDirection() {
			return gunDirection;
		}

		@Override
		public double getRadarDirection() {
			return radarDirection;
		}

		@Override
		public double getSpeed() {
			return speed;
		}

		@Override
		public double getGunHeat() {
			return gunHeat;
		}

		@Override
		public Arc getScanArc() {
			return scanArc;
		}

		@Override
		public Score getScore() {
			return score;
		}

		/**
		 * Adds damage to the bot.
		 * 
		 * @param damage
		 * @return true if the robot got killed due to the damage, false otherwise.
		 */
		public boolean addDamage(double damage) {
			boolean aliveBefore = isAlive();
			energy -= damage;
			return isDead() && aliveBefore;
		}

		public void increaseEnergy(double gain) {
			energy += gain;
		}

		public void bounceBackPosition(double distance) {
			double angle = Math.toRadians(direction - 180);
			double x = position.x + Math.cos(angle) * distance;
			double y = position.y + Math.sin(angle) * distance;
			position = new Position(x, y);
		}
	}
}