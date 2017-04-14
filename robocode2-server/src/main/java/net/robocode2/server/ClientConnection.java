package net.robocode2.server;

import org.java_websocket.WebSocket;

public class ClientConnection<Handshake> {

	private final WebSocket connection;
	private final Handshake handshake;
	private int id;

	public ClientConnection(WebSocket connection, Handshake handshake) {
		this.connection = connection;
		this.handshake = handshake;
	}

	public WebSocket getConnection() {
		return connection;
	}

	public Handshake getHandshake() {
		return handshake;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}