package net.robocode2.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import net.robocode2.model.events.IEvent;

/**
 * State of a game turn in a round.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public final class Turn {

	/** Turn number */
	int turnNumber;

	/** Bots */
	@Singular Set<IBot> bots;

	/** Bullets */
	@Singular Set<Bullet> bullets;

	/** Observer events */
	@Singular Set<IEvent> observerEvents;

	/** Map over bot events */
	@Singular("botEvent") Map<Integer, Set<IEvent>> botEventsMap;

	/**
	 * Returns a bot instance.
	 * 
	 * @param botId
	 *            is the id of the bot
	 * @return a bot instance
	 */
	public IBot getBot(int botId) {
		Optional<IBot> opt = bots.stream().filter(b -> b.getId() == botId).findAny();
		return opt.isPresent() ? opt.get() : null;
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
	public Set<IEvent> getBotEvents(int botId) {
		Set<IEvent> botEvents = getBotEventsMap().get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
		}
		return Collections.unmodifiableSet(botEvents);
	}

	/**
	 * Adds an observer event
	 * 
	 * @param event
	 *            is the observer event
	 */
	public void addObserverEvent(IEvent event) {
		observerEvents.add(event);
	}

	/**
	 * Adds a private bot event and returns the new set of bot events.
	 * 
	 * @param botId
	 *            is the bot id
	 * @param event
	 *            is the bot event to add, only given to the specified bot
	 */
	public Set<IEvent> addPrivateBotEvent(int botId, IEvent event) {
		// Only a specific bot retrieves the event, not any other bot
		Set<IEvent> currentBotEvents = botEventsMap.get(botId);
		Set<IEvent> newBotEvents = (currentBotEvents == null) ? new HashSet<>() : new HashSet<>(currentBotEvents);
		newBotEvents.add(event);
		return newBotEvents;
	}

	/**
	 * Adds a public bot event and returns the new bot events map.
	 * 
	 * @param event
	 *            is the bot event to add for every bot
	 */
	public Map<Integer, Set<IEvent>> addPublicBotEvent(IEvent event) {
		// Every bots get notified about the bot event
		Map<Integer, Set<IEvent>> newBotEventsMap = new HashMap<>(botEventsMap);
		for (IBot bot : bots) {
			newBotEventsMap.put(bot.getId(), addPrivateBotEvent(bot.getId(), event));
		}
		return newBotEventsMap;
	}
}