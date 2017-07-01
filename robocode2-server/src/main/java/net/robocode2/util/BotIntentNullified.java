package net.robocode2.util;

import net.robocode2.model.IBotIntent;

public final class BotIntentNullified implements IBotIntent {

	private final IBotIntent botIntent;

	public BotIntentNullified(IBotIntent botIntent) {
		this.botIntent = botIntent;
	}

	@Override
	public Double getTargetSpeed() {
		return botIntent.getTargetSpeed() == null ? 0d : botIntent.getTargetSpeed();
	}

	@Override
	public Double getDrivingTurnRate() {
		return botIntent.getDrivingTurnRate() == null ? 0d : botIntent.getDrivingTurnRate();
	}

	@Override
	public Double getGunTurnRate() {
		return botIntent.getGunTurnRate() == null ? 0d : botIntent.getGunTurnRate();
	}

	@Override
	public Double getRadarTurnRate() {
		return botIntent.getRadarTurnRate() == null ? 0d : botIntent.getRadarTurnRate();
	}

	@Override
	public Double getBulletPower() {
		return botIntent.getBulletPower() == null ? 0d : botIntent.getBulletPower();
	}
}