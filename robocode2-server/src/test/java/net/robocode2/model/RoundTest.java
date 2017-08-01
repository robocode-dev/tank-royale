package net.robocode2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class RoundTest {

	@Test
	public void setRoundNumber() {
		Round round = new Round();
		round.setRoundNumber(7913);
		assertEquals(7913, round.getRoundNumber());
	}

	@Test
	public void appendTurn() {
		Round round = new Round();
		List<ITurn> turns = round.getTurns();

		Turn turn1 = new Turn();
		turn1.setTurnNumber(1);
		turns.add(turn1);

		assertEquals(1, turns.size());

		Turn turn2 = new Turn();
		turn2.setTurnNumber(2);
		turns.add(turn2);

		assertEquals(2, turns.size());
	}

	@Test
	public void setRoundEnded() {
		Round round = new Round();
		assertFalse(round.isRoundEnded());
		round.setRoundEnded();
		assertTrue(round.isRoundEnded());
	}
}