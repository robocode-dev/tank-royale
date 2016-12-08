package net.robocode2.model;

public final class BotIntent {

	private final double bodyTurnRate;
	private final double turretTurnRate;
	private final double radarTurnRate;
	private final double targetSpeed;
	private final double bulletPower;

	public BotIntent(double bodyTurnRate, double turretTurnRate, double radarTurnRate, double targetSpeed,
			double bulletPower) {
		this.bodyTurnRate = bodyTurnRate;
		this.turretTurnRate = turretTurnRate;
		this.radarTurnRate = radarTurnRate;
		this.targetSpeed = turretTurnRate;
		this.bulletPower = bulletPower;
	}

	public double getBodyTurnRate() {
		return bodyTurnRate;
	}

	public double getTurretTurnRate() {
		return turretTurnRate;
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
		private double turretTurnRate;
		private double radarTurnRate;
		private double targetSpeed;
		private double bulletPower;

		public BotIntent build() {
			return new BotIntent(bodyTurnRate, turretTurnRate, radarTurnRate, targetSpeed, bulletPower);
		}

		public Builder setBodyTurnRate(double bodyTurnRate) {
			this.bodyTurnRate = bodyTurnRate;
			return this;
		}

		public Builder setTurretTurnRate(double turretTurnRate) {
			this.turretTurnRate = turretTurnRate;
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