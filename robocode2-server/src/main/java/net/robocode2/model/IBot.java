package net.robocode2.model;

import net.robocode2.game.MathUtil;

public interface IBot {

	int getId();

	double getEnergy();

	Point getPosition();

	double getDirection();

	double getGunDirection();

	double getRadarDirection();

	double getSpeed();

	double getGunHeat();

	Arc getScanArc();

	Score getScore();

	default boolean isAlive() {
		return getEnergy() >= 0;
	}

	default boolean isDead() {
		return !isAlive();
	}

	default boolean isDisabled() {
		return isAlive() && MathUtil.isNear(getEnergy(), 0);
	}
}
