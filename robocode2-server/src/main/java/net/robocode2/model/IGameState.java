package net.robocode2.model;

import java.util.List;

/**
 * Game state interface
 * 
 * @author Flemming N. Larsen
 */
interface IGameState {

	/** Returns the arena */
	Arena getArena();

	/** Returns the list of rounds */
	List<IRound> getRounds();

	/** Returns the flag specifying if the game has ended */
	boolean isGameEnded();

	/**
	 * Returns the last round appended to this game state
	 * 
	 * @return A round instance, if at least one game state has been appended; null otherwise
	 */
	default IRound getLastRound() {
		List<IRound> rounds = getRounds();
		int numRounds = rounds.size();
		if (numRounds > 0) {
			return rounds.get(numRounds - 1);
		}
		return null;
	}
}