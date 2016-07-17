package net.robocode2.model;

public final class Setup {

	private final String gameType;
	private final boolean obstacles;
	private final int arenaWidth;
	private final int arenaHeight;
	private final int minNumberOfParticipants;
	private final Integer maxNumberOfParticipants;
	private final int numberOfRounds;
	private final int turnTimeout;
	private final int readyTimeout;

	public Setup(String gameType, boolean obstacles, int arenaWidth, int arenaHeight, int minNumberOfParticipants,
			Integer maxNumberOfParticipants, int numberOfRounds, int turnTimeout, int readyTimeout) {
		this.gameType = gameType;
		this.obstacles = obstacles;
		this.arenaWidth = arenaWidth;
		this.arenaHeight = arenaHeight;
		this.minNumberOfParticipants = minNumberOfParticipants;
		this.maxNumberOfParticipants = maxNumberOfParticipants;
		this.numberOfRounds = numberOfRounds;
		this.turnTimeout = turnTimeout;
		this.readyTimeout = readyTimeout;
	}

	public String getGameType() {
		return gameType;
	}

	public boolean hasObstacles() {
		return obstacles;
	}

	public int getArenaWidth() {
		return arenaWidth;
	}

	public int getArenaHeight() {
		return arenaHeight;
	}

	public int getMinNumberOfParticipants() {
		return minNumberOfParticipants;
	}

	public Integer getMaxNumberOfParticipants() {
		return maxNumberOfParticipants;
	}

	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public int getTurnTimeout() {
		return turnTimeout;
	}

	public int getReadyTimeout() {
		return readyTimeout;
	}
}
