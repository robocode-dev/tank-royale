package net.robocode2.model;

public final class BotIntent {

	private final double bodyTurnRate;
	private final double gunTurnRate;
	private final double radarTurnRate;
	private final double targetSpeed;
	private final double bulletPower;

	public BotIntent(double bodyTurnRate, double gunTurnRate, double radarTurnRate, double targetSpeed,
			double bulletPower) {
		this.bodyTurnRate = bodyTurnRate;
		this.gunTurnRate = gunTurnRate;
		this.radarTurnRate = radarTurnRate;
		this.targetSpeed = targetSpeed;
		this.bulletPower = bulletPower;
	}

	public double getBodyTurnRate() {
		return bodyTurnRate;
	}

	public double getGunTurnRate() {
		return gunTurnRate;
	}

	public double getRadarTurnRate() {
		return radarTurnRate;
	}

	public double getTargetSpeed() {
		return targetSpeed;
	}

	public double getBulletPower() {
		return bulletPower;
	}

	public static final class Builder {
		private double bodyTurnRate;
		private double gunTurnRate;
		private double radarTurnRate;
		private double targetSpeed;
		private double bulletPower;

		public BotIntent build() {
			return new BotIntent(bodyTurnRate, gunTurnRate, radarTurnRate, targetSpeed, bulletPower);
		}

		public Builder setBodyTurnRate(double bodyTurnRate) {
			this.bodyTurnRate = bodyTurnRate;
			return this;
		}

		public Builder setGunTurnRate(double gunTurnRate) {
			this.gunTurnRate = gunTurnRate;
			return this;
		}

		public Builder setRadarTurnRate(double radarTurnRate) {
			this.radarTurnRate = radarTurnRate;
			return this;
		}

		public Builder setTargetSpeed(double targetSpeed) {
			this.targetSpeed = targetSpeed;
			return this;
		}

		public Builder setBulletPower(double bulletPower) {
			this.bulletPower = bulletPower;
			return this;
		}
	}
}