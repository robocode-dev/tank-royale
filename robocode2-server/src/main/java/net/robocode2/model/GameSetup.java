package net.robocode2.model;

public final class GameSetup {

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

	public GameSetup(String gameType, int arenaWidth, int arenaHeight, int minNumberOfParticipants,
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

	public String getGameType() {
		return gameType;
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

	public double getGunCoolingRate() {
		return gunCoolingRate;
	}

	public int getInactiveTurns() {
		return inactiveTurns;
	}

	public int getTurnTimeout() {
		return turnTimeout;
	}

	public int getReadyTimeout() {
		return readyTimeout;
	}

	public int getNumberOfDelayedTurnsForObservers() {
		return numberOfDelayedTurnsForObservers;
	}

	public static final class Builder {
		private String gameType = "melee";
		private int arenaWidth = 300; // FIXME: 1000
		private int arenaHeight = 300; // FIXME: 1000
		private int minNumberOfParticipants = 2;
		private Integer maxNumberOfParticipants;
		private int numberOfRounds = 10;
		private double gunCoolingRate = 0.1;
		private int inactiveTurns = 450;
		private int turnTimeout = 100;
		private int readyTimeout = 10_000;
		private int numberOfDelayedTurnsForObservers = 10;

		public GameSetup build() {
			return new GameSetup(gameType, arenaWidth, arenaHeight, minNumberOfParticipants, maxNumberOfParticipants,
					numberOfRounds, gunCoolingRate, inactiveTurns, turnTimeout, readyTimeout,
					numberOfDelayedTurnsForObservers);
		}

		public Builder setGameType(String gameType) {
			this.gameType = gameType;
			return this;
		}

		public Builder setArenaWidth(int arenaWidth) {
			this.arenaWidth = arenaWidth;
			return this;
		}

		public Builder setArenaHeight(int arenaHeight) {
			this.arenaHeight = arenaHeight;
			return this;
		}

		public Builder setMinNumberOfParticipants(int minNumberOfParticipants) {
			this.minNumberOfParticipants = minNumberOfParticipants;
			return this;
		}

		public Builder setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
			this.maxNumberOfParticipants = maxNumberOfParticipants;
			return this;
		}

		public Builder setNumberOfRounds(int numberOfRounds) {
			this.numberOfRounds = numberOfRounds;
			return this;
		}

		public Builder setGunCoolingRate(int gunCoolingRate) {
			this.gunCoolingRate = gunCoolingRate;
			return this;
		}

		public Builder setInactiveTurns(int inactiveTurns) {
			this.inactiveTurns = inactiveTurns;
			return this;
		}

		public Builder setTurnTimeout(int turnTimeout) {
			this.turnTimeout = turnTimeout;
			return this;
		}

		public Builder setReadyTimeout(int readyTimeout) {
			this.readyTimeout = readyTimeout;
			return this;
		}

		public Builder setNumberOfDelayedTurnsForObservers(int numberOfDelayedTurnsForObservers) {
			this.numberOfDelayedTurnsForObservers = numberOfDelayedTurnsForObservers;
			return this;
		}
	}
}
