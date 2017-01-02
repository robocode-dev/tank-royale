package net.robocode2.model;

public interface ImmutableBot {

	int getId();

	double getEnergy();

	boolean isDisabled();

	Position getPosition();

	double getDirection();

	double getGunDirection();

	double getRadarDirection();

	double getSpeed();

	double getGunHeat();

	Arc getScanArc();

	Score getScore();
}
