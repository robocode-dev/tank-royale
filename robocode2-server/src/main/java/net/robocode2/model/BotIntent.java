package net.robocode2.model;

public class BotIntent implements IBotIntent {

	private Double targetSpeed;
	private Double bodyTurnRate;
	private Double gunTurnRate;
	private Double radarTurnRate;
	private Double bulletPower;

	public ImmutableBotIntent toImmutableBotIntent() {
		return new ImmutableBotIntent(this);
	}

	@Override
	public Double getTargetSpeed() {
		return targetSpeed;
	}

	@Override
	public Double getBodyTurnRate() {
		return bodyTurnRate;
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

	public void setTargetSpeed(double targetSpeed) {
		this.targetSpeed = targetSpeed;
	}

	public void setBodyTurnRate(double bodyTurnRate) {
		this.bodyTurnRate = bodyTurnRate;
	}

	public void setGunTurnRate(double gunTurnRate) {
		this.gunTurnRate = gunTurnRate;
	}

	public void setRadarTurnRate(double radarTurnRate) {
		this.radarTurnRate = radarTurnRate;
	}

	public void setBulletPower(double bulletPower) {
		this.bulletPower = bulletPower;
	}

	public void update(IBotIntent botIntent) {
		if (botIntent.getTargetSpeed() != null) {
			targetSpeed = botIntent.getTargetSpeed();
		}
		if (botIntent.getBodyTurnRate() != null) {
			bodyTurnRate = botIntent.getBodyTurnRate();
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
}