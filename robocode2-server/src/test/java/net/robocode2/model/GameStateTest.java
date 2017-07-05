package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class GameStateTest {

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
	public void constructorEmpty() {
		GameState state = new GameState();

		assertNull(state.getArena());
		assertFalse(state.isGameEnded());
		assertNotNull(state.getRounds());
		assertEquals(0, state.getRounds().size());
		assertNull(state.getLastRound());
	}

	@Test
	public void toImmutableGameSetup() {
		ImmutableGameState immuState = initializedGameState.toImmutableGameState();

		Round round1 = new Round();
		round1.setRoundNumber(1);

		Round round2 = new Round();
		round2.setRoundNumber(2);

		List<IRound> rounds = new ArrayList<>();
		rounds.add(round1);
		rounds.add(round2);

		assertReflectionEquals(new Arena(new Size(123, 456)), immuState.getArena());
		assertReflectionEquals(rounds, immuState.getRounds());
		assertTrue(immuState.isGameEnded());
	}

	@Test
	public void setArena() {
		GameState state = new GameState();
		state.setArena(new Arena(new Size(785, 235)));
		assertReflectionEquals(new Arena(new Size(785, 235)), state.getArena());

		state.setArena(null);
		assertNull(state.getArena());
	}

	@Test
	public void appendRound() {
		GameState state = new GameState();

		assertNotNull(state.getRounds());
		assertEquals(0, state.getRounds().size());

		Round round1 = new Round();
		round1.setRoundNumber(1);
		state.appendRound(round1);
		assertEquals(1, state.getRounds().size());
		assertEquals(round1, state.getRounds().get(0));

		Round round2 = new Round();
		round2.setRoundNumber(2);
		state.appendRound(round2);
		assertEquals(2, state.getRounds().size());
		assertEquals(round1, state.getRounds().get(0));
		assertEquals(round2, state.getRounds().get(1));
	}

	@Test
	public void setGameEnded() {
		GameState state = new GameState();
		assertFalse(state.isGameEnded());
		state.setGameEnded();
		assertTrue(state.isGameEnded());
	}
}