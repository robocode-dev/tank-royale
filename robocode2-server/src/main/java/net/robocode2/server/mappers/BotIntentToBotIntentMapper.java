package net.robocode2.server.mappers;

import net.robocode2.model.BotIntent;

public final class BotIntentToBotIntentMapper {

	public static BotIntent map(net.robocode2.json_schema.messages.BotIntent intent) {
		return new BotIntent(intent.getTargetSpeed(), intent.getTurnRate(), intent.getGunTurnRate(),
				intent.getRadarTurnRate(), intent.getBulletPower());
	}
}
