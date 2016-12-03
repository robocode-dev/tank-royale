package net.robocode2.server;

public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(BotConn bot);

	void onBotLeft(BotConn bot);

	void onObserverJoined(ObserverConn observer);

	void onObserverLeft(ObserverConn observer);

	void onBotReady(BotConn bot);
}
