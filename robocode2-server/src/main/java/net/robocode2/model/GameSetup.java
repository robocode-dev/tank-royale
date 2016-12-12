package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	private final Set<Integer> participantIds;

	public GameSetup(String gameType, int arenaWidth, int arenaHeight, int minNumberOfParticipants,
			Integer maxNumberOfParticipants, int numberOfRounds, double gunCoolingRate, int inactiveTurns,
			int turnTimeout, int readyTimeout, Set<Integer> participantIds) {

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
		if (participantIds == null) {
			this.participantIds = new HashSet<>();
		} else {
			this.participantIds = new HashSet<>(participantIds);
		}
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

	public Set<Integer> getParticipantIds() {
		return Collections.unmodifiableSet(participantIds);
	}

	public static final class Builder {
		private String gameType = "melee";
		private int arenaWidth = 1000;
		private int arenaHeight = 1000;
		private int minNumberOfParticipants = 2;
		private Integer maxNumberOfParticipants;
		private int numberOfRounds = 10;
		private double gunCoolingRate = 0.1;
		private int inactiveTurns = 450;
		private int turnTimeout = 100;
		private int readyTimeout = 10_000;
		private Set<Integer> participantIds = new HashSet<>();

		public GameSetup build() {
			return new GameSetup(gameType, arenaWidth, arenaHeight, minNumberOfParticipants, maxNumberOfParticipants,
					numberOfRounds, gunCoolingRate, inactiveTurns, turnTimeout, readyTimeout, participantIds);
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

		public Builder addParticipantId(int botId) {
			participantIds.add(botId);
			return this;
		}
	}
}
