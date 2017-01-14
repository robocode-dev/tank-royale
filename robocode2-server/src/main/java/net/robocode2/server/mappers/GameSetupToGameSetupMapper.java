package net.robocode2.server.mappers;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.json_schema.GameSetup;

public final class GameSetupToGameSetupMapper {

	public static GameSetup map(net.robocode2.model.GameSetup gameSetup) {
		GameSetup setup = new GameSetup();
		setup.setGameType(gameSetup.getGameType());
		setup.setArenaWidth(gameSetup.getArenaWidth());
		setup.setArenaHeight(gameSetup.getArenaHeight());
		setup.setMinNumberOfParticipants(gameSetup.getMinNumberOfParticipants());
		setup.setMaxNumberOfParticipants(gameSetup.getMaxNumberOfParticipants());
		setup.setNumberOfRounds(gameSetup.getNumberOfRounds());
		setup.setGunCoolingRate(gameSetup.getGunCoolingRate());
		setup.setInactivityTurns(gameSetup.getInactiveTurns());
		setup.setTurnTimeout(gameSetup.getTurnTimeout());
		setup.setReadyTimeout(gameSetup.getReadyTimeout());
		setup.setNumberOfTurnsDelayedForObservers(gameSetup.getNumberOfDelayedTurnsForObservers());
		return setup;
	}

	public static Set<GameSetup> map(Set<net.robocode2.model.GameSetup> games) {
		Set<GameSetup> mappedGames = new HashSet<>();
		for (net.robocode2.model.GameSetup gameSetup : games) {
			mappedGames.add(map(gameSetup));
		}
		return mappedGames;
	}
}
