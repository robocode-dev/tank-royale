package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class ImmutableRoundTest {

	static Round initializedRound;

	@BeforeClass
	public static void initialize() {
		Round round = new Round();

		round.setRoundNumber(7913);
		round.setRoundEnded();

		Turn turn1 = new Turn();
		turn1.setTurnNumber(1);

		Turn turn2 = new Turn();
		turn2.setTurnNumber(2);

		List<ITurn> turns = round.getTurns();
		turns.add(turn1);
		turns.add(turn2);

		initializedRound = round;
	}

	@Test
	public void constructorParams() {
		ImmutableRound round = new ImmutableRound(initializedRound.getRoundNumber(), initializedRound.getTurns(),
				initializedRound.isRoundEnded());

		assertEquals(initializedRound.getRoundNumber(), round.getRoundNumber());
		assertEquals(initializedRound.getTurns(), round.getTurns());
		assertEquals(initializedRound.isRoundEnded(), round.isRoundEnded());
	}

	@Test
	public void unmodifiableTurns() {
		ImmutableRound round = new ImmutableRound(initializedRound.getRoundNumber(), initializedRound.getTurns(),
				initializedRound.isRoundEnded());
		try {
			round.getTurns().clear();
			fail();
		} catch (RuntimeException e) {
		}
	}
}
