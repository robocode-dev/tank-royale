package dev.robocode.tankroyale.server.model;

import lombok.Builder;
import lombok.Value;

/**
 * Mutable bot intent. A bot intent is updated by a bot between turns. The bot intent reflects the bot's wiches/orders
 * for new target speed, turn rates, bullet power etc.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public class BotIntent {

	/** Desired speed */
	Double targetSpeed;

	/** Desired driving turn rate */
	Double turnRate;

	/** Desired gun turn rate */
	Double gunTurnRate;
	
	/** Desired radar turn rate */
	Double radarTurnRate;

	/** Desired bullet power */
	Double bulletPower;

	/**
	 * Updates and returns this intent with new orders for target speed, turn rates and bullet power.
	 * 
	 * @param botIntent
	 *            is the adjustments for this intent. Fields that are null are ignored, meaning that the corresponding
	 *            fields on this intent are left unchanged.
	 */
	public BotIntent update(BotIntent botIntent) {
		BotIntentBuilder builder = toBuilder();
		if (botIntent.targetSpeed != null) {
			builder.targetSpeed(botIntent.targetSpeed);
		}
		if (botIntent.turnRate != null) {
			builder.turnRate(botIntent.turnRate);
		}
		if (botIntent.gunTurnRate != null) {
			builder.gunTurnRate(botIntent.gunTurnRate);
		}
		if (botIntent.radarTurnRate != null) {
			builder.radarTurnRate(botIntent.radarTurnRate);
		}
		if (botIntent.bulletPower != null) {
			builder.bulletPower(botIntent.bulletPower);
		}
		return builder.build();
	}

	/**
	 * Returns a zerofied version of this bot intent where all null field have been changed into zeros.
	 */
	public BotIntent zerofied() {
		BotIntentBuilder builder = BotIntent.builder();
		builder.targetSpeed(targetSpeed == null ? 0 : targetSpeed);
		builder.turnRate(turnRate == null ? 0 : turnRate);
		builder.gunTurnRate(gunTurnRate == null ? 0 : gunTurnRate);
		builder.radarTurnRate(radarTurnRate == null ? 0 : radarTurnRate);
		builder.bulletPower(bulletPower == null ? 0 : bulletPower);
		return builder.build();
	}
}