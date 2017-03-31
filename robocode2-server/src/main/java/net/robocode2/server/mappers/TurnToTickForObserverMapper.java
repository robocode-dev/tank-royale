package net.robocode2.server.mappers;

import net.robocode2.json_schema.messages.TickForObserver;
import net.robocode2.model.IRound;
import net.robocode2.model.ITurn;

public final class TurnToTickForObserverMapper {

	public static TickForObserver map(IRound round, ITurn turn) {
		TickForObserver tick = new TickForObserver();
		tick.setType(TickForObserver.Type.TICK_FOR_OBSERVER);
		tick.setBotStates(BotsToBotsWithIdMapper.map(turn.getBots()));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets()));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getObserverEvents()));
		return tick;
	}
}