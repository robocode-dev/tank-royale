package net.robocode2.server;

public enum GameState {

	// Pending, waiting for enough players to join
	PENDING,
	// Game type has been sent, waiting for ready signal from players
	READY,
	// Game is running
	RUNNING,
	// Game is paused
	PAUSED,
	// Game has been stopped (by user)
	STOPPED,
	// Game has finished
	FINISHED
}
