package net.robocode2.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.robocode2.model.events.Event;

public final class Turn {

	private final int turnNumber;
	private final Set<Bot> bots;
	private final Set<Bullet> bullets;
	private final Set<Event> observerEvents;
	private final Map<Integer, Set<Event>> botEventsMap;

	public Turn(int turnNumber, Set<Bot> bots, Set<Bullet> bullets, Set<Event> observerEvents,
			Map<Integer, Set<Event>> botEventsMap) {

		this.turnNumber = turnNumber;
		if (bots == null) {
			this.bots = new HashSet<>();
		} else {
			this.bots = new HashSet<>(bots);
		}
		if (bullets == null) {
			this.bullets = new HashSet<>();
		} else {
			this.bullets = new HashSet<>(bullets);
		}
		if (observerEvents == null) {
			this.observerEvents = new HashSet<>();
		} else {
			this.observerEvents = new HashSet<>(observerEvents);
		}
		if (botEventsMap == null) {
			this.botEventsMap = new HashMap<>();
		} else {
			this.botEventsMap = new HashMap<>(botEventsMap);
		}
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public Set<Bot> getBots() {
		return Collections.unmodifiableSet(bots);
	}

	public Bot getBot(int botId) {
		return bots.stream().filter(b -> b.getId() == botId).findAny().get();
	}

	public Set<Bullet> getBullets() {
		return Collections.unmodifiableSet(bullets);
	}

	public Set<Bullet> getBullets(int botId) {
		return bullets.stream().filter(b -> b.getBotId() == botId).collect(Collectors.toSet());
	}

	public Set<Event> getObserverEvents() {
		return Collections.unmodifiableSet(observerEvents);
	}

	public Set<Event> getBotEvents(int botId) {
		return Collections.unmodifiableSet(botEventsMap.get(botId));
	}

	public static final class Builder {
		private int turnNumber;
		private Set<Bot> bots = new HashSet<>();
		private Set<Bullet> bullets = new HashSet<>();
		private Set<Event> observerEvents = new HashSet<>();
		private Map<Integer, Set<Event>> botEventsMap = new HashMap<>();

		public Turn build() {
			return new Turn(turnNumber, bots, bullets, observerEvents, botEventsMap);
		}

		public Builder setTurnNumber(int turnNumber) {
			this.turnNumber = turnNumber;
			return this;
		}

		public Builder setBots(Set<Bot> bots) {
			if (bots == null) {
				this.bots = new HashSet<>();
			} else {
				this.bots = new HashSet<>(bots);
			}
			return this;
		}

		public Builder setBullets(Set<Bullet> bullets) {
			if (bullets == null) {
				this.bullets = new HashSet<>();
			} else {
				this.bullets = new HashSet<>(bullets);
			}
			return this;
		}

		public Builder addBullet(Bullet bullet) {
			bullets.add(bullet);
			return this;
		}

		public Builder addObserverEvent(Event event) {
			observerEvents.add(event);
			return this;
		}

		public Builder addPrivateBotEvent(int botId, Event event) {
			Set<Event> botEvents = botEventsMap.get(botId);
			if (botEvents == null) {
				botEvents = new HashSet<>();
				botEventsMap.put(botId, botEvents);
			}
			botEvents.add(event);
			return this;
		}

		public Builder addPublicBotEvent(Event event) {
			for (Bot bot : bots) {
				addPrivateBotEvent(bot.getId(), event);
			}
			return this;
		}
	}
}