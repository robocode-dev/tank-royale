package net.robocode2.model.mappers;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.json_schema.GameSetup;
import net.robocode2.model.IGameSetup;

public final class GameSetupToGameSetupMapper {

	public static GameSetup map(IGameSetup gameSetup) {
		GameSetup setup = new GameSetup();

		setup.setGameType(gameSetup.getGameType());
		setup.setArenaWidth(gameSetup.getArenaWidth());
		setup.setArenaHeight(gameSetup.getArenaHeight());
		setup.setMinNumberOfParticipants(gameSetup.getMinNumberOfParticipants());
		setup.setMaxNumberOfParticipants(gameSetup.getMaxNumberOfParticipants()); // XXX
		setup.setNumberOfRounds(gameSetup.getNumberOfRounds());
		setup.setGunCoolingRate(gameSetup.getGunCoolingRate());
		setup.setInactivityTurns(gameSetup.getInactivityTurns());
		setup.setTurnTimeout(gameSetup.getTurnTimeout());
		setup.setReadyTimeout(gameSetup.getReadyTimeout());
		setup.setDelayedObserverTurns(gameSetup.getDelayedObserverTurns());

		setup.setIsArenaWidthLocked(gameSetup.isArenaWidthLocked());
		setup.setIsArenaHeightLocked(gameSetup.isArenaHeightLocked());
		setup.setIsMinNumberOfParticipantsLocked(gameSetup.isMinNumberOfParticipantsLocked());
		setup.setIsMaxNumberOfParticipantsLocked(gameSetup.isMaxNumberOfParticipantsLocked());
		setup.setIsNumberOfRoundsLocked(gameSetup.isNumberOfRoundsLocked());
		setup.setIsGunCoolingRateLocked(gameSetup.isGunCoolingRateLocked());
		setup.setIsInactivityTurnsLocked(gameSetup.isInactiveTurnsLocked());
		setup.setIsTurnTimeoutLocked(gameSetup.isTurnTimeoutLocked());
		setup.setIsReadyTimeoutLocked(gameSetup.isReadyTimeoutLocked());
		setup.setIsDelayedObserverTurnsLocked(gameSetup.isDelayedObserverTurnsLocked());

		return setup;
	}

	public static net.robocode2.model.GameSetup map(GameSetup gameSetup) {
		net.robocode2.model.GameSetup setup = new net.robocode2.model.GameSetup();

		setup.setGameType(gameSetup.getGameType());
		setup.setArenaWidth(gameSetup.getArenaWidth());
		setup.setArenaHeight(gameSetup.getArenaHeight());
		setup.setMinNumberOfParticipants(gameSetup.getMinNumberOfParticipants());
		setup.setMaxNumberOfParticipants(gameSetup.getMaxNumberOfParticipants());
		setup.setNumberOfRounds(gameSetup.getNumberOfRounds());
		setup.setGunCoolingRate(gameSetup.getGunCoolingRate());
		setup.setInactivityTurns(gameSetup.getInactivityTurns());
		setup.setTurnTimeout(gameSetup.getTurnTimeout());
		setup.setReadyTimeout(gameSetup.getReadyTimeout());
		setup.setDelayedObserverTurns(gameSetup.getDelayedObserverTurns());

		return setup;
	}

	public static Set<GameSetup> map(Set<IGameSetup> games) {
		Set<GameSetup> mappedGames = new HashSet<>();
		for (IGameSetup gameSetup : games) {
			mappedGames.add(map(gameSetup));
		}
		return mappedGames;
	}
}
