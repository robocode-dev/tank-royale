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
		if (rounds == null) {
			this.rounds = new ArrayList<>();
		} else {
			this.rounds = new ArrayList<>(rounds);
		}
		this.gameEnded = gameEnded;
	}

	public Arena getArena() {
		return arena;
	}

	public List<Round> getRounds() {
		return Collections.unmodifiableList(rounds);
	}

	public Round getLastRound() {
		int numRounds = rounds.size();
		if (numRounds > 0) {
			return rounds.get(numRounds - 1);
		}
		return null;
	}

	public boolean isGameEnded() {
		return gameEnded;
	}

	public static final class Builder {
		private Arena arena;
		private List<Round> rounds = new ArrayList<>();
		private boolean gameEnded;

		public GameState build() {
			return new GameState(arena, rounds, gameEnded);
		}

		public Builder setArena(Arena arena) {
			this.arena = arena;
			return this;
		}

		public Builder setRounds(List<Round> rounds) {
			this.rounds = new ArrayList<>(rounds);
			return this;
		}

		public Builder setGameEnded(boolean gameEnded) {
			this.gameEnded = gameEnded;
			return this;
		}

		public Builder appendRound(Round round) {
			rounds.add(round);
			return this;
		}
	}
}