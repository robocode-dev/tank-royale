package net.robocode2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable game state
 * 
 * @author Flemming N. Larsen
 */
public final class ImmutableGameState implements IGameState {

	/** Arena */
	private final Arena arena;
	/** List of rounds */
	private final List<IRound> rounds;
	/** Flag specifying if game has ended */
	private final boolean gameEnded;

	/**
	 * Creates a immutable game state.
	 * 
	 * @param arena
	 *            is the arena
	 * @param rounds
	 *            is list of rounds
	 * @param gameEnded
	 *            is specifying if the game has ended
	 */
	public ImmutableGameState(Arena arena, List<IRound> rounds, boolean gameEnded) {
		this.arena = arena;
		List<IRound> immuRounds = new ArrayList<>();
		if (rounds != null) {
			immuRounds.addAll(rounds);
		}
		this.rounds = Collections.unmodifiableList(immuRounds);
		this.gameEnded = gameEnded;
	}

	/**
	 * Creates a immutablegame state based on anothergame state.
	 * 
	 * @param bot
	 *            is thegame state that is deep copied into thisgame state.
	 */
	public ImmutableGameState(IGameState gameState) {
		this(gameState.getArena(), gameState.getRounds(), gameState.isGameEnded());
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
}