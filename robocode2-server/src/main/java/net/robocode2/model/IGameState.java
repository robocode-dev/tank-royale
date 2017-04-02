package net.robocode2.model;

import java.util.List;

interface IGameState {

	Arena getArena();

	List<IRound> getRounds();

	boolean isGameEnded();

	default IRound getLastRound() {
		List<IRound> rounds = getRounds();
		int numRounds = rounds.size();
		if (numRounds > 0) {
			return rounds.get(numRounds - 1);
		}
		return null;
	}
}