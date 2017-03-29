package net.robocode2.model;

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
}
