package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.Bot;
import dev.robocode.tankroyale.server.model.Round;
import dev.robocode.tankroyale.server.model.Turn;
import dev.robocode.tankroyale.schema.TickEventForBot;

public final class TurnToGameTickForBotMapper {

	private TurnToGameTickForBotMapper() {}

	public static TickEventForBot map(Round round, Turn turn, int botId) {
		Bot bot = turn.getBot(botId);
		if (bot == null) {
			return null;
		}
		TickEventForBot tick = new TickEventForBot();
		tick.set$type(TickEventForBot.$type.TICK_EVENT_FOR_BOT);
		tick.setRoundNumber(round.getRoundNumber());
		tick.setTurnNumber(turn.getTurnNumber());
		tick.setBotState(BotToBotStateMapper.map(bot));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		
		return tick;
	}
}