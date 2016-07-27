package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Setup {

	private final String gameType;
	private final boolean obstacles;
	private final int arenaWidth;
	private final int arenaHeight;
	private final int numberOfRounds;
	private final int turnTimeout;
	private final int readyTimeout;

	private final Set<Integer> participantIds;

	public Setup(String gameType, boolean obstacles, int arenaWidth, int arenaHeight, int numberOfRounds,
			int turnTimeout, int readyTimeout, Set<Integer> participantIds) {

		this.gameType = gameType;
		this.obstacles = obstacles;
		this.arenaWidth = arenaWidth;
		this.arenaHeight = arenaHeight;
		this.numberOfRounds = numberOfRounds;
		this.turnTimeout = turnTimeout;
		this.readyTimeout = readyTimeout;
		this.participantIds = new HashSet<Integer>(participantIds);
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
