package net.robocode2.server;

import java.util.Collection;

import org.java_websocket.WebSocket;

import net.robocode2.json_schema.BotAddress;
import net.robocode2.json_schema.GameSetup;
import net.robocode2.json_schema.messages.BotHandshake;
import net.robocode2.json_schema.messages.BotIntent;
import net.robocode2.json_schema.messages.ControllerHandshake;
import net.robocode2.json_schema.messages.ObserverHandshake;

public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(WebSocket socket, BotHandshake handshake);

	void onBotLeft(WebSocket socket);

	void onObserverJoined(WebSocket socket, ObserverHandshake handshake);

	void onObserverLeft(WebSocket socket);

	void onControllerJoined(WebSocket socket, ControllerHandshake handshake);

	void onControllerLeft(WebSocket socket);

	void onBotReady(WebSocket socket);

	void onBotIntent(WebSocket socket, BotIntent intent);

	void onListBots(WebSocket socket, Collection<String> gameTypes);

	void onListGameTypes(WebSocket socket);

	void onStartGame(WebSocket socket, GameSetup gameSetup, Collection<BotAddress> botAddresses);
}
