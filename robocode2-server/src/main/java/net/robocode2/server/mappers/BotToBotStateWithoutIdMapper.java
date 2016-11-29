package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BotStateWithoutId;
import net.robocode2.model.Bot;

public final class BotToBotStateWithoutIdMapper {

	public static BotStateWithoutId map(Bot bot) {
		BotStateWithoutId botState = new BotStateWithoutId();

		botState.setEnergy(bot.getEnergy());
		botState.setPosition(PositionMapper.map(bot.getPosition()));
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(bot.getDirection());
		botState.setRadarDirection(bot.getRadarDirection());
		botState.setTurretDirection(bot.getTurretDirection());
		botState.setScanArc(ArcMapper.map(bot.getScanArc()));

		return botState;
	}
}