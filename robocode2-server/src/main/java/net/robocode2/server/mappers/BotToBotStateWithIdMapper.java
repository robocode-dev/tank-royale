package net.robocode2.server.mappers;

import net.robocode2.game.MathUtil;
import net.robocode2.json_schema.states.BotStateWithId;
import net.robocode2.model.ImmutableBot;

public final class BotToBotStateWithIdMapper {

	public static BotStateWithId map(ImmutableBot bot) {
		BotStateWithId botState = new BotStateWithId();
		botState.setId(bot.getId());
		botState.setEnergy(bot.getEnergy());
		botState.setPosition(PointMapper.map(bot.getPosition()));
		botState.setSpeed(bot.getSpeed());
		botState.setDirection(MathUtil.normalAbsoluteAngleDegrees(bot.getDirection()));
		botState.setRadarDirection(MathUtil.normalAbsoluteAngleDegrees(bot.getRadarDirection()));
		botState.setGunDirection(MathUtil.normalAbsoluteAngleDegrees(bot.getGunDirection()));
		botState.setScanArc(ArcMapper.map(bot.getScanArc()));
		return botState;
	}
}