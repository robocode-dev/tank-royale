package net.robocode2.server;

import org.java_websocket.WebSocket;

import net.robocode2.json_schema.messages.ControllerHandshake;

public final class ControllerConn extends ClientConnection<ControllerHandshake> {

	public ControllerConn(WebSocket connection, ControllerHandshake handshake) {
		super(connection, handshake);
	}
}