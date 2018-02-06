package net.robocode2.model.events;

import lombok.Value;

/**
 * Event sent when a bot has been killed.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BotDeathEvent implements Event {

	/** Bot id of the victim that got killed */
	int victimId;
}