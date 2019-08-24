package dev.robocode.tankroyale.server.events;

import lombok.Value;

/**
 * Event sent when a bot has been killed.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BotDeathEvent implements Event {

	/** Turn number when event occurred */
	int turnNumber;

	/** Bot id of the victim that got killed */
	int victimId;
}