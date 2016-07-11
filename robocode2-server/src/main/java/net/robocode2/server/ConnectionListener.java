package net.robocode2.server;

public interface ConnectionListener {

	void onException(Exception exception);

	void onBotJoined(Bot bot);

	void onBotLeft(Bot bot);

	void onObserverJoined(Observer observer);

	void onObserverLeft(Observer observer);

	void onBotReady(Bot bot);
}
