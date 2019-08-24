package dev.robocode.tankroyale.server.model;

import lombok.Builder;
import lombok.Value;

/**
 * Bullet instance.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public class Bullet {

	/** Id of the bot that fired this bullet */
	int botId;

	/** Id of the bullet */
	int bulletId;

	/** Power of the bullet */
	double power;

	/** X coordinate of the position where the bullet was fired from */
	double startX;

	/** Y coordinate of the position where the bullet was fired from */
	double startY;

	/** Direction of the bullet in degrees */
	double direction;

	/** Tick, which is the number of turns since the bullet was fired */
	int tick;

	public double getSpeed() {
		return RuleMath.calcBulletSpeed(power);
	}
	
	/**
	 * Calculates the current bullet position based on the fire position and current tick.
	 * 
	 * @return the calculated bullet position
	 */
	public Point calcPosition() {
		return calcPosition(startX, startY, getDirection(), getSpeed(), getTick());
	}

	/**
	 * Calculates the next bullet position based on the fire position and current tick.
	 * 
	 * @return the calculated bullet position
	 */
	public Point calcNextPosition() {
		return calcPosition(startX, startY, getDirection(), getSpeed(), getTick() + 1);
	}

	/**
	 * Calculates the position of a bullet.
	 * 
	 * @param startX
	 *            is the x coordinate of the position where the bullet was fired from
	 * @param startY
	 *            is the y coordinate of the position where the bullet was fired from
	 * @param direction
	 *            is the direction of the bullet
	 * @param speed
	 *            is the speed of the bullet
	 * @param tick
	 *            is the number of turns since the bullet was fired
	 * @return the calculated bullet position
	 */
	private static Point calcPosition(double startX, double startY, double direction, double speed, int tick) {
		double angle = Math.toRadians(direction);
		double distance = speed * tick;
		double x = startX + Math.cos(angle) * distance;
		double y = startY + Math.sin(angle) * distance;
		return new Point(x, y);
	}
}