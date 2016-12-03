package net.robocode2.server;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.json_schema.GameDefinition;

public final class ServerSetup {

	public String getHostName() {
		return "localhost";
	}

	public int getPort() {
		return 50000;
	}

	public Set<GameDefinition> getGames() {
		Set<GameDefinition> games = new HashSet<>();

		GameDefinition game1 = new DefaultGameDef();
		game1.setGameType("melee");
		games.add(game1);

		GameDefinition game2 = new DefaultGameDef();
		game2.setGameType("1v1");
		game2.setMinNumberOfParticipants(2);
		game2.setMaxNumberOfParticipants(2);

		games.add(game2);

		return games;
	}
}
