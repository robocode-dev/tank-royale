package net.robocode2.server.mappers;

import net.robocode2.json_schema.states.BotStateWithId;
import net.robocode2.model.IBot;
import net.robocode2.util.MathUtil;

public final class BotToBotStateWithIdMapper {

	public static BotStateWithId map(IBot bot) {
		BotStateWithId botState = new BotStateWithId();
		botState.setId(bot.getId());
		botState.setEnergy(bot.getEnergy());
		botState.setPosition(PointMapper.map(bot.getPosition()));
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(MathUtil.normalAbsoluteDegrees(bot.getDirection()));
		botState.setRadarDirection(MathUtil.normalAbsoluteDegrees(bot.getRadarDirection()));
		botState.setGunDirection(MathUtil.normalAbsoluteDegrees(bot.getGunDirection()));
		botState.setScanField(ScanFieldMapper.map(bot.getScanField()));
		return botState;
	}
}