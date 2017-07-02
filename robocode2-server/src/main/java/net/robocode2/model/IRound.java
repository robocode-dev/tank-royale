package net.robocode2.model;

import java.util.List;

/**
 * Round interface
 * 
 * @author Flemming N. Larsen
 */
public interface IRound {

	/** Returns the round number */
	int getRoundNumber();

	/** Returns list of turns */
	List<ITurn> getTurns();

	/** Returns flag specifying if round has ended */
	boolean isRoundEnded();

	/**
	 * Returns the last turn of this round
	 */
	default ITurn getLastTurn() {
		List<ITurn> turns = getTurns();
		int numTurns = turns.size();
		if (numTurns > 0) {
			return turns.get(numTurns - 1);
		}
		return null;
	}
}