package net.robocode2.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.robocode2.model.events.Event;

public final class ImmutableTurn implements ITurn {

	private final int turnNumber;
	private final Set<IBot> bots;
	private final Set<IBullet> bullets;
	private final Set<Event> observerEvents;
	private final Map<Integer, Set<Event>> botEventsMap;

	public ImmutableTurn(int turnNumber, Set<IBot> bots, Set<IBullet> bullets, Set<Event> observerEvents,
			Map<Integer, Set<Event>> botEventsMap) {

		this.turnNumber = turnNumber;

		Set<ImmutableBot> immuBots = new HashSet<>();
		if (bots != null) {
			for (IBot bot : bots) {
				immuBots.add(new ImmutableBot(bot));
			}
		}
		this.bots = Collections.unmodifiableSet(immuBots);

		Set<ImmutableBullet> immuBullets = new HashSet<>();
		if (bullets != null) {
			for (IBullet bullet : bullets) {
				immuBullets.add(new ImmutableBullet(bullet));
			}
		}
		this.bullets = Collections.unmodifiableSet(immuBullets);

		Set<Event> immuObserverEvents = new HashSet<>();
		if (observerEvents != null) {
			immuObserverEvents.addAll(observerEvents);
		}
		this.observerEvents = Collections.unmodifiableSet(immuObserverEvents);

		Map<Integer, Set<Event>> immuBotEventsMap = new HashMap<>();
		if (botEventsMap != null) {
			immuBotEventsMap.putAll(botEventsMap);
		}
		this.botEventsMap = Collections.unmodifiableMap(immuBotEventsMap);
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
}