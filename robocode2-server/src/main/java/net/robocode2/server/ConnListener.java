package net.robocode2.server;

import java.util.Collection;

import net.robocode2.schema.GameSetup;
import net.robocode2.schema.BotAddress;
import net.robocode2.schema.BotIntent;
import net.robocode2.schema.ControllerHandshake;
import net.robocode2.schema.ObserverHandshake;


public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(String clientKey, net.robocode2.schema.BotHandshake handshake);

	void onBotLeft(String clientKey);

	void onObserverJoined(String clientKey, ObserverHandshake handshake);

	void onObserverLeft(String clientKey);

	void onControllerJoined(String clientKey, ControllerHandshake handshake);

	void onControllerLeft(String clientKey);

	void onBotReady(String clientKey);

	void onBotIntent(String clientKey, BotIntent intent);

	void onStartGame(GameSetup gameSetup, Collection<BotAddress> botAddresses);

	void onAbortGame();

	void onPauseGame();

	void onResumeGame();
}
