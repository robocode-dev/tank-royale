package net.robocode2.server;

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.ObserverHandshake;

public class GameServer {

	ServerSetup setup;
	ConnectionListener connectionObserver;
	ConnectionHandler connectionHandler;		

	public GameServer() {
		this.setup = new ServerSetup();
		this.connectionObserver = new ConnectionObserver();
		this.connectionHandler = new ConnectionHandler(setup, connectionObserver);		
	}

	public void run() {
		connectionHandler.run();
	}

	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.run();
	}

	private class ConnectionObserver implements ConnectionListener {

		@Override
		public void onBotJoined(BotHandshake botHandshake) {
		}

		@Override
		public void onObserverJoined(ObserverHandshake observerHandshake) {
		}

		@Override
		public void onBotLeft(BotHandshake botHandshake) {
		}

		@Override
		public void onObserverLeft(ObserverHandshake observerHandshake) {
		}
	}
}
