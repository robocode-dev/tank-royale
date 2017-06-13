package net.robocode2.model;

public interface IRuleConstants {

	double INITIAL_BOT_ENERGY = 100.0;
	double INITIAL_GUN_HEAT = 3.0;

	int BOT_BOUNDING_CIRCLE_DIAMETER = 36;
	int BOT_BOUNDING_CIRCLE_RADIUS = BOT_BOUNDING_CIRCLE_DIAMETER / 2;

	double RADAR_RADIUS = 1200.0;

	double MAX_TURN_RATE = 10.0;
	double MAX_GUN_TURN_RATE = 20.0;
	double MAX_RADAR_TURN_RATE = 45.0;

	double MAX_FORWARD_SPEED = 8.0;
	double MAX_REVERSE_SPEED = -8.0;

	double MIN_BULLET_POWER = 0.1;
	double MAX_BULLET_POWER = 3.0;

	double MIN_BULLET_SPEED = RuleMath.calcBulletSpeed(MAX_BULLET_POWER);
	double MAX_BULLET_SPEED = RuleMath.calcBulletSpeed(MIN_BULLET_POWER);

	double ACCELLERATION = 1.0;
	double DECELERATION = -2.0;

	double RAM_DAMAGE = 0.6;

	int BULLET_HIT_ENERGY_GAIN_FACTOR = 3;

	double SCORE_PER_SURVIVAL = 50;
	double BONUS_PER_LAST_SURVIVOR = 10;
	double SCORE_PER_BULLET_DAMAGE = 1;
	double BONUS_PER_BULLET_KILL = 0.20;
	double SCORE_PER_RAM_DAMAGE = 2;
	double BONUS_PER_RAM_KILL = 0.30;
}
