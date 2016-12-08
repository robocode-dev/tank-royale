package net.robocode2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Round {

	private final int roundNumber;
	private final List<Turn> turns;
	private final boolean roundEnded;

	public Round(int roundNumber, List<Turn> turns, boolean roundEnded) {
		this.roundNumber = roundNumber;
		if (turns == null) {
			this.turns = new ArrayList<>();
		} else {
			this.turns = new ArrayList<>(turns);
		}
		this.roundEnded = roundEnded;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public List<Turn> getTurns() {
		return Collections.unmodifiableList(turns);
	}

	public Turn getLastTurn() {
		int numTurns = turns.size();
		if (numTurns > 0) {
			return turns.get(numTurns - 1);
		}
		return null;
	}

	public boolean isRoundEnded() {
		return roundEnded;
	}

	public static final class Builder {
		private int roundNumber;
		private List<Turn> turns = new ArrayList<>();
		private boolean roundEnded;

		public Round build() {
			return new Round(roundNumber, turns, roundEnded);
		}

		public Builder setRoundNumber(int roundNumber) {
			this.roundNumber = roundNumber;
			return this;
		}

		public Builder setTurns(List<Turn> turns) {
			this.turns = new ArrayList<>(turns);
			return this;
		}

		public Builder setRoundEnded(boolean roundEnded) {
			this.roundEnded = roundEnded;
			return this;
		}

		public Builder appendTurn(Turn turn) {
			turns.add(turn);
			return this;
		}
	}
}