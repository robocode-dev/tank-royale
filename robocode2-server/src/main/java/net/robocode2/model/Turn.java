package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.robocode2.model.events.Event;

public final class Turn {

	private final int turnNumber;
	private final Set<Bot> bots;
	private final Set<Bullet> bullets;
	private final Set<Event> events;

	public Turn(int turnNumber, Set<Bot> bots, Set<Bullet> bullets, Set<Event> events) {
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
		if (events == null) {
			this.events = new HashSet<>();
		} else {
			this.events = new HashSet<>(events);
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

	public Set<Event> getEvents() {
		return Collections.unmodifiableSet(events);
	}

	public static final class TurnBuilder {
		private int turnNumber;
		private Set<Bot> bots = new HashSet<>();
		private Set<Bullet> bullets = new HashSet<>();
		private Set<Event> events = new HashSet<>();

		public Turn build() {
			return new Turn(turnNumber, bots, bullets, events);
		}

		public TurnBuilder setTurnNumber(int turnNumber) {
			this.turnNumber = turnNumber;
			return this;
		}

		public TurnBuilder setBots(Set<Bot> bots) {
			if (bots == null) {
				this.bots = new HashSet<>();
			} else {
				this.bots = new HashSet<>(bots);
			}
			return this;
		}

		public TurnBuilder setBullets(Set<Bullet> bullets) {
			if (bullets == null) {
				this.bullets = new HashSet<>();
			} else {
				this.bullets = new HashSet<>(bullets);
			}
			return this;
		}

		public TurnBuilder addBullet(Bullet bullet) {
			bullets.add(bullet);
			return this;
		}

		public TurnBuilder addEvent(Event event) {
			events.add(event);
			return this;
		}
	}
}