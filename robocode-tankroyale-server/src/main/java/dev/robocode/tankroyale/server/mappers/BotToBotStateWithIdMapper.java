package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.Bot;
import dev.robocode.tankroyale.server.util.MathUtil;
import dev.robocode.tankroyale.schema.BotStateWithId;

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
		botState.setGunHeat(bot.getGunHeat());
		return botState;
	}
}