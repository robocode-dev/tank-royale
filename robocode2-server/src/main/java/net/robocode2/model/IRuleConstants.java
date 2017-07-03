package net.robocode2.model;

/**
 * Rule constants
 * 
 * @author Flemming N. Larsen
 */
public interface IRuleConstants {

	/** Arena minimum width */
	int ARENA_MIN_WIDTH = 400;
	/** Arena minimum height */
	int ARENA_MIN_HEIGHT = 400;
	/** Arena maximum width */
	int ARENA_MAX_WIDTH = 5000;
	/** Arena maximum height */
	int ARENA_MAX_HEIGHT = 5000;

	/** Minimum gun cooling rate */
	double MIN_GUN_COOLING_RATE = 0.1;
	/** Maximum gun cooling rate */
	double MAX_GUN_COOLING_RATE = 3.0;

	/** Initial bot energy level */
	double INITIAL_BOT_ENERGY = 100.0;
	/** Initial gun heat */
	double INITIAL_GUN_HEAT = 3.0;

	/** Bot bounding circle diameter */
	int BOT_BOUNDING_CIRCLE_DIAMETER = 36;
	/** Bot bounding circle radius */
	int BOT_BOUNDING_CIRCLE_RADIUS = BOT_BOUNDING_CIRCLE_DIAMETER / 2;

	/** Radar radius */
	double RADAR_RADIUS = 1200.0;

	/** Maximum driving turn rate */
	double MAX_TURN_RATE = 10.0;
	/** Maximum gun turn rate */
	double MAX_GUN_TURN_RATE = 20.0;
	/** Maximum radar turn rate */
	double MAX_RADAR_TURN_RATE = 45.0;

	/** Maximum forward speed */
	double MAX_FORWARD_SPEED = 8.0;
	/** Maximum reverse speed */
	double MAX_REVERSE_SPEED = -8.0;

	/** Minimum bullet power */
	double MIN_BULLET_POWER = 0.1;
	/** Maximum bullet power */
	double MAX_BULLET_POWER = 3.0;

	/** Minimum bullet speed */
	double MIN_BULLET_SPEED = RuleMath.calcBulletSpeed(MAX_BULLET_POWER);
	/** Maximum bullet speed */
	double MAX_BULLET_SPEED = RuleMath.calcBulletSpeed(MIN_BULLET_POWER);

	/** Acceleration */
	double ACCELERATION = 1.0;
	/** Deceleration */
	double DECELERATION = -2.0;

	/** Ram damage */
	double RAM_DAMAGE = 0.6;

	/** Energy gain factor, when bullet hits */
	int BULLET_HIT_ENERGY_GAIN_FACTOR = 3;

	/** Score per survival */
	double SCORE_PER_SURVIVAL = 50;
	/** Bonus for last survival */
	double BONUS_PER_LAST_SURVIVOR = 10;
	/** Score per bullet damage */
	double SCORE_PER_BULLET_DAMAGE = 1;
	/** Bonus per bullet kill */
	double BONUS_PER_BULLET_KILL = 0.20;
	/** Score per ram damage */
	double SCORE_PER_RAM_DAMAGE = 2;
	/** Bonus per ram kill */
	double BONUS_PER_RAM_KILL = 0.30;
}
