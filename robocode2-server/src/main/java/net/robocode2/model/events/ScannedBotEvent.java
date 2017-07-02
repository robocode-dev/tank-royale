package net.robocode2.model.events;

import net.robocode2.model.Point;

/**
 * Event sent when a bot got scanned
 * 
 * @author Flemming N. Larsen
 *
 */
public final class ScannedBotEvent implements IEvent {

	/** Bot id of the bot that scanned the bot */
	private final int scannedByBotId;
	/** Bot id of the bot that got scanned */
	private final int scannedBotId;
	/** Energy level of the scanned bot */
	private final double energy;
	/** Position of the scanned bot */
	private final Point position;
	/** Driving direction of the scanned bot */
	private final double direction;
	/** Speed of the scanned bot */
	private final double speed;

	/**
	 * Creates a new bot scanned event
	 * 
	 * @param scannedByBotId
	 *            is the bot id of the bot that scanned the bot
	 * @param scannedBotId
	 *            is the bot id of the bot that got scanned
	 * @param energy
	 *            is the energy level of the scanned bot
	 * @param position
	 *            is the position of the scanned bot
	 * @param direction
	 *            is the driving direction of the scanned bot
	 * @param speed
	 *            is the speed of the scanned bot
	 */
	public ScannedBotEvent(int scannedByBotId, int scannedBotId, double energy, Point position, double direction,
			double speed) {
		this.scannedByBotId = scannedByBotId;
		this.scannedBotId = scannedBotId;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.speed = speed;
	}

	/** Returns the bot id of the bot that scanned the bot */
	public int getScannedByBotId() {
		return scannedByBotId;
	}

	/** Returns the bot id of the bot that got scanned */
	public int getScannedBotId() {
		return scannedBotId;
	}

	/** Returns the energy level of the scanned bot */
	public double getEnergy() {
		return energy;
	}

	/** Returns the the position of the scanned bot */
	public Point getPosition() {
		return position;
	}

	/** Returns the the driving direction of the scanned bot */
	public double getDirection() {
		return direction;
	}

	/** Returns the the speed of the scanned bot */
	public double getSpeed() {
		return speed;
	}
}