package net.robocode2.model.mappers;

import net.robocode2.model.ImmutableBotIntent;

public final class BotIntentToBotIntentMapper {

	public static ImmutableBotIntent map(net.robocode2.json_schema.comm.BotIntent intent) {
		return new ImmutableBotIntent(intent.getTargetSpeed(), intent.getTurnRate(), intent.getGunTurnRate(),
				intent.getRadarTurnRate(), intent.getBulletPower());
	}
}
