package dev.robocode.tankroyale.server.mappers;

import dev.robocode.tankroyale.server.model.BotIntent;

public final class BotIntentToBotIntentMapper {

	private BotIntentToBotIntentMapper() {}

	public static BotIntent map(dev.robocode.tankroyale.schema.BotIntent intent) {

		return BotIntent.builder()
			.targetSpeed(intent.getTargetSpeed())
			.turnRate(intent.getTurnRate())
			.gunTurnRate(intent.getGunTurnRate())
			.radarTurnRate(intent.getRadarTurnRate())
			.bulletPower(intent.getFirepower())
			.adjustGunForBodyTurn(intent.getAdjustGunForBodyTurn())
			.adjustRadarForGunTurn(intent.getAdjustRadarForGunTurn())
            .bodyColor(intent.getBodyColor())
            .turretColor(intent.getTurretColor())
            .radarColor(intent.getRadarColor())
            .bulletColor(intent.getBulletColor())
            .scanColor(intent.getScanColor())
			.tracksColor(intent.getTracksColor())
			.gunColor(intent.getGunColor())
			.build();
	}
}
