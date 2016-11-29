package net.robocode2.server.mappers;

import net.robocode2.json_schema.messages.TickForBot;
import net.robocode2.model.Bot;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;

public final class TurnToTickForBotMapper {

	public static TickForBot map(Round round, Turn turn, int botId) {
		Bot bot = turn.getBot(botId);

		TickForBot tickForBot = new TickForBot();
		tickForBot.setMessageType(TickForBot.MessageType.TICK_FOR_BOT);

		tickForBot.setBotState(BotToBotStateMapper.map(bot));
		tickForBot.setBulletStates(BulletsToBulletStatesMapper.map(turn, botId));
		tickForBot.setRoundState(RoundToRoundStateMapper.map(round, turn));
		// TODO: Map bot events

		return tickForBot;
	}
}