package net.robocode2.server;

import java.util.Collection;

import net.robocode2.schema.GameSetup;
import net.robocode2.schema.BotAddress;
import net.robocode2.schema.BotIntent;
import net.robocode2.schema.ControllerHandshake;
import net.robocode2.schema.ObserverHandshake;
import org.java_websocket.WebSocket;


public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(WebSocket conn, net.robocode2.schema.BotHandshake handshake);

	void onBotLeft(WebSocket conn);

	void onObserverJoined(WebSocket conn, ObserverHandshake handshake);

	void onObserverLeft(WebSocket conn);

	void onControllerJoined(WebSocket conn, ControllerHandshake handshake);

	void onControllerLeft(WebSocket conn);

	void onBotReady(WebSocket conn);

	void onBotIntent(WebSocket conn, BotIntent intent);

	void onStartGame(GameSetup gameSetup, Collection<BotAddress> botAddresses);

	void onAbortGame();

	void onPauseGame();

	void onResumeGame();
}
