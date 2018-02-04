package net.robocode2.model;

import lombok.Data;

/**
 * Mutable bot instance.
 * 
 * @author Flemming N. Larsen
 */
@Data
public class Bot implements IBot {

	/** Bot id */
	int id;

	/** Energy level */
	double energy = 100;

	/** Position on the arena */
	Point position;

	/** Driving direction in degrees */
	double direction;

	/** Gun direction in degrees */
	double gunDirection;

	/** Radar direction in degrees */
	double radarDirection;

	/** Radar spread angle in degrees */
	double radarSpreadAngle;

	/** Speed */
	double speed;

	/** Gun heat */
	double gunHeat;

	/** Score record */
	Score score;

	/**
	 * Creates a mutable bot that needs to be initialized
	 */
	public Bot() {
	}

	/**
	 * Creates a mutable bot that is initialized by another bot instance
	 * 
	 * @param bot
	 *            is the other bot instance, which is deep copied into this bot.
	 */
	public Bot(IBot bot) {
		id = bot.getId();
		energy = bot.getEnergy();
		position = bot.getPosition();
		direction = bot.getDirection();
		gunDirection = bot.getGunDirection();
		radarDirection = bot.getRadarDirection();
		radarSpreadAngle = bot.getRadarSpreadAngle();
		speed = bot.getSpeed();
		gunHeat = bot.getGunHeat();
		score = bot.getScore();
	}

	/**
	 * Creates an immutable bot instance that is a deep copy of this bot.
	 * 
	 * @return an immutable bot instance
	 */
	public ImmutableBot toImmutableBot() {
		return new ImmutableBot(this);
	}

	/**
	 * Adds damage to the bot.
	 * 
	 * @param damage
	 *            is the damage done to this bot
	 * @return true if the robot got killed due to the damage, false otherwise.
	 */
	public boolean addDamage(double damage) {
		boolean aliveBefore = isAlive();
		energy -= damage;
		return isDead() && aliveBefore;
	}

	/**
	 * Change the energy level.
	 * 
	 * @param deltaEnergy
	 *            is the delta energy to add to the current energy level, which can be both positive and negative.
	 */
	public void changeEnergy(double deltaEnergy) {
		energy += deltaEnergy;
	}

	/**
	 * Move bot to new position of the bot based on the current position, the driving direction and speed.
	 */
	public void moveToNewPosition() {
		position = calcNewPosition(direction, speed);
	}

	/**
	 * Moves bot backwards a specific amount of distance due to bouncing.
	 * 
	 * @param distance
	 *            is the distance to bounce back
	 */
	public void bounceBack(double distance) {
		position = calcNewPosition(direction, (speed > 0 ? -distance : distance));
	}

	/**
	 * Calculates new position of the bot based on the current position, the driving direction and speed.
	 * 
	 * @param direction
	 *            is the new driving direction
	 * @param distance
	 *            is the distance to move
	 * @return the calculated new position of the bot
	 */
	private Point calcNewPosition(double direction, double distance) {
		double angle = Math.toRadians(direction);
		double x = position.x + Math.cos(angle) * distance;
		double y = position.y + Math.sin(angle) * distance;
		return new Point(x, y);
	}
}