package net.robocode2.server.mappers;

import net.robocode2.model.ImmutableBotIntent;

public final class BotIntentToBotIntentMapper {

	public static ImmutableBotIntent map(net.robocode2.json_schema.messages.BotIntent intent) {
		return new ImmutableBotIntent(intent.getTargetSpeed(), intent.getTurnRate(), intent.getGunTurnRate(),
				intent.getRadarTurnRate(), intent.getBulletPower());
	}
}
