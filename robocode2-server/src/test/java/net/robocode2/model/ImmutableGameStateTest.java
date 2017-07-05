package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableGameStateTest {

	static GameState initializedGameState;

	@BeforeClass
	public static void initialize() {
		GameState state = new GameState();

		state.setArena(new Arena(new Size(123, 456)));
		state.setGameEnded();

		Round round1 = new Round();
		round1.setRoundNumber(1);

		Round round2 = new Round();
		round2.setRoundNumber(2);

		List<IRound> rounds = state.getRounds();
		rounds.add(round1);
		rounds.add(round2);

		initializedGameState = state;
	}

	@Test
	public void constructorParams() {
		ImmutableGameState state = new ImmutableGameState(initializedGameState.getArena(),
				initializedGameState.getRounds(), initializedGameState.isGameEnded());

		assertEquals(initializedGameState.getArena(), state.getArena());
		assertEquals(initializedGameState.getRounds(), state.getRounds());
		assertEquals(initializedGameState.isGameEnded(), state.isGameEnded());
	}

	@Test
	public void unmodifiableRounds() {
		ImmutableGameState state = new ImmutableGameState(initializedGameState.getArena(),
				initializedGameState.getRounds(), initializedGameState.isGameEnded());
		try {
			state.getRounds().clear();
			fail();
		} catch (RuntimeException e) {
		}
	}
}
