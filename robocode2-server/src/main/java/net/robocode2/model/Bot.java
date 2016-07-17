package net.robocode2.model;

public final class Bot {

	private final int id;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double turretDirection;
	private final double radarDirection;
	private final double speed;
	private final ScanArc scanArc;

	public Bot(int id, double energy, Position position, double direction, double turretDiretion, double radarDirection,
			double speed, ScanArc scanArc) {
		this.id = id;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.turretDirection = turretDiretion;
		this.radarDirection = radarDirection;
		this.speed = speed;
		this.scanArc = scanArc;
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

	public ScanArc getScanArc() {
		return scanArc;
	}
}