package net.robocode2.model;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * Mutable bullet instance.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder
public class Bullet {

	/** Id of the bot that fired this bullet */
	int botId;

	/** Id of the bullet */
	int bulletId;

	/** Power of the bullet */
	double power;

	/** Position, the bullet was fired from */
	Point firePosition;

	/** Direction of the bullet in degrees */
	double direction;

	/** Bullet speed */
	double speed;

	/** Tick, which is the number of turns since the bullet was fired */
	@Wither int tick;

	/**
	 * Calculates the position of a bullet.
	 * 
	 * @param firePosition
	 *            is the position, from which the bullet was fired
	 * @param direction
	 *            is the direction of the bullet
	 * @param speed
	 *            is the speed of the bullet
	 * @param tick
	 *            is the number of turns since the bullet was fired
	 * @return the calculated bullet position
	 */
	private static Point calcPosition(Point firePosition, double direction, double speed, int tick) {
		double angle = Math.toRadians(direction);
		double distance = speed * tick;
		double x = firePosition.x + Math.cos(angle) * distance;
		double y = firePosition.y + Math.sin(angle) * distance;
		return new Point(x, y);
	}

	/**
	 * Calculates the current bullet position based on the fire position and current tick.
	 * 
	 * @return the calculated bullet position
	 */
	public Point calcPosition() {
		return calcPosition(getFirePosition(), getDirection(), getSpeed(), getTick());
	}

	/**
	 * Calculates the next bullet position based on the fire position and current tick.
	 * 
	 * @return the calculated bullet position
	 */
	public Point calcNextPosition() {
		return calcPosition(getFirePosition(), getDirection(), getSpeed(), getTick() + 1);
	}
}