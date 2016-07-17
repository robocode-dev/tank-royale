package net.robocode2.model;

public final class GameState {

	private final int roundNumber;
	private final int turnNumber;
	private final Arena arena;

	public GameState(int roundNumber, int turnNumber, Arena arena) {
		this.roundNumber = roundNumber;
		this.turnNumber = turnNumber;
		this.arena = arena;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public Arena getArena() {
		return arena;
	}
}