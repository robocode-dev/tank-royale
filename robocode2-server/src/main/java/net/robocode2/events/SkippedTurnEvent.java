package net.robocode2.events;

import lombok.Value;

/**
 * Event sent when a bot has skipped a turn
 * 
 * @author Flemming N. Larsen
 */
@Value
public class SkippedTurnEvent implements Event {

	/** Turn the got skipped */
	int skippedTurn;
}