package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Event occurring when the bot has skipped a turn, meaning that no intent has reached the server
 * for a specific turn
 */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class SkippedTurnEvent extends GameEvent {}
