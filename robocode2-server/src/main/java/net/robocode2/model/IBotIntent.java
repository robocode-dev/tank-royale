package net.robocode2.model;

/**
 * Interface for a bot intent. A bot intent is updated by a bot between turns. The bot intent reflects the bot's
 * wiches/orders for new target speed, turn rates, bullet power etc.
 * 
 * @author Flemming N. Larsen
 */
public interface IBotIntent {

	/** Returns desired target speed */
	Double getTargetSpeed();

	/** Returns desired driving turn rate */
	Double getDrivingTurnRate();

	/** Returns desired gun turn rate */
	Double getGunTurnRate();

	/** Returns desired radar turn rate */
	Double getRadarTurnRate();

	/** Returns desired bullet power */
	Double getBulletPower();
}
