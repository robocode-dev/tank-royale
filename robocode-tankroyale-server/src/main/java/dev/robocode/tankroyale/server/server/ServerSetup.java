package dev.robocode.tankroyale.server.server;

import java.util.HashSet;
import java.util.Set;

import dev.robocode.tankroyale.server.model.GameSetup;

final class ServerSetup {

	Set<GameSetup> getGames() {
		Set<GameSetup> games = new HashSet<>();

		GameSetup setup1 = GameSetup.builder()
			.gameType("melee")
			.minNumberOfParticipants(2)
			.minNumberOfParticipantsLocked(true)
			.build();
		games.add(setup1);

		GameSetup setup2 = GameSetup.builder()	
			.gameType("1v1")
			.minNumberOfParticipants(2)
			.minNumberOfParticipantsLocked(true)
			.maxNumberOfParticipants(2)
			.maxNumberOfParticipantsLocked(true)
			.build();
		games.add(setup2);

		return games;
	}
}
