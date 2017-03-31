package net.robocode2.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.robocode2.model.events.IEvent;

public final class Turn implements ITurn {

	private int turnNumber;
	private Set<IBot> bots = new HashSet<>();
	private Set<IBullet> bullets = new HashSet<>();
	private Set<IEvent> observerEvents = new HashSet<>();
	private Map<Integer, Set<IEvent>> botEventsMap = new HashMap<>();

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
	public Set<IBullet> getBullets() {
		return bullets;
	}

	@Override
	public Set<IEvent> getObserverEvents() {
		return observerEvents;
	}

	@Override
	public Map<Integer, Set<IEvent>> getBotEventsMap() {
		return botEventsMap;
	}

	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}

	public void setBots(Collection<Bot> bots) {
		this.bots = new HashSet<>();
		if (bots != null) {
			this.bots.addAll(bots);
		}
	}

	public void setBullets(Collection<Bullet> bullets) {
		this.bullets = new HashSet<>();
		if (bullets != null) {
			this.bullets.addAll(bullets);
		}
	}

	public void addObserverEvent(IEvent event) {
		observerEvents.add(event);
	}

	public void addPrivateBotEvent(int botId, IEvent event) {
		Set<IEvent> botEvents = botEventsMap.get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
			botEventsMap.put(botId, botEvents);
		}
		botEvents.add(event);
	}

	public void addPublicBotEvent(IEvent event) {
		for (IBot bot : bots) {
			addPrivateBotEvent(bot.getId(), event);
		}
	}

	public void resetEvents() {
		botEventsMap.clear();
		observerEvents.clear();
	}
}