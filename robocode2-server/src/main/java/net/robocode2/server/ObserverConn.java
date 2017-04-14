package net.robocode2.server;

import org.java_websocket.WebSocket;

import net.robocode2.json_schema.messages.ObserverHandshake;

public final class ObserverConn extends ClientConnection<ObserverHandshake> {

	public ObserverConn(WebSocket connection, ObserverHandshake handshake) {
		super(connection, handshake);
	}
}