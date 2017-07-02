package net.robocode2.model;

/**
 * Immutable bot intent.
 * 
 * @author Flemming N. Larsen
 */
public final class ImmutableBotIntent implements IBotIntent {

	/** Desired target speed */
	private final Double targetSpeed;
	/** Desired driving turn rate */
	private final Double drivingTurnRate;
	/** Desired gun turn rate */
	private final Double gunTurnRate;
	/** Desired radar turn rate */
	private final Double radarTurnRate;
	/** Desired bullet power */
	private final Double bulletPower;

	/**
	 * Creates a immutable bot intent.
	 * 
	 * @param targetSpeed
	 *            is the desired target speed
	 * @param drivingTurnRate
	 *            is the desired driving turn rate
	 * @param gunTurnRate
	 *            is the desired gun turn rate
	 * @param radarTurnRate
	 *            is the desired radar turn rate
	 * @param bulletPower
	 *            is the desired bullet power
	 */
	public ImmutableBotIntent(Double targetSpeed, Double drivingTurnRate, Double gunTurnRate, Double radarTurnRate,
			Double bulletPower) {

		this.targetSpeed = targetSpeed;
		this.drivingTurnRate = drivingTurnRate;
		this.gunTurnRate = gunTurnRate;
		this.radarTurnRate = radarTurnRate;
		this.bulletPower = bulletPower;
	}

	/**
	 * Creates a immutable bot intent based on another bot intent.
	 * 
	 * @param bot
	 *            is the bot intent that is deep copied into this bot intent.
	 */
	public ImmutableBotIntent(IBotIntent botIntent) {
		this(botIntent.getTargetSpeed(), botIntent.getDrivingTurnRate(), botIntent.getGunTurnRate(),
				botIntent.getRadarTurnRate(), botIntent.getBulletPower());
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