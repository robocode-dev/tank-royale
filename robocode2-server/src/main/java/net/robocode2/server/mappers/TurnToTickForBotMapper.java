package net.robocode2.server.mappers;

import net.robocode2.json_schema.messages.TickForBot;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;

public final class TurnToTickForBotMapper {

	public static TickForBot map(Round round, Turn turn, int botId) {
		TickForBot tick = new TickForBot();
		tick.setMessageType(TickForBot.MessageType.TICK_FOR_BOT);
		tick.setBotState(BotToBotStateMapper.map(turn.getBot(botId)));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		return tick;
	}
}