package net.robocode2.model;

public final class ImmutableGameSetup implements IGameSetup {

	private final String gameType;
	private final int arenaWidth;
	private final int arenaHeight;
	private final int minNumberOfParticipants;
	private final Integer maxNumberOfParticipants;
	private final int numberOfRounds;
	private final double gunCoolingRate;
	private final int inactiveTurns;
	private final int turnTimeout;
	private final int readyTimeout;
	private final int numberOfDelayedTurnsForObservers;

	public ImmutableGameSetup(String gameType, int arenaWidth, int arenaHeight, int minNumberOfParticipants,
			Integer maxNumberOfParticipants, int numberOfRounds, double gunCoolingRate, int inactiveTurns,
			int turnTimeout, int readyTimeout, int numberOfDelayedTurnsForObservers) {

		this.gameType = gameType;
		this.arenaWidth = arenaWidth;
		this.arenaHeight = arenaHeight;
		this.minNumberOfParticipants = minNumberOfParticipants;
		this.maxNumberOfParticipants = maxNumberOfParticipants;
		this.numberOfRounds = numberOfRounds;
		this.gunCoolingRate = gunCoolingRate;
		this.inactiveTurns = inactiveTurns;
		this.turnTimeout = turnTimeout;
		this.readyTimeout = readyTimeout;
		this.numberOfDelayedTurnsForObservers = numberOfDelayedTurnsForObservers;
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
}