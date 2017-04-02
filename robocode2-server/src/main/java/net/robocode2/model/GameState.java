package net.robocode2.model;

import java.util.ArrayList;
import java.util.List;

public class GameState implements IGameState {

	private Arena arena;
	private final List<IRound> rounds = new ArrayList<>();
	private boolean gameEnded;

	public ImmutableGameState toImmutableGameState() {
		return new ImmutableGameState(this);
	}

	@Override
	public Arena getArena() {
		return arena;
	}

	@Override
	public List<IRound> getRounds() {
		return rounds;
	}

	@Override
	public boolean isGameEnded() {
		return gameEnded;
	}

	public void setArena(Arena arena) {
		this.arena = arena;
	}

	public void appendRound(IRound round) {
		this.rounds.add(round);
	}

	public void setGameEnded() {
		gameEnded = true;
	}
}