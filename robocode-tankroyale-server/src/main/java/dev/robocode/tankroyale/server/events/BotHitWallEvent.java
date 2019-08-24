package dev.robocode.tankroyale.server.events;

import lombok.Value;

/**
 * Event sent when a bot has hit a wall.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BotHitWallEvent implements Event {

	/** Turn number when event occurred */
	int turnNumber;

	/** Bot id of the victim that has hit a wall */
	int victimId;
}