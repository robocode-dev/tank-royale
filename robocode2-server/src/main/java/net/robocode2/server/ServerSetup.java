package net.robocode2.server;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.json_schema.Game;

public final class ServerSetup {

	public String getHostName() {
		return "localhost";
	}

	public int getPort() {
		return 50000;
	}

	public Set<Game> getGames() {
		Set<Game> games = new HashSet<>();

		Game game1 = new DefaultGame();
		game1.setGameType("melee");
		games.add(game1);

		Game game2 = new DefaultGame();
		game2.setGameType("1v1");
		game2.setMinNumberOfParticipants(2);
		game2.setMinNumberOfParticipants(2);

		games.add(game2);

		return games;
	}
}
