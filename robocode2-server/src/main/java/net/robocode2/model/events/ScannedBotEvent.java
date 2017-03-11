package net.robocode2.model.events;

import net.robocode2.model.Position;

public final class ScannedBotEvent implements Event {

	private final int scannedByBotId;
	private final int scannedBotId;
	private final double energy;
	private final Position position;
	private final double direction;
	private final double speed;

	public ScannedBotEvent(int scannedByBotId, int scannedBotId, double energy, Position position, double direction,
			double speed) {
		this.scannedByBotId = scannedByBotId;
		this.scannedBotId = scannedBotId;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.speed = speed;
	}

	public int getScannedByBotId() {
		return scannedByBotId;
	}

	public int getScannedBotId() {
		return scannedBotId;
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
}