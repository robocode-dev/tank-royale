package net.robocode2.model.events;

import lombok.Value;

/**
 * Event sent when a bot has hit a wall.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BotHitWallEvent implements Event {

	/** Bot id of the victim that has hit a wall */
	int victimId;
}