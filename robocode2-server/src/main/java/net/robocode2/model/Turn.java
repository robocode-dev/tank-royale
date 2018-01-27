package net.robocode2.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.Singular;
import net.robocode2.model.events.IEvent;

/**
 * State of a game turn in a round.
 * 
 * @author Flemming N. Larsen
 */
@Data
public final class Turn implements ITurn {

	/** Turn number */
	private int turnNumber;
	/** Bots */
	private Set<IBot> bots = new HashSet<>();
	/** Bullets */
	@Singular private Set<Bullet> bullets = new HashSet<>();
	/** Observer events */
	private Set<IEvent> observerEvents = new HashSet<>();
	/** Map over bot events */
	private Map<Integer, Set<IEvent>> botEventsMap = new HashMap<>();

	/**
	 * Creates an immutable turn instance that is a deep copy of this turn.
	 * 
	 * @return an immutable turn instance
	 */
	public ImmutableTurn toImmutableTurn() {
		return new ImmutableTurn(this);
	}

	/**
	 * Adds a private bot event
	 * 
	 * @param botId
	 *            is the bot id
	 * @param event
	 *            is the bot event, only given to the specified bot
	 */
	public void addPrivateBotEvent(int botId, IEvent event) {
		// Only a specific bot retrieves the event, not any other bot
		Set<IEvent> botEvents = botEventsMap.get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
			botEventsMap.put(botId, botEvents);
		}
		botEvents.add(event);
	}

	/**
	 * Adds a public bot event
	 * 
	 * @param botId
	 *            is the bot id
	 * @param event
	 *            is the bot event
	 */
	public void addPublicBotEvent(IEvent event) {
		// Every bots get notified about the bot event
		for (IBot bot : bots) {
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
}