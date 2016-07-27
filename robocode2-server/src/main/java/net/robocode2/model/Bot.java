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
}