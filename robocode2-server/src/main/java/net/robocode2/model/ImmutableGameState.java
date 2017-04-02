package net.robocode2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ImmutableGameState implements IGameState {

	private final Arena arena;
	private final List<IRound> rounds;
	private final boolean gameEnded;

	public ImmutableGameState(Arena arena, List<IRound> rounds, boolean gameEnded) {
		this.arena = arena;
		List<IRound> immuRounds = new ArrayList<>();
		if (rounds != null) {
			immuRounds.addAll(rounds);
		}
		this.rounds = Collections.unmodifiableList(immuRounds);
		this.gameEnded = gameEnded;
	}

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