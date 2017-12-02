package net.robocode2.server;

import java.util.HashSet;
import java.util.Set;

import net.robocode2.model.GameSetup;
import net.robocode2.model.IGameSetup;

public final class ServerSetup {

	public String getHostName() {
		return "localhost";
	}

	public int getPort() {
		return 50000;
	}

	public Set<IGameSetup> getGames() {
		Set<IGameSetup> games = new HashSet<>();

		GameSetup setup = new GameSetup();
		setup.setGameType("melee");
		setup.setMinNumberOfParticipants(2);
		setup.setMinNumberOfParticipantsLocked(true);

		games.add(setup.toImmutableGameSetup());

		setup = new GameSetup();
		setup.setGameType("1v1");
		setup.setMinNumberOfParticipants(2);
		setup.setMinNumberOfParticipantsLocked(true);
		setup.setMaxNumberOfParticipants(2);
		setup.setMaxNumberOfParticipantsLocked(true);

		games.add(setup.toImmutableGameSetup());

		return games;
	}
}
