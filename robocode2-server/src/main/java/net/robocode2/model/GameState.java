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

	public static final class GameStateBuilder {
		private Arena arena;
		private List<Round> rounds;
		private boolean gameEnded;

		public GameState build() {
			return new GameState(arena, rounds, gameEnded);
		}

		public GameStateBuilder setArena(Arena arena) {
			this.arena = arena;
			return this;
		}

		public GameStateBuilder setRounds(List<Round> rounds) {
			this.rounds = new ArrayList<>(rounds);
			return this;
		}

		public GameStateBuilder setGameEnded(boolean gameEnded) {
			this.gameEnded = gameEnded;
			return this;
		}

		public GameStateBuilder appendRound(Round round) {
			rounds.add(round);
			return this;
		}
	}
}