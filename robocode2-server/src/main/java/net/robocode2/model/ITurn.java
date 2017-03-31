package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.robocode2.model.events.Event;

public interface ITurn {

	int getTurnNumber();

	Set<IBot> getBots();

	Set<IBullet> getBullets();

	Set<Event> getObserverEvents();

	Map<Integer, Set<Event>> getBotEventsMap();

	default Optional<IBot> getBot(int botId) {
		return getBots().stream().filter(b -> b.getId() == botId).findAny();
	}

	default Set<IBullet> getBullets(int botId) {
		return getBullets().stream().filter(b -> b.getBotId() == botId).collect(Collectors.toSet());
	}

	default Set<Event> getBotEvents(int botId) {
		Set<Event> botEvents = getBotEventsMap().get(botId);
		if (botEvents == null) {
			botEvents = new HashSet<>();
		}
		return Collections.unmodifiableSet(botEvents);
	}
}
