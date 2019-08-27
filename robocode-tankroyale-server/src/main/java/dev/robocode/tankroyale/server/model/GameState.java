package dev.robocode.tankroyale.server.model;

import java.util.List;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Game state.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
@SuppressWarnings("unused")
public class GameState {

	/** Arena */
	@NonNull Arena arena;

	/** List of rounds */
	List<Round> rounds;

	/** Flag specifying if game has ended */
	boolean gameEnded;
	
	/**
	 * Returns the last round.
	 */
	public Round getLastRound() {
		return (rounds == null || rounds.isEmpty()) ? null : rounds.get(rounds.size() - 1);
	}
}