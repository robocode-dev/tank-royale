package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.robocode2.model.events.IEvent;

/**
 * Turn interface
 * 
 * @author Flemming N. Larsen
 */
public interface ITurn {

	/** Returns the turn number */
	int getTurnNumber();

	/** Returns the bots */
	Set<IBot> getBots();

	/** Returns the bullets */
	Set<Bullet> getBullets();

	/** Returns the observer events */
	Set<IEvent> getObserverEvents();

	/** Returns map over bot events */
	Map<Integer, Set<IEvent>> getBotEventsMap();

	/**
	 * Returns a bot instance.
	 * 
	 * @param botId
	 *            is the id of the bot
	 * @return a bot instance
	 */
	default IBot getBot(int botId) {
		return getBots().stream().filter(b -> b.getId() == botId).findAny().orElse(null);
	}

	/**
	 * Returns the bullets fired by a specific bot
	 * 
	 * @param botId
	 *            is the id of the bot that fired the bullets
	 * @return a set of bullets
	 */
	default Set<Bullet> getBullets(int botId) {
		return getBullets().stream().filter(b -> b.getBotId() == botId).collect(Collectors.toSet());
	}

	/**
	 * Returns the event for a specific bot
	 * 
	 * @param botId
	 *            is the id of the bot
	 * @return a set of bot events
	 */
	default Set<IEvent> getBotEvents(int botId) {
		Set<IEvent> botEvents = getBotEventsMap().get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
		}
		return Collections.unmodifiableSet(botEvents);
	}
}
