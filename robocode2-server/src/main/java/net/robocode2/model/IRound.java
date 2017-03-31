package net.robocode2.model;

import java.util.List;

public interface IRound {

	int getRoundNumber();

	List<ITurn> getTurns();

	boolean isRoundEnded();

	default ITurn getLastTurn() {
		List<ITurn> turns = getTurns();
		int numTurns = turns.size();
		if (numTurns > 0) {
			return turns.get(numTurns - 1);
		}
		return null;
	}
}