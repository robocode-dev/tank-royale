package net.robocode2.model;

import java.util.Optional;
import java.util.Set;

import net.robocode2.model.events.Event;

public interface ITurn {

	int getTurnNumber();

	Set<IBot> getBots();

	Optional<IBot> getBot(int botId);

	Set<IBullet> getBullets();

	Set<IBullet> getBullets(int botId);

	Set<Event> getObserverEvents();

	Set<Event> getBotEvents(int botId);
}
