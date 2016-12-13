package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BotStateWithId;
import net.robocode2.model.Bot;

public final class BotToBotStateWithIdMapper {

	public static BotStateWithId map(Bot bot) {
		BotStateWithId botState = new BotStateWithId();
		botState.setId(bot.getId());
		botState.setEnergy(bot.getEnergy());
		botState.setPosition(PositionMapper.map(bot.getPosition()));
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(bot.getDirection());
		botState.setRadarDirection(bot.getRadarDirection());
		botState.setGunDirection(bot.getGunDirection());
		botState.setScanArc(ArcMapper.map(bot.getScanArc()));
		return botState;
	}
}