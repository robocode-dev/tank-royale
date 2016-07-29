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
		this.turns = new ArrayList<>(turns);
		this.roundEnded = roundEnded;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public List<Turn> getTurns() {
		return Collections.unmodifiableList(turns);
	}

	public boolean isRoundEnded() {
		return roundEnded;
	}

	public static final class RoundBuilder {
		private int roundNumber;
		private List<Turn> turns = new ArrayList<>();
		private boolean roundEnded;

		public Round build() {
			return new Round(roundNumber, turns, roundEnded);
		}

		public RoundBuilder setRoundNumber(int roundNumber) {
			this.roundNumber = roundNumber;
			return this;
		}

		public RoundBuilder setTurns(List<Turn> turns) {
			this.turns = new ArrayList<>(turns);
			return this;
		}

		public RoundBuilder setRoundEnded(boolean roundEnded) {
			this.roundEnded = roundEnded;
			return this;
		}

		public RoundBuilder appendTurn(Turn turn) {
			turns.add(turn);
			return this;
		}
	}
}