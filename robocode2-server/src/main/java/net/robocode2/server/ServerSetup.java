package net.robocode2.server;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.model.GameSetup;
import net.robocode2.model.ImmutableGameSetup;

public final class ServerSetup {

	public String getHostName() {
		return "localhost";
	}

	public int getPort() {
		return 50000;
	}

	public Set<ImmutableGameSetup> getGames() {
		Set<ImmutableGameSetup> games = new HashSet<>();

		GameSetup setup = new GameSetup();
		setup.setGameType("melee");

		games.add(setup.toImmutableGameSetup());

		setup = new GameSetup();
		setup.setGameType("1v1");
		setup.setMinNumberOfParticipants(2);
		setup.setMaxNumberOfParticipants(2);

		games.add(setup.toImmutableGameSetup());

		return games;
	}
}
