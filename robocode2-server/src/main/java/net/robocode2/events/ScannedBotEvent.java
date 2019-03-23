package net.robocode2.events;

import lombok.Value;
import net.robocode2.model.Point;

/**
 * Event sent when a bot got scanned.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class ScannedBotEvent implements Event {

	/** Bot id of the bot that scanned the bot */
	int scannedByBotId;

	/** Bot id of the bot that got scanned */
	int scannedBotId;
	
	/** Energy level of the scanned bot */
	double energy;
	
	/** X coordinate of the scanned bot */
	double x;

	/** Y coordinate of the scanned bot */
	double y;

	/** Driving direction of the scanned bot */
	double direction;
	
	/** Speed of the scanned bot */
	double speed;
}