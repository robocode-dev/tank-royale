package net.robocode2.model;

/**
 * Immutable bullet instance.
 * 
 * @author Flemming N. Larsen
 */
public final class ImmutableBullet implements IBullet {

	/** Id of the bot that fired this bullet */
	private final int botId;
	/** Id of the bullet */
	private final int bulletId;
	/** Power of the bullet */
	private final double power;
	/** Position, the bullet was fired from */
	private final Point firePosition;
	/** Direction of the bullet in degrees */
	private final double direction;
	/** Bullet speed */
	private final double speed;
	/** Tick, which is the number of turns since the bullet was fired */
	private final int tick; // Used for calculating position with precision

	/**
	 * Creates a immutable bullet instance based on another bullet instance.
	 * 
	 * @param bullet
	 *            is the bullet instance that is deep copied into this bullet instance.
	 */
	public ImmutableBullet(IBullet bullet) {
		botId = bullet.getBotId();
		bulletId = bullet.getBulletId();
		power = bullet.getPower();
		firePosition = bullet.getFirePosition();
		direction = bullet.getDirection();
		speed = bullet.getSpeed();
		tick = bullet.getTick();
	}

	@Override
	public int getBotId() {
		return botId;
	}

	@Override
	public int getBulletId() {
		return bulletId;
	}

	@Override
	public double getPower() {
		return power;
	}

	@Override
	public Point getFirePosition() {
		return firePosition;
	}

	@Override
	public double getDirection() {
		return direction;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public int getTick() {
		return tick;
	}
}