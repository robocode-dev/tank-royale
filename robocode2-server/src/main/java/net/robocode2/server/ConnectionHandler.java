package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.robocode2.json_schema.Game;
import net.robocode2.json_schema.ServerHandshake;
import net.robocode2.server.config.DefaultGame;

public class ConnectionHandler extends WebSocketServer {

	Set<WebSocket> openConnections = new HashSet<>();

	public ConnectionHandler(InetSocketAddress address) {
		super(address);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("onOpen(): " + conn.getRemoteSocketAddress());

		openConnections.add(conn);

		ServerHandshake hs = new ServerHandshake();

		Set<Game> games = new HashSet<>();

		Game game1 = new DefaultGame();
		game1.setGameType("melee");
		games.add(game1);

		Game game2 = new DefaultGame();
		game2.setGameType("1v1");
		game2.setMinNumberOfParticipants(2);
		game2.setMinNumberOfParticipants(2);

		games.add(game2);

		hs.setGames(games);

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		String msg = gson.toJson(hs);

		conn.send(msg);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("onClose(): " + conn.getRemoteSocketAddress() + ", code: " + code + ", reason: " + reason
				+ ", remote: " + remote);

		openConnections.remove(conn);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("onMessage(): " + conn.getRemoteSocketAddress() + ", message: " + message);
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
		 String host = "localhost";
		 int port = 50000;
		 InetSocketAddress address = new InetSocketAddress(host, port);
		 ConnectionHandler server = new ConnectionHandler(address);
		 server.run();
	}
}
