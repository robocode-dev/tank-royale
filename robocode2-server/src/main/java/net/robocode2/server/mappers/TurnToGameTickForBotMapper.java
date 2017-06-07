package net.robocode2.server.mappers;

import java.util.Optional;

import net.robocode2.json_schema.messages.GameTickForBot;
import net.robocode2.model.IBot;
import net.robocode2.model.IRound;
import net.robocode2.model.ITurn;

public final class TurnToGameTickForBotMapper {

	public static GameTickForBot map(IRound round, ITurn turn, int botId) {
		Optional<IBot> optionalBot = turn.getBot(botId);
		if (!optionalBot.isPresent()) {
			return null;
		}
		GameTickForBot tick = new GameTickForBot();
		tick.setType(GameTickForBot.Type.GAME_TICK_FOR_BOT);
		tick.setBotState(BotToBotStateMapper.map(optionalBot.get()));
		tick.setBulletStates(BulletsToBulletStatesMapper.map(turn.getBullets(botId)));
		tick.setRoundState(RoundToRoundStateMapper.map(round, turn));
		tick.setEvents(EventsToEventsMapper.map(turn.getBotEvents(botId)));
		return tick;
	}
}