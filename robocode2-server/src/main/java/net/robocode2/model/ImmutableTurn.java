package net.robocode2.model;

import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Value;
import net.robocode2.model.events.IEvent;

/**
 * Immutable turn instance
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder
public final class ImmutableTurn implements ITurn {

	/** Turn number */
	int turnNumber;

	/** Bots */
	Set<IBot> bots;

	/** Bullets */
	Set<Bullet> bullets;

	/** Observer events */
	Set<IEvent> observerEvents;

	/** Map over bot events */
	Map<Integer, Set<IEvent>> botEventsMap;
}