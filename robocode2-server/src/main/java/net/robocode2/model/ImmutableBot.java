package net.robocode2.model;

/**
 * Immutable bot instance.
 * 
 * @author Flemming N. Larsen
 */
public final class ImmutableBot implements IBot {

	/** Bot id */
	private final int id;
	/** Energy level */
	private final double energy;
	/** Position on the arena */
	private final Point position;
	/** Driving direction in degrees */
	private final double direction;
	/** Gun direction in degrees */
	private final double gunDirection;
	/** Radar direction in degrees */
	private final double radarDirection;
	/** Radar spread angle in degrees */
	private double radarSpreadAngle;
	/** Speed */
	private final double speed;
	/** Gun heat */
	private final double gunHeat;
	/** Score record */
	private final Score score;

	/**
	 * Creates a immutable bot instance.
	 * 
	 * @param id
	 *            is the bot id
	 * @param energy
	 *            is the energy level
	 * @param position
	 *            is the position on the arena
	 * @param direction
	 *            is the driving direction in degrees
	 * @param gunDirection
	 *            is the gun direction in degrees
	 * @param radarDirection
	 *            is the radar direction in degrees
	 * @param radarSpreadAngle
	 *            is the radar spread angle in degrees
	 * @param speed
	 *            is the speed
	 * @param gunHeat
	 *            is the gun heat
	 * @param score
	 *            is the score record
	 */
	public ImmutableBot(int id, double energy, Point position, double direction, double gunDirection,
			double radarDirection, double radarSpreadAngle, double speed, double gunHeat, Score score) {

		this.id = id;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.gunDirection = gunDirection;
		this.radarDirection = radarDirection;
		this.radarSpreadAngle = radarSpreadAngle;
		this.speed = speed;
		this.gunHeat = gunHeat;
		this.score = score;
	}

	/**
	 * Creates a immutable bot instance based on another bot instance.
	 * 
	 * @param bot
	 *            is the bot instance that is deep copied into this bot instance.
	 */
	public ImmutableBot(IBot bot) {
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
	public double getRadarSpreadAngle() {
		return radarSpreadAngle;
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
	public Score getScore() {
		return score;
	}
}