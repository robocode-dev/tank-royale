package net.robocode2.server.mappers;

import net.robocode2.json_schema.TickForBot;
import net.robocode2.model.Bot;
import net.robocode2.model.Turn;

public class TurnToTickForBotMapper {

	public static TickForBot map(Turn turn, int botId) {
		Bot bot = turn.getBot(botId);

		TickForBot tickForBot = new TickForBot();
		tickForBot.setMessageType(TickForBot.MessageType.TICK_FOR_BOT);

		tickForBot.setBotState(BotToBotStateWithoutIdMapper.map(bot));
		tickForBot.setBulletStates(BulletsToBulletStatesMapper.map(turn, botId));

		// tickForBot.setRoundState(roundState); // TODO
		// tickForBot.setScannedBots(scannedBots); // TODO

		return null; // TODO
	}
}