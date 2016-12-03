package net.robocode2.server;

import net.robocode2.json_schema.GameDefinition;

public final class DefaultGameDef extends GameDefinition {

	public DefaultGameDef() {
		setGameType("melee");
		setArenaWidth(1000);
		setArenaHeight(1000);
		setMinNumberOfParticipants(2);
		setMaxNumberOfParticipants(10);
		setNumberOfRounds(10);
		setTurnTimeout(100);
		setReadyTimeout(10_000);
	}
}
