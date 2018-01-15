package net.robocode2.model;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 * Game state.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public class GameState {

	/** Arena */
	Arena arena;

	/** List of rounds */
	@Singular List<Round> rounds;

	/** Flag specifying if game has ended */
	boolean gameEnded;
	
	/**
	 * Returns the last round.
	 */
	public Round getLastRound() {
		return (rounds == null || rounds.isEmpty()) ? null : rounds.get(rounds.size() - 1);
	}
}