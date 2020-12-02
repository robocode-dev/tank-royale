package dev.robocode.tankroyale.server.server;

import java.util.Collection;

import dev.robocode.tankroyale.schema.*;
import org.java_websocket.WebSocket;


@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused"})
public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(WebSocket conn, BotHandshake handshake);

	void onBotLeft(WebSocket conn, BotHandshake handshake);

	void onObserverJoined(WebSocket conn, ObserverHandshake handshake);

	void onObserverLeft(WebSocket conn, ObserverHandshake handshake);

	void onControllerJoined(WebSocket conn, ControllerHandshake handshake);

	void onControllerLeft(WebSocket conn, ControllerHandshake handshake);

	void onBotReady(WebSocket conn);

	void onBotIntent(WebSocket conn, BotIntent intent);

	void onStartGame(GameSetup gameSetup, Collection<BotAddress> botAddresses);

	void onAbortGame();

	void onPauseGame();

	void onResumeGame();

	void onChangeTps(int tps);
}
