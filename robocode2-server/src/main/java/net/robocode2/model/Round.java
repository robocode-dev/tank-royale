package net.robocode2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable round
 * 
 * @author Flemming N. Larsen
 */
public final class Round implements IRound {

	/** Round number */
	private int roundNumber;
	/** List of turns */
	private final List<ITurn> turns = new ArrayList<>();
	/** Flag specifying if round has ended */
	private boolean roundEnded;

	/**
	 * Creates a immutable round instance that is a copy of this round.
	 * 
	 * @return a immutable round instance
	 */
	public ImmutableRound toImmutableRound() {
		return new ImmutableRound(this);
	}

	@Override
	public int getRoundNumber() {
		return roundNumber;
	}

	@Override
	public List<ITurn> getTurns() {
		return turns;
	}

	@Override
	public boolean isRoundEnded() {
		return roundEnded;
	}

	/**
	 * Sets the round number
	 * 
	 * @param roundNumber
	 *            is the round number
	 */
	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
	}

	/**
	 * Appends a turn to this round
	 * 
	 * @param turn
	 *            is the turn to append
	 */
	public void appendTurn(ITurn turn) {
		turns.add(turn);
	}
}