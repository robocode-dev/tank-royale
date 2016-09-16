package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Setup {

	private final String gameType;
	private final int arenaWidth;
	private final int arenaHeight;
	private final int numberOfRounds;
	private final int turnTimeout;
	private final int readyTimeout;

	private final Set<Integer> participantIds;

	public Setup(String gameType, int arenaWidth, int arenaHeight, int numberOfRounds, int turnTimeout,
			int readyTimeout, Set<Integer> participantIds) {

		this.gameType = gameType;
		this.arenaWidth = arenaWidth;
		this.arenaHeight = arenaHeight;
		this.numberOfRounds = numberOfRounds;
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

	public int getNumberOfRounds() {
		return numberOfRounds;
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
}
