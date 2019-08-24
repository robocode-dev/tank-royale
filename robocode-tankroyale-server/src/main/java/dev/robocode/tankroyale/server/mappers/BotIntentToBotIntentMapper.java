package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.BotIntent;

public final class BotIntentToBotIntentMapper {

	private BotIntentToBotIntentMapper() {}

	public static BotIntent map(net.robocode2.schema.BotIntent intent) {

		return BotIntent.builder()
			.targetSpeed(intent.getTargetSpeed())
			.turnRate(intent.getTurnRate())
			.gunTurnRate(intent.getGunTurnRate())
			.radarTurnRate(intent.getRadarTurnRate())
			.bulletPower(intent.getFirepower())
			.build();
	}
}
