package dev.robocode.tankroyale.server.model;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * State of a game round.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public final class Round {

	/** Round number */
	int roundNumber;

	/** List of turns */
	@Singular List<Turn> turns;

	/** Flag specifying if round has ended */
	boolean roundEnded;
	
	/**
	 * Returns the last turn of this round
	 */
	public Turn getLastTurn() {
		int numTurns = turns.size();
		if (numTurns > 0) {
			return turns.get(numTurns - 1);
		}
		return null;
	}
}