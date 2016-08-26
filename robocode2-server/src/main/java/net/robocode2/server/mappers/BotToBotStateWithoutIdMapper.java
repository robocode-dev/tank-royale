package net.robocode2.server.mappers;

import net.robocode2.json_schema.BotStateWithoutId;
import net.robocode2.model.Bot;

public class BotToBotStateWithoutIdMapper {

	public static BotStateWithoutId map(Bot bot) {
		BotStateWithoutId botState = new BotStateWithoutId();

		botState.setEnergy(bot.getEnergy());
		botState.setPosition(TypesMapper.map(bot.getPosition()));
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(bot.getDirection());
		botState.setRadarDirection(bot.getRadarDirection());
		botState.setTurretDirection(bot.getTurretDirection());
		botState.setArc(TypesMapper.map(bot.getScanArc()));

		return botState;
	}
}