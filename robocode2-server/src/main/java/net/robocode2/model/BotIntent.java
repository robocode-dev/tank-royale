package net.robocode2.model;

import lombok.Builder;
import lombok.Value;

/**
 * Mutable bot intent. A bot intent is updated by a bot between turns. The bot
 * intent reflects the bot's wiches/orders for new target speed, turn rates,
 * bullet power etc.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder
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
	 * Returns a bot intent that is updated by another bot intent. The bot intent is
	 * updates with new orders for target speed, turn rates and bullet power.
	 * 
	 * @param botIntent
	 *            is the adjustments for this intent. Fields that are null are
	 *            ignored, meaning that the corresponding fields on this intent are
	 *            left unchanged.
	 * @return An updated bot intent
	 */
	public BotIntent update(BotIntent botIntent) {
		BotIntentBuilder builder = builder();

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
	 * Returns a nullified version of this bot intent, where all {@code null} values have been replaced with a 0.
	 */
	public BotIntent nullify() {
		return builder()
			.targetSpeed(targetSpeed == null ? 0 : targetSpeed)
			.turnRate(turnRate == null ? 0 : turnRate)
			.gunTurnRate(gunTurnRate == null ? 0 : gunTurnRate)
			.radarTurnRate(radarTurnRate == null ? 0 : radarTurnRate)
			.bulletPower(bulletPower == null ? 0 : bulletPower).build();
	}
}