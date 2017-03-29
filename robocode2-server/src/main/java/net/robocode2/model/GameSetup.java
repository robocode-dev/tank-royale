package net.robocode2.model;

public class GameSetup implements IGameSetup {

	private String gameType = "melee";
	private int arenaWidth = 400; // FIXME: 1000
	private int arenaHeight = 400; // FIXME: 1000
	private int minNumberOfParticipants = 2;
	private Integer maxNumberOfParticipants;
	private int numberOfRounds = 10;
	private double gunCoolingRate = 0.1;
	private int inactiveTurns = 450;
	private int turnTimeout = 100;
	private int readyTimeout = 10_000;
	private int numberOfDelayedTurnsForObservers = 10;

	public GameSetup() {
	}

	public GameSetup(IGameSetup gameSetup) {
		gameType = gameSetup.getGameType();
		arenaWidth = gameSetup.getArenaWidth();
		arenaHeight = gameSetup.getArenaWidth();
		minNumberOfParticipants = gameSetup.getMinNumberOfParticipants();
		maxNumberOfParticipants = gameSetup.getMaxNumberOfParticipants();
		numberOfRounds = gameSetup.getNumberOfRounds();
		gunCoolingRate = gameSetup.getGunCoolingRate();
		inactiveTurns = gameSetup.getInactiveTurns();
		turnTimeout = gameSetup.getTurnTimeout();
		readyTimeout = gameSetup.getReadyTimeout();
		numberOfDelayedTurnsForObservers = gameSetup.getNumberOfDelayedTurnsForObservers();
	}

	public ImmutableGameSetup toImmutableGameSetup() {
		return new ImmutableGameSetup(gameType, arenaWidth, arenaHeight, minNumberOfParticipants,
				maxNumberOfParticipants, numberOfRounds, gunCoolingRate, inactiveTurns, turnTimeout, readyTimeout,
				numberOfDelayedTurnsForObservers);
	}

	@Override
	public String getGameType() {
		return gameType;
	}

	@Override
	public int getArenaWidth() {
		return arenaWidth;
	}

	@Override
	public int getArenaHeight() {
		return arenaHeight;
	}

	@Override
	public int getMinNumberOfParticipants() {
		return minNumberOfParticipants;
	}

	@Override
	public Integer getMaxNumberOfParticipants() {
		return maxNumberOfParticipants;
	}

	@Override
	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	@Override
	public double getGunCoolingRate() {
		return gunCoolingRate;
	}

	@Override
	public int getInactiveTurns() {
		return inactiveTurns;
	}

	@Override
	public int getTurnTimeout() {
		return turnTimeout;
	}

	@Override
	public int getReadyTimeout() {
		return readyTimeout;
	}

	@Override
	public int getNumberOfDelayedTurnsForObservers() {
		return numberOfDelayedTurnsForObservers;
	}

	public void setGameType(String gameType) {
		this.gameType = gameType;
	}

	public void setArenaWidth(int arenaWidth) {
		this.arenaWidth = arenaWidth;
	}

	public void setArenaHeight(int arenaHeight) {
		this.arenaHeight = arenaHeight;
	}

	public void setMinNumberOfParticipants(int minNumberOfParticipants) {
		this.minNumberOfParticipants = minNumberOfParticipants;
	}

	public void setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
		this.maxNumberOfParticipants = maxNumberOfParticipants;
	}

	public void setNumberOfRounds(int numberOfRounds) {
		this.numberOfRounds = numberOfRounds;
	}

	public void setGunCoolingRate(int gunCoolingRate) {
		this.gunCoolingRate = gunCoolingRate;
	}

	public void setInactiveTurns(int inactiveTurns) {
		this.inactiveTurns = inactiveTurns;
	}

	public void setTurnTimeout(int turnTimeout) {
		this.turnTimeout = turnTimeout;
	}

	public void setReadyTimeout(int readyTimeout) {
		this.readyTimeout = readyTimeout;
	}

	public void setNumberOfDelayedTurnsForObservers(int numberOfDelayedTurnsForObservers) {
		this.numberOfDelayedTurnsForObservers = numberOfDelayedTurnsForObservers;
	}
}