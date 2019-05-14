package net.robocode2.mappers;

import net.robocode2.schema.TickEventForBot;
import net.robocode2.model.Bot;
import net.robocode2.model.Turn;
import net.robocode2.model.Round;

public final class TurnToGameTickForBotMapper {

	private TurnToGameTickForBotMapper() {}

	public static TickEventForBot map(Round round, Turn turn, int botId) {
		Bot bot = turn.getBot(botId);
		if (bot == null) {
			return null;
		}
		TickEventForBot tick = new TickEventForBot();
		tick.setType(TickEventForBot.Type.TICK_EVENT_FOR_BOT);
		tick.setRoundNumber(round.getRoundNumber());
		tick.setTurnNumber(turn.getTurnNumber());
		tick.setBotState(BotToBotStateMapper.map(bot));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		
		return tick;
	}
}