package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BotState;
import net.robocode2.model.Bot;

public final class BotToBotStateMapper {

	public static BotState map(Bot bot) {
		BotState botState = new BotState();

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