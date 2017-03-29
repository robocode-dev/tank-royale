package net.robocode2.model;

import static net.robocode2.game.MathUtil.isNear;

public class Bot implements IBot {

	private int id;
	private double energy = 100;
	private Point position;
	private double direction;
	private double gunDirection;
	private double radarDirection;
	private double speed;
	private double gunHeat;
	private Arc scanArc;
	private Score score;

	public Bot() {
	}

	public Bot(IBot bot) {
		id = bot.getId();
		energy = bot.getEnergy();
		position = bot.getPosition();
		direction = bot.getDirection();
		gunDirection = bot.getGunDirection();
		radarDirection = bot.getRadarDirection();
		speed = bot.getSpeed();
		gunHeat = bot.getGunHeat();
		scanArc = bot.getScanArc();
		score = bot.getScore();
	}

	public ImmutableBot toImmutableBot() {
		return new ImmutableBot(id, energy, position, direction, gunDirection, radarDirection, speed, gunHeat, scanArc,
				score);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public double getEnergy() {
		return energy;
	}

	@Override
	public Point getPosition() {
		return position;
	}

	@Override
	public double getDirection() {
		return direction;
	}

	@Override
	public double getGunDirection() {
		return gunDirection;
	}

	@Override
	public double getRadarDirection() {
		return radarDirection;
	}

	@Override
	public double getSpeed() {
		return speed;
	}

	@Override
	public double getGunHeat() {
		return gunHeat;
	}

	@Override
	public Arc getScanArc() {
		return scanArc;
	}

	@Override
	public Score getScore() {
		return score;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public void setDisabled() {
		this.energy = 0.0;
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public void setGunDirection(double gunDirection) {
		this.gunDirection = gunDirection;
	}

	public void setRadarDirection(double radarDirection) {
		this.radarDirection = radarDirection;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setGunHeat(double gunHeat) {
		this.gunHeat = gunHeat;
	}

	public void setScanArc(Arc scanArc) {
		this.scanArc = scanArc;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public boolean isAlive() {
		return energy >= 0;
	}

	public boolean isDead() {
		return !isAlive();
	}

	public boolean isDisabled() {
		return isAlive() && isNear(energy, 0);
	}

	/**
	 * Adds damage to the bot.
	 * 
	 * @param damage
	 * @return true if the robot got killed due to the damage, false otherwise.
	 */
	public boolean addDamage(double damage) {
		boolean aliveBefore = isAlive();
		energy -= damage;
		return isDead() && aliveBefore;
	}

	public void increaseEnergy(double gain) {
		energy += gain;
	}

	public void moveToNewPosition() {
		position = move(direction, speed);
	}

	public void bounceBack(double distance) {
		position = move(direction, (speed > 0 ? -distance : distance));
	}

	private Point move(double direction, double distance) {
		double angle = Math.toRadians(direction);
		double x = position.x + Math.cos(angle) * distance;
		double y = position.y + Math.sin(angle) * distance;
		return new Point(x, y);
	}
}