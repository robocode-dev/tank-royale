package net.robocode2.model;

import net.robocode2.util.MathUtil;

/**
 * Bot interface.
 * 
 * @author Flemming N. Larsen
 */
public interface IBot {

	/** Returns the bot id */
	int getId();

	/** Returns the energy level */
	double getEnergy();

	/** Returns the position */
	Point getPosition();

	/** Returns the driving direction in degrees */
	double getDirection();

	/** Returns the gun direction in degrees */
	double getGunDirection();

	/** Returns the radar direction in degrees */
	double getRadarDirection();

	/** Returns the speed */
	double getSpeed();

	/** Returns the gun heat */
	double getGunHeat();

	/** Returns the scan field */
	ScanField getScanField();

	/** Returns the score record */
	IScore getScore();

	/**
	 * Checks if the bot is alive.
	 * 
	 * @return true if the bot is alive; false otherwise.
	 */
	default boolean isAlive() {
		return getEnergy() >= 0;
	}

	/**
	 * Checks if the bot is dead.
	 *
	 * @return true if the bot is dead; false otherwise.
	 */
	default boolean isDead() {
		return !isAlive();
	}

	/**
	 * Checks if the bot is disabled, i.e. when the energy level is 0.
	 * 
	 * @return true if the bot is disabled; false otherwise.
	 */
	default boolean isDisabled() {
		return isAlive() && MathUtil.nearlyEqual(getEnergy(), 0);
	}
}
