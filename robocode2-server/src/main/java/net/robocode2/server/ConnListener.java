package net.robocode2.server;

import java.util.Collection;

import org.java_websocket.WebSocket;

import net.robocode2.json_schema.GameSetup;
import net.robocode2.json_schema.comm.BotAddress;
import net.robocode2.json_schema.comm.BotIntent;
import net.robocode2.json_schema.comm.ControllerHandshake;
import net.robocode2.json_schema.comm.ObserverHandshake;


public interface ConnListener {

	void onException(Exception exception);

	void onBotJoined(WebSocket socket, net.robocode2.json_schema.comm.BotHandshake handshake);

	void onBotLeft(WebSocket socket);

	void onObserverJoined(WebSocket socket, ObserverHandshake handshake);

	void onObserverLeft(WebSocket socket);

	void onControllerJoined(WebSocket socket, ControllerHandshake handshake);

	void onControllerLeft(WebSocket socket);

	void onBotReady(WebSocket socket);

	void onBotIntent(WebSocket socket, BotIntent intent);

	void onStartGame(WebSocket socket, GameSetup gameSetup, Collection<BotAddress> botAddresses);

	void onStopGame(WebSocket socket);

	void onPauseGame(WebSocket socket);

	void onResumeGame(WebSocket socket);
}
