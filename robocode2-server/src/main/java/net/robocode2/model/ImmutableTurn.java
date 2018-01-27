package net.robocode2.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Value;
import net.robocode2.model.events.IEvent;

/**
 * Immutable turn instance
 * 
 * @author Flemming N. Larsen
 */
@Value
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

	/**
	 * Creates a immutable turn based on another turn.
	 * 
	 * @param bot
	 *            is the turn that is deep copied into this turn.
	 */
	public ImmutableTurn(ITurn turn) {
		turnNumber = turn.getTurnNumber();

		Set<ImmutableBot> immuBots = new HashSet<>();
		Set<IBot> bots = turn.getBots();
		if (bots != null) {
			for (IBot bot : bots) {
				immuBots.add(new ImmutableBot(bot));
			}
		}
		this.bots = Collections.unmodifiableSet(immuBots);

		this.bullets = turn.getBullets();

		Set<IEvent> immuObserverEvents = new HashSet<>();
		Set<IEvent> observerEvents = turn.getObserverEvents();
		if (observerEvents != null) {
			immuObserverEvents.addAll(observerEvents);
		}
		this.observerEvents = Collections.unmodifiableSet(immuObserverEvents);

		Map<Integer, Set<IEvent>> immuBotEventsMap = new HashMap<>();
		Map<Integer, Set<IEvent>> botEventsMap = turn.getBotEventsMap();
		if (botEventsMap != null) {
			immuBotEventsMap.putAll(botEventsMap);
		}
		this.botEventsMap = Collections.unmodifiableMap(immuBotEventsMap);
	}
}