package net.robocode2.model;

public final class ImmutableBot implements IBot {

	private final int id;
	private final double energy;
	private final Point position;
	private final double direction;
	private final double gunDirection;
	private final double radarDirection;
	private final double speed;
	private final double gunHeat;
	private final ScanField scanField;
	private final Score score;

	public ImmutableBot(int id, double energy, Point position, double direction, double gunDirection,
			double radarDirection, double speed, double gunHeat, ScanField scanArc, Score score) {

		this.id = id;
		this.energy = energy;
		this.position = position;
		this.direction = direction;
		this.gunDirection = gunDirection;
		this.radarDirection = radarDirection;
		this.speed = speed;
		this.gunHeat = gunHeat;
		this.scanField = scanArc;
		this.score = score;
	}

	public ImmutableBot(IBot bot) {
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
	public Score getScore() {
		return score;
	}
}