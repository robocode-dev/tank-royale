package net.robocode2.server.mappers;

import java.util.Optional;

import net.robocode2.json_schema.messages.TickForBot;
import net.robocode2.model.IBot;
import net.robocode2.model.IRound;
import net.robocode2.model.ITurn;

public final class TurnToTickForBotMapper {

	public static TickForBot map(IRound round, ITurn turn, int botId) {
		Optional<IBot> optionalBot = turn.getBot(botId);
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