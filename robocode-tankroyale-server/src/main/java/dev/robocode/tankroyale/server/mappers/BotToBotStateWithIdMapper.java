package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.Bot;
import dev.robocode.tankroyale.server.util.MathUtil;
import dev.robocode.tankroyale.schema.BotStateWithId;

@SuppressWarnings("WeakerAccess")
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
		botState.setGunDirection(MathUtil.normalAbsoluteDegrees(bot.getGunDirection()));
		botState.setRadarDirection(MathUtil.normalAbsoluteDegrees(bot.getRadarDirection()));
		botState.setRadarSweep(bot.getRadarSpreadAngle());
		botState.setGunHeat(bot.getGunHeat());
		botState.setBodyColor(bot.getBodyColor());
		botState.setTurretColor(bot.getTurretColor());
		botState.setRadarColor(bot.getRadarColor());
		botState.setBulletColor(bot.getBulletColor());
		botState.setScanColor(bot.getScanColor());
		botState.setTracksColor(bot.getTracksColor());
		botState.setGunColor(bot.getGunColor());
		return botState;
	}
}