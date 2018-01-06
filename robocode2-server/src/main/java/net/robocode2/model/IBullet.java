package net.robocode2.model;

/**
 * Bullet interface.
 * 
 * @author Flemming N. Larsen
 */
public interface IBullet {

	/** Returns the id of the bot that fired this bullet */
	int getOwnerId();

	/** Returns the id of the bullet */
	int getBulletId();

	/** Returns the power of the bullet */
	double getPower();

	/** Returns the position, the bullet was fired from */
	Point getFirePosition();

	/** Returns the direction of the bullet in degrees */
	double getDirection();

	/** Returns the bullet speed */
	double getSpeed();

	/** Returns the tick, which is the number of turns since the bullet was fired */
	int getTick();
}