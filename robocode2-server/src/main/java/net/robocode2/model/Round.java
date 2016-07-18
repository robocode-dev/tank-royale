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
		this.turns = new ArrayList<Turn>(turns);
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
}