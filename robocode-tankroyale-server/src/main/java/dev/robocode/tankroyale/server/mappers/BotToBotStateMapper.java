package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.Bot;
import dev.robocode.tankroyale.server.util.MathUtil;
import dev.robocode.tankroyale.schema.BotState;

@SuppressWarnings("WeakerAccess")
public final class BotToBotStateMapper {

	private BotToBotStateMapper() {}

	public static BotState map(Bot bot) {
		BotState botState = new BotState();
		botState.setEnergy(bot.getEnergy());
		botState.setX(bot.getX());
		botState.setY(bot.getY());
		botState.setSpeed(bot.getSpeed());
		botState.setTurnRate(bot.getTurnRate());
		botState.setGunTurnRate(bot.getGunTurnRate());
		botState.setRadarTurnRate(bot.getRadarTurnRate());
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