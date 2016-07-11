package net.robocode2.server;

import org.java_websocket.WebSocket;

import net.robocode2.json_schema.BotHandshake;

public class Bot {

	private final WebSocket connection;
	private final BotHandshake handshake;
	private int id;

	public Bot(WebSocket connection, BotHandshake handshake) {
		this.connection = connection;
		this.handshake = handshake;			
	}

	public WebSocket getConnection() {
		return connection;
	}

	public BotHandshake getHandshake() {
		return handshake;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}