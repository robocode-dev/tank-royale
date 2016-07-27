package net.robocode2.game;

import net.robocode2.model.Setup;

public final class MainLoop {

	MainLoopListener listener;

	public MainLoop(MainLoopListener listener) {
		this.listener = listener;
	}

	public void start(Setup setups) {
	}

	public void pause() {
	}

	public void resume() {
	}

	public void stop() {
	}

	private void nextTurn() {
		// Get move from intents

		// update world
	}
}