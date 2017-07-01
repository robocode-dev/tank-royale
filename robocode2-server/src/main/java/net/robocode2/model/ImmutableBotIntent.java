package net.robocode2.model;

public final class ImmutableBotIntent implements IBotIntent {

	private final Double targetSpeed;
	private final Double drivingTurnRate;
	private final Double gunTurnRate;
	private final Double radarTurnRate;
	private final Double bulletPower;

	public ImmutableBotIntent(Double targetSpeed, Double drivingTurnRate, Double gunTurnRate, Double radarTurnRate,
			Double bulletPower) {

		this.targetSpeed = targetSpeed;
		this.drivingTurnRate = drivingTurnRate;
		this.gunTurnRate = gunTurnRate;
		this.radarTurnRate = radarTurnRate;
		this.bulletPower = bulletPower;
	}

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