package dev.robocode.tankroyale.server.server;

import java.util.Collection;

import dev.robocode.tankroyale.schema.GameSetup;
import dev.robocode.tankroyale.schema.BotAddress;
import dev.robocode.tankroyale.schema.BotIntent;
import dev.robocode.tankroyale.schema.ControllerHandshake;
import dev.robocode.tankroyale.schema.ObserverHandshake;
import org.java_websocket.WebSocket;


@SuppressWarnings({"WeakerAccess", "EmptyMethod", "unused"})
public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(WebSocket conn, dev.robocode.tankroyale.schema.BotHandshake handshake);

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
