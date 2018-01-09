package net.robocode2.server;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.model.GameSetup;

public final class ServerSetup {

	public String getHostName() {
		return "localhost";
	}

	public int getPort() {
		return 50000;
	}

	public Set<GameSetup> getGames() {
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
