package net.robocode2.model.mappers;

import net.robocode2.json_schema.events.TickEventForBot;
import net.robocode2.model.IBot;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;

public final class TurnToGameTickForBotMapper {

	public static TickEventForBot map(Round round, Turn turn, int botId) {
		IBot bot = turn.getBot(botId);
		if (bot == null) {
			return null;
		}
		TickEventForBot tick = new TickEventForBot();
		tick.setType(TickEventForBot.Type.TICK_EVENT_FOR_BOT);
		tick.setBotState(BotToBotStateMapper.map(bot));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		
		return tick;
	}
}