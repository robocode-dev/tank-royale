package net.robocode2.server;

public enum GameState {

	// Waiting for enough participant bots to join
	WAIT_FOR_PARTICIPANTS_TO_JOIN,
	// Game type has been sent, waiting for ready signal from players
	WAIT_FOR_READY_PARTICIPANTS,
	// Game is running
	RUNNING,
	// Game is paused
	PAUSED,
	// Game has been stopped (by user)
	STOPPED,
	// Game has finished
	FINISHED
}
