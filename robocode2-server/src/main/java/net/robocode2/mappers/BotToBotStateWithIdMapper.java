package net.robocode2.mappers;

import net.robocode2.schema.BotStateWithId;
import net.robocode2.model.Bot;
import net.robocode2.util.MathUtil;

public final class BotToBotStateWithIdMapper {

	private BotToBotStateWithIdMapper() {}

	public static BotStateWithId map(Bot bot) {
		BotStateWithId botState = new BotStateWithId();
		botState.setId(bot.getId());
		botState.setEnergy(bot.getEnergy());
		botState.setX(bot.getX());
		botState.setY(bot.getY());
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(MathUtil.normalAbsoluteDegrees(bot.getDirection()));
		botState.setRadarDirection(MathUtil.normalAbsoluteDegrees(bot.getRadarDirection()));
		botState.setRadarSweep(MathUtil.normalAbsoluteDegrees(bot.getRadarSpreadAngle()));
		botState.setGunDirection(MathUtil.normalAbsoluteDegrees(bot.getGunDirection()));
		return botState;
	}
}