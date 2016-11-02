package net.robocode2.model;

public final class ScannedBot {

	private final int id;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double speed;

	public ScannedBot(int id, double energy, Position position, double direction, double speed) {
		this.id = id;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.speed = speed;
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

	public double getSpeed() {
		return speed;
	}

	public static final class ScannedBotBuilder {
		private int id;
		private double energy;
		private Position position;
		private double direction;
		private double speed;

		public ScannedBot build() {
			return new ScannedBot(id, energy, position, direction, speed);
		}

		public ScannedBotBuilder setId(int id) {
			this.id = id;
			return this;
		}

		public ScannedBotBuilder setEnergy(double energy) {
			this.energy = energy;
			return this;
		}

		public ScannedBotBuilder setPosition(Position position) {
			this.position = position;
			return this;
		}

		public ScannedBotBuilder setDirection(double direction) {
			this.direction = direction;
			return this;
		}

		public ScannedBotBuilder setSpeed(double speed) {
			this.speed = speed;
			return this;
		}
	}
}