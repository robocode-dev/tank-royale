package net.robocode2.server.mappers;

import java.util.Optional;

import net.robocode2.json_schema.messages.TickForBot;
import net.robocode2.model.ImmutableBot;
import net.robocode2.model.Round;
import net.robocode2.model.Turn;

public final class TurnToTickForBotMapper {

	public static TickForBot map(Round round, Turn turn, int botId) {
		Optional<ImmutableBot> optionalBot = turn.getBot(botId);
		if (!optionalBot.isPresent()) {
			return null;
		}
		TickForBot tick = new TickForBot();
		tick.setType(TickForBot.Type.TICK_FOR_BOT);
		tick.setBotState(BotToBotStateMapper.map(optionalBot.get()));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		return tick;
	}
}