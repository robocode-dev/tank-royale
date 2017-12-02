package net.robocode2.model;

/**
 * Mutable bullet instance.
 * 
 * @author Flemming N. Larsen
 */
public class Bullet implements IBullet {

	/** Id of the bot that fired this bullet */
	private int botId;
	/** Id of the bullet */
	private int bulletId;
	/** Power of the bullet */
	private double power;
	/** Position, the bullet was fired from */
	private Point firePosition;
	/** Direction of the bullet in degrees */
	private double direction;
	/** Bullet speed */
	private double speed;
	/** Tick, which is the number of turns since the bullet was fired */
	private int tick;

	/**
	 * Creates a mutable bullet that needs to be initialized
	 */
	public Bullet() {
	}

	/**
	 * Creates a mutable bullet that is initialized by another bullet instance
	 * 
	 * @param bullet
	 *            is the other bullet instance, which is deep copied into this bullet.
	 */
	public Bullet(IBullet bullet) {
		botId = bullet.getOwnerId();
		bulletId = bullet.getBulletId();
		power = bullet.getPower();
		firePosition = bullet.getFirePosition();
		direction = bullet.getDirection();
		speed = bullet.getSpeed();
		tick = bullet.getTick();
	}

	/**
	 * Creates a immutable bullet instance that is a deep copy of this bullet.
	 * 
	 * @return a immutable bullet instance
	 */
	public ImmutableBullet toImmutableBullet() {
		return new ImmutableBullet(this);
	}

	@Override
	public int getOwnerId() {
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

	/**
	 * Sets the id of the bot that fired this bullet
	 *
	 * @param botId
	 *            is the bot id
	 */
	public void setBotId(int botId) {
		this.botId = botId;
	}

	/**
	 * Sets the id of the bullet
	 * 
	 * @param bulletId
	 *            is the bullet id
	 */
	public void setBulletId(int bulletId) {
		this.bulletId = bulletId;
	}

	/**
	 * Sets the power of the bullet
	 * 
	 * @param power
	 *            is the power of the bullet
	 */
	public void setPower(double power) {
		this.power = power;
	}

	/**
	 * Sets the position, the bullet was fired from
	 * 
	 * @param firePosition
	 *            is the fire position
	 */
	public void setFirePosition(Point firePosition) {
		this.firePosition = firePosition;
	}

	/**
	 * Sets the direction of the bullet
	 * 
	 * @param direction
	 *            is the direction of the bullet
	 */
	public void setDirection(double direction) {
		this.direction = direction;
	}

	/**
	 * Sets the speed of the bullet
	 * 
	 * @param speed
	 *            is the speed of the bullet
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Sets the tick, which is the number of turns since the bullet was fired
	 * 
	 * @param tick
	 *            is the number of turns since the bullet was fired
	 */
	public void setTick(int tick) {
		this.tick = tick;
	}

	/**
	 * Increments the tick to "move" the bullet to the next turn in the battle
	 */
	public void incrementTick() {
		this.tick++;
	}
}
