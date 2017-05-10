package net.robocode2.model;

public class Bullet implements IBullet {
	private int botId;
	private int bulletId;
	private double power;
	private Point firePosition;
	private double direction;
	private double speed;
	private int tick;

	public Bullet() {
	}

	public Bullet(IBullet bullet) {
		botId = bullet.getBotId();
		bulletId = bullet.getBulletId();
		power = bullet.getPower();
		firePosition = bullet.getFirePosition();
		direction = bullet.getDirection();
		speed = bullet.getSpeed();
		tick = bullet.getTick();
	}

	public ImmutableBullet toImmutableBullet() {
		return new ImmutableBullet(this);
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

	public void setBotId(int botId) {
		this.botId = botId;
	}

	public void setBulletId(int bulletId) {
		this.bulletId = bulletId;
	}

	public void setPower(double power) {
		this.power = power;
	}

	public void setFirePosition(Point firePosition) {
		this.firePosition = firePosition;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public void incrementTick() {
		this.tick++;
	}
}
