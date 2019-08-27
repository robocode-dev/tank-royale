package dev.robocode.tankroyale.server.model;

/**
 * Rule constants
 * 
 * @author Flemming N. Larsen
 */
@SuppressWarnings("unused")
public final class RuleConstants {
	
	private RuleConstants() {}

	/** Arena minimum size (width / height) */
	public static final int ARENA_MIN_SIZE = 400;
	/** Arena maximum size (width / height) */
	public static final int ARENA_MAX_SIZE = 5000;

	/** Minimum gun cooling rate */
	public static final double MIN_GUN_COOLING_RATE = 0.1;
	/** Maximum gun cooling rate */
	public static final double MAX_GUN_COOLING_RATE = 3.0;

	/** Initial bot energy level */
	public static final double INITIAL_BOT_ENERGY = 100.0;
	/** Initial gun heat */
	public static final double INITIAL_GUN_HEAT = 3.0;

	/** Bot bounding circle diameter */
	public static final int BOT_BOUNDING_CIRCLE_DIAMETER = 36;
	/** Bot bounding circle radius */
	public static final int BOT_BOUNDING_CIRCLE_RADIUS = BOT_BOUNDING_CIRCLE_DIAMETER / 2;

	/** Radar radius */
	public static final double RADAR_RADIUS = 1200.0;

	/** Maximum driving turn rate */
	public static final double MAX_TURN_RATE = 10.0;
	/** Maximum gun turn rate */
	public static final double MAX_GUN_TURN_RATE = 20.0;
	/** Maximum radar turn rate */
	public static final double MAX_RADAR_TURN_RATE = 45.0;

	/** Maximum forward speed */
	public static final double MAX_FORWARD_SPEED = 8.0;
	/** Maximum backward speed */
	public static final double MAX_BACKWARD_SPEED = -8.0;

	/** Minimum firepower */
	public static final double MIN_FIREPOWER = 0.1;
	/** Maximum firepower */
	public static final double MAX_FIREPOWER = 3.0;

	/** Minimum bullet speed */
	public static final double MIN_BULLET_SPEED = RuleMath.calcBulletSpeed(MAX_FIREPOWER);
	/** Maximum bullet speed */
	public static final double MAX_BULLET_SPEED = RuleMath.calcBulletSpeed(MIN_FIREPOWER);

	/** Acceleration */
	public static final double ACCELERATION = 1.0;
	/** Deceleration */
	public static final double DECELERATION = -2.0;

	/** Ram damage */
	public static final double RAM_DAMAGE = 0.6;

	/** Energy gain factor, when bullet hits */
	public static final int BULLET_HIT_ENERGY_GAIN_FACTOR = 3;

	/** Score per survival */
	public static final double SCORE_PER_SURVIVAL = 50;
	/** Bonus for last survival */
	public static final double BONUS_PER_LAST_SURVIVOR = 10;
	/** Score per bullet damage */
	public static final double SCORE_PER_BULLET_DAMAGE = 1;
	/** Bonus per bullet kill */
	public static final double BONUS_PER_BULLET_KILL = 0.20;
	/** Score per ram damage */
	public static final double SCORE_PER_RAM_DAMAGE = 2;
	/** Bonus per ram kill */
	public static final double BONUS_PER_RAM_KILL = 0.30;

	/** Inactivity punishment damage per turn */
	public static final double INACTIVITY_DAMAGE = 0.1;
}
