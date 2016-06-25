package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.ObserverHandshake;
import net.robocode2.json_schema.ServerHandshake;

public class ConnectionHandler extends WebSocketServer {

	final ServerSetup setup;

	Set<WebSocket> openConnections = new HashSet<>();
	Map<WebSocket, BotHandshake> openBotConnections = new HashMap<>();
	Map<WebSocket, ObserverHandshake> openObserverConnections = new HashMap<>();

	static final String MESSAGE_TYPE_FIELD = "message-type";

	public ConnectionHandler(ServerSetup setup) {
		super(new InetSocketAddress(setup.getHostName(), setup.getPort()));
		this.setup = setup;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("onOpen(): " + conn.getRemoteSocketAddress());

		openConnections.add(conn);

		ServerHandshake hs = new ServerHandshake();
		hs.setMessageType(ServerHandshake.MessageType.SERVER_HANDSHAKE);
		hs.setGames(setup.getGames());

		String msg = new Gson().toJson(hs);
		conn.send(msg);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("onClose(): " + conn.getRemoteSocketAddress() + ", code: " + code + ", reason: " + reason
				+ ", remote: " + remote);

		openConnections.remove(conn);
		openBotConnections.remove(conn);
		openObserverConnections.remove(conn);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("onMessage(): " + conn.getRemoteSocketAddress() + ", message: " + message);

		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

		JsonElement jsonElement = jsonObject.get(MESSAGE_TYPE_FIELD);
		if (jsonElement != null) {
			String messageType = jsonElement.getAsString();

			if (BotHandshake.MessageType.BOT_HANDSHAKE.toString().equalsIgnoreCase(messageType)) {
				System.out.println("Handling BotHandshake");

				BotHandshake botHandshake = gson.fromJson(message, BotHandshake.class);
				openBotConnections.put(conn, botHandshake);

			} else if (ObserverHandshake.MessageType.OBSERVER_HANDSHAKE.toString().equalsIgnoreCase(messageType)) {
				System.out.println("Handling ObserverHandshake");

				ObserverHandshake observerHandshake = gson.fromJson(message, ObserverHandshake.class);
				openObserverConnections.put(conn, observerHandshake);

			} else {
				throw new IllegalStateException("Unhandled message type: " + messageType);
			}
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		if (conn == null) {
			System.err.println("onError(): exeption: " + ex);
		} else {
			System.err.println("onError(): " + conn.getRemoteSocketAddress() + ", exeption: " + ex);
		}
	}

	public static void main(String[] args) {
		ServerSetup setup = new ServerSetup();
		ConnectionHandler server = new ConnectionHandler(setup);
		server.run();
	}
}
