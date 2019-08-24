
package dev.robocode.tankroyale.server.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.robocode.tankroyale.server.events.Event;
import lombok.Builder;
import lombok.Value;

/**
 * State of a game turn in a round.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder
public class Turn {

	/** Turn number */
	int turnNumber;

	/** Bots */
	Set<Bot> bots;

	/** Bullets */
	Set<Bullet> bullets;

	/** Observer events */
	Set<Event> observerEvents;

	/** Map over bot events */
	Map<Integer, Set<Event>> botEventsMap;


	/**
	 * Returns a bot instance.
	 * 
	 * @param botId
	 *            is the id of the bot
	 * @return a bot instance
	 */
	public Bot getBot(int botId) {
		return bots.stream().filter(b -> b.getId() == botId).findAny().orElse(null);
	}

	/**
	 * Returns the bullets fired by a specific bot
	 * 
	 * @param botId
	 *            is the id of the bot that fired the bullets
	 * @return a set of bullets
	 */
	public Set<Bullet> getBullets(int botId) {
		return bullets.stream().filter(b -> b.getBotId() == botId).collect(Collectors.toSet());
	}

	/**
	 * Returns the event for a specific bot
	 * 
	 * @param botId
	 *            is the id of the bot
	 * @return a set of bot events
	 */
	public Set<Event> getBotEvents(int botId) {
		Set<Event> botEvents = botEventsMap.get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
		}
		return Collections.unmodifiableSet(botEvents);
	}

	public static final class TurnBuilder {

		public TurnBuilder() {
			bots = new HashSet<>();
			bullets = new HashSet<>();
			observerEvents = new HashSet<>();
			botEventsMap = new HashMap<>();
		}
		
		/**
		 * Adds a observer event.
		 * 
		 * @param event
		 *            is the observer event
		 */
		public void addObserverEvent(Event event) {
			observerEvents.add(event);
		}

		/**
		 * Adds a private bot event
		 * 
		 * @param botId
		 *            is the bot id
		 * @param event
		 *            is the bot event, only given to the specified bot
		 */
		public void addPrivateBotEvent(int botId, Event event) {
			// Only a specific bot retrieves the event, not any other bot
			Set<Event> botEvents = botEventsMap.get(botId);
			if (botEvents == null) {
				botEvents = new HashSet<>();
			}
			botEvents.add(event);
			botEventsMap.put(botId, botEvents);
		}

		/**
		 * Adds a public bot event
		 * 
		 * @param event
		 *            is the bot event
		 */
		public void addPublicBotEvent(Event event) {
			// Every bots get notified about the bot event
			for (Bot bot : bots) {
				addPrivateBotEvent(bot.getId(), event);
			}
		}

		/**
		 * Reset all events
		 */
		public void resetEvents() {
			botEventsMap.clear();
			observerEvents.clear();
		}
		
		public Turn build() {
			return new Turn(
				turnNumber,
				new HashSet<>(bots),
				new HashSet<>(bullets),
				new HashSet<>(observerEvents),
				new HashMap<>(botEventsMap));
		}
	}
}