package net.robocode2.model.mappers;

import java.util.Optional;

import net.robocode2.json_schema.events.TickEventForBot;
import net.robocode2.model.IBot;
import net.robocode2.model.IRound;
import net.robocode2.model.ITurn;

public final class TurnToGameTickForBotMapper {

	public static TickEventForBot map(IRound round, ITurn turn, int botId) {
		Optional<IBot> optionalBot = turn.getBot(botId);
		if (!optionalBot.isPresent()) {
			return null;
		}
		TickEventForBot tick = new TickEventForBot();
		tick.setType(TickEventForBot.Type.TICK_EVENT_FOR_BOT);
		tick.setBotState(BotToBotStateMapper.map(optionalBot.get()));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		return tick;
	}
}