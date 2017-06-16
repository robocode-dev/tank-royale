package net.robocode2.model;

import net.robocode2.util.MathUtil;

public interface IBot {

	int getId();

	double getEnergy();

	Point getPosition();

	double getDirection();

	double getGunDirection();

	double getRadarDirection();

	double getSpeed();

	double getGunHeat();

	ScanField getScanField();

	Score getScore();

	default boolean isAlive() {
		return getEnergy() >= 0;
	}

	default boolean isDead() {
		return !isAlive();
	}

	default boolean isDisabled() {
		return isAlive() && MathUtil.nearlyEqual(getEnergy(), 0);
	}
}
