package dev.robocode.tankroyale.server.events;

import lombok.Value;

/**
 * Event sent when a bot has skipped a turn.
 * The skipped turn is the turn number of the event.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class SkippedTurnEvent implements Event {

    /** Turn number when event occurred */
    int turnNumber;
}