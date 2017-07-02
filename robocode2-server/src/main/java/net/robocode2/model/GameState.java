package net.robocode2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable game state.
 * 
 * @author Flemming N. Larsen
 */
public class GameState implements IGameState {

	/** Arena */
	private Arena arena;
	/** List of rounds */
	private final List<IRound> rounds = new ArrayList<>();
	/** Flag specifying if game has ended */
	private boolean gameEnded;

	/**
	 * Creates an immutable game state that is a copy of this state.
	 * 
	 * @return an immutable game state
	 */
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

	/**
	 * Sets the arena
	 * 
	 * @param arena
	 *            is the arena
	 */
	public void setArena(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Appends a round to the game state
	 * 
	 * @param round
	 *            is the round to append
	 */
	public void appendRound(IRound round) {
		this.rounds.add(round);
	}

	/**
	 * Flag the game has ended
	 */
	public void setGameEnded() {
		gameEnded = true;
	}
}