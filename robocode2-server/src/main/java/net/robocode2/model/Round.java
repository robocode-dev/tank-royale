package net.robocode2.model;

import java.util.ArrayList;
import java.util.List;

public final class Round implements IRound {

	private int roundNumber;
	private final List<ITurn> turns = new ArrayList<>();
	private boolean roundEnded;

	public Round() {
	}

	public ImmutableRound toImmutableRound() {
		return new ImmutableRound(roundNumber, turns, roundEnded);
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

	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
	}

	public void appendTurn(ITurn turn) {
		turns.add(turn);
	}
}