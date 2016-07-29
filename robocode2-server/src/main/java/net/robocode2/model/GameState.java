package net.robocode2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameState {

	private final Arena arena;
	private final List<Round> rounds;
	private final boolean gameEnded;

	public GameState(Arena arena, List<Round> rounds, boolean gameEnded) {
		this.arena = arena;
		this.rounds = new ArrayList<>(rounds);
		this.gameEnded = gameEnded;
	}

	public Arena getArena() {
		return arena;
	}

	public List<Round> getRounds() {
		return Collections.unmodifiableList(rounds);
	}

	public boolean isGameEnded() {
		return gameEnded;
	}
}