package net.robocode2.server;

import org.java_websocket.WebSocket;

import net.robocode2.json_schema.messages.BotHandshake;

public final class BotConn extends ClientConnection<BotHandshake> {

	public BotConn(WebSocket connection, BotHandshake handshake) {
		super(connection, handshake);
	}
}