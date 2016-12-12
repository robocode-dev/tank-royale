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

		GameSetup.Builder builder = new GameSetup.Builder();
		builder.setGameType("melee");

		games.add(builder.build());

		builder = new GameSetup.Builder();
		builder.setGameType("1v1");
		builder.setMinNumberOfParticipants(2);
		builder.setMaxNumberOfParticipants(2);

		games.add(builder.build());

		return games;
	}
}
