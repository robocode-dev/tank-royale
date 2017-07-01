package net.robocode2.model;

/**
 * Mutable bot intent. A bot intent is updated by a bot between turns. The bot intent reflects the bot's wiches/orders
 * for new target speed, turn rates, bullet power etc.
 * 
 * @author Flemming N. Larsen
 */
public class BotIntent implements IBotIntent {

	/** Desired speed */
	private Double targetSpeed;
	/** Desired driving turn rate */
	private Double drivingTurnRate;
	/** Desired gun turn rate */
	private Double gunTurnRate;
	/** Desired radar turn rate */
	private Double radarTurnRate;
	/** Desired bullet power */
	private Double bulletPower;

	/**
	 * Updates this intent with new orders for target speed, turn rates and bullet power.
	 * 
	 * @param botIntent
	 *            is the adjustments for this intent. Fields that are null are ignored, meaning that the corresponding
	 *            fields on this intent are left unchanged.
	 */
	public void update(IBotIntent botIntent) {
		if (botIntent.getTargetSpeed() != null) {
			targetSpeed = botIntent.getTargetSpeed();
		}
		if (botIntent.getDrivingTurnRate() != null) {
			drivingTurnRate = botIntent.getDrivingTurnRate();
		}
		if (botIntent.getGunTurnRate() != null) {
			gunTurnRate = botIntent.getGunTurnRate();
		}
		if (botIntent.getRadarTurnRate() != null) {
			radarTurnRate = botIntent.getRadarTurnRate();
		}
		if (botIntent.getBulletPower() != null) {
			bulletPower = botIntent.getBulletPower();
		}
	}

	/**
	 * Returns an immutable bot intent that is a deep copy of this object.
	 * 
	 * @return an immutable bot intent
	 */
	public ImmutableBotIntent toImmutableBotIntent() {
		return new ImmutableBotIntent(this);
	}

	@Override
	public Double getTargetSpeed() {
		return targetSpeed;
	}

	@Override
	public Double getDrivingTurnRate() {
		return drivingTurnRate;
	}

	@Override
	public Double getGunTurnRate() {
		return gunTurnRate;
	}

	@Override
	public Double getRadarTurnRate() {
		return radarTurnRate;
	}

	@Override
	public Double getBulletPower() {
		return bulletPower;
	}
}