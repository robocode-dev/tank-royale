package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.Round;
import dev.robocode.tankroyale.server.model.Turn;
import dev.robocode.tankroyale.schema.TickEventForObserver;

public final class TurnToTickEventForObserverMapper {

	private TurnToTickEventForObserverMapper() {}

	public static TickEventForObserver map(Round round, Turn turn) {
		TickEventForObserver tick = new TickEventForObserver();
		tick.set$type(TickEventForObserver.$type.TICK_EVENT_FOR_OBSERVER);
		tick.setRoundNumber(round.getRoundNumber());
		tick.setTurnNumber(turn.getTurnNumber());
		tick.setBotStates(BotsToBotsWithIdMapper.map(turn.getBots()));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets()));
		tick.setEvents(EventsToEventsMapper.map(turn.getObserverEvents()));
		return tick;
	}
}