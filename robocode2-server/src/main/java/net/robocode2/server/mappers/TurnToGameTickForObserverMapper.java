package net.robocode2.server.mappers;

import net.robocode2.json_schema.messages.GameTickForObserver;
import net.robocode2.model.IRound;
import net.robocode2.model.ITurn;

public final class TurnToGameTickForObserverMapper {

	public static GameTickForObserver map(IRound round, ITurn turn) {
		GameTickForObserver tick = new GameTickForObserver();
		tick.setType(GameTickForObserver.Type.GAME_TICK_FOR_OBSERVER);
		tick.setBotStates(BotsToBotsWithIdMapper.map(turn.getBots()));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets()));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getObserverEvents()));
		return tick;
	}
}