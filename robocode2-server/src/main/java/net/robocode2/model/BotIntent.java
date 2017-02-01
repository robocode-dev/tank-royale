package net.robocode2.model;

public final class BotIntent {

	private final Double targetSpeed;
	private final Double bodyTurnRate;
	private final Double gunTurnRate;
	private final Double radarTurnRate;
	private final Double bulletPower;

	public BotIntent(Double targetSpeed, Double bodyTurnRate, Double gunTurnRate, Double radarTurnRate,
			Double bulletPower) {
		this.targetSpeed = targetSpeed;
		this.bodyTurnRate = bodyTurnRate;
		this.gunTurnRate = gunTurnRate;
		this.radarTurnRate = radarTurnRate;
		this.bulletPower = bulletPower;
	}

	public double getTargetSpeed() {
		return targetSpeed == null ? 0d : targetSpeed;
	}

	public double getBodyTurnRate() {
		return bodyTurnRate == null ? 0d : bodyTurnRate;
	}

	public double getGunTurnRate() {
		return gunTurnRate == null ? 0d : gunTurnRate;
	}

	public double getRadarTurnRate() {
		return radarTurnRate == null ? 0d : radarTurnRate;
	}

	public double getBulletPower() {
		return bulletPower == null ? 0d : bulletPower;
	}

	public static final class Builder {
		private Double targetSpeed;
		private Double bodyTurnRate;
		private Double gunTurnRate;
		private Double radarTurnRate;
		private Double bulletPower;

		public BotIntent build() {
			return new BotIntent(targetSpeed, bodyTurnRate, gunTurnRate, radarTurnRate, bulletPower);
		}

		public Builder setTargetSpeed(double targetSpeed) {
			this.targetSpeed = targetSpeed;
			return this;
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

		public Builder setBulletPower(double bulletPower) {
			this.bulletPower = bulletPower;
			return this;
		}

		public Builder update(BotIntent source) {
			if (source.targetSpeed != null) {
				targetSpeed = source.targetSpeed;
			}
			if (source.bodyTurnRate != null) {
				bodyTurnRate = source.bodyTurnRate;
			}
			if (source.gunTurnRate != null) {
				gunTurnRate = source.gunTurnRate;
			}
			if (source.radarTurnRate != null) {
				radarTurnRate = source.radarTurnRate;
			}
			if (source.bulletPower != null) {
				bulletPower = source.bulletPower;
			}
			return this;
		}
	}
}