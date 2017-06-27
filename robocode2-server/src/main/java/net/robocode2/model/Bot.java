package net.robocode2.model;

/**
 * Defines a mutable bot instance.
 * 
 * @author Flemming N. Larsen
 */
public class Bot implements IBot {

	/** Bot id */
	private int id;
	/** Energy level */
	private double energy = 100;
	/** Position on the arena */
	private Point position;
	/** Driving direction in degrees */
	private double direction;
	/** Gun direction in degrees */
	private double gunDirection;
	/** Radar direction in degrees */
	private double radarDirection;
	/** Speed */
	private double speed;
	/** Gun heat */
	private double gunHeat;
	/** Scan field */
	private ScanField scanField;
	/** Score record */
	private IScore score;

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
		speed = bot.getSpeed();
		gunHeat = bot.getGunHeat();
		scanField = bot.getScanField();
		score = bot.getScore();
	}

	/**
	 * Creates an immutable bot instance that is a copy of this bot.
	 * 
	 * @return an immutable bot instance
	 */
	public ImmutableBot toImmutableBot() {
		return new ImmutableBot(this);
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
	public ScanField getScanField() {
		return scanField;
	}

	@Override
	public IScore getScore() {
		return score;
	}

	/**
	 * Sets the bot is.
	 * 
	 * @param id
	 *            is the bot id.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the energy level.
	 * 
	 * @param energy
	 *            is the energy level.
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
	}

	/**
	 * Sets the position.
	 * 
	 * @param positionis
	 *            the position.
	 */
	public void setPosition(Point position) {
		this.position = position;
	}

	/**
	 * Sets the driving direction.
	 * 
	 * @param direction
	 *            is the driving direction in degrees.
	 */
	public void setDirection(double direction) {
		this.direction = direction;
	}

	/**
	 * Sets the gun direction.
	 * 
	 * @param gunDirection
	 *            is the gun direction in degrees.
	 */
	public void setGunDirection(double gunDirection) {
		this.gunDirection = gunDirection;
	}

	/**
	 * Sets the radar direction.
	 * 
	 * @param radarDirection
	 *            is the radar direction in degrees.
	 */
	public void setRadarDirection(double radarDirection) {
		this.radarDirection = radarDirection;
	}

	/**
	 * Sets the speed.
	 * 
	 * @param speed
	 *            is the speed, which can be positive and negative. With a positive speed, the bot is moving forward.
	 *            With a negative speed, the bot is moving backwards. A speed of 0 means that the bot must stand still.
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Sets the gun heat.
	 * 
	 * @param gunHeat
	 *            is the gun heat. When the gun heat is greater that 0, the gun is not able to fire. Hence, the gun must
	 *            be cooled down first. The gun will automatically cool down after some turns.
	 */
	public void setGunHeat(double gunHeat) {
		this.gunHeat = gunHeat;
	}

	/**
	 * Sets the scan field.
	 * 
	 * @param scanArc
	 *            is the scan field.
	 */
	public void setScanField(ScanField scanArc) {
		this.scanField = scanArc;
	}

	/**
	 * Sets the score record.
	 * 
	 * @param score
	 *            is the score record.
	 */
	public void setScore(IScore score) {
		this.score = score;
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