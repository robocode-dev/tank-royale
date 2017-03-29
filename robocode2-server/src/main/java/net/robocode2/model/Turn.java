package net.robocode2.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.robocode2.model.events.Event;

public final class Turn implements ITurn {

	private int turnNumber;
	private Set<IBot> bots = new HashSet<>();
	private Set<IBullet> bullets = new HashSet<>();
	private Set<Event> observerEvents = new HashSet<>();
	private Map<Integer, Set<Event>> botEventsMap = new HashMap<>();

	public ImmutableTurn toImmutableTurn() {
		return new ImmutableTurn(turnNumber, bots, bullets, observerEvents, botEventsMap);
	}

	@Override
	public int getTurnNumber() {
		return turnNumber;
	}

	@Override
	public Set<IBot> getBots() {
		return bots;
	}

	@Override
	public Optional<IBot> getBot(int botId) {
		return bots.stream().filter(b -> b.getId() == botId).findAny();
	}

	@Override
	public Set<IBullet> getBullets() {
		return bullets;
	}

	@Override
	public Set<IBullet> getBullets(int botId) {
		return bullets.stream().filter(b -> b.getBotId() == botId).collect(Collectors.toSet());
	}

	@Override
	public Set<Event> getObserverEvents() {
		return observerEvents;
	}

	@Override
	public Set<Event> getBotEvents(int botId) {
		Set<Event> botEvents = botEventsMap.get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
		}
		return Collections.unmodifiableSet(botEvents);
	}

	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}

	public void setBots(Set<ImmutableBot> bots) {
		if (bots == null) {
			this.bots = new HashSet<>();
		} else {
			this.bots = new HashSet<>(bots);
		}
	}

	public void setBullets(Set<ImmutableBullet> bullets) {
		if (bullets == null) {
			this.bullets = new HashSet<>();
		} else {
			this.bullets = new HashSet<>(bullets);
		}
	}

	public void addObserverEvent(Event event) {
		observerEvents.add(event);
	}

	public void addPrivateBotEvent(int botId, Event event) {
		Set<Event> botEvents = botEventsMap.get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
			botEventsMap.put(botId, botEvents);
		}
		botEvents.add(event);
	}

	public void addPublicBotEvent(Event event) {
		for (IBot bot : bots) {
			addPrivateBotEvent(bot.getId(), event);
		}
	}

	public void resetEvents() {
		botEventsMap.clear();
		observerEvents.clear();
	}
}