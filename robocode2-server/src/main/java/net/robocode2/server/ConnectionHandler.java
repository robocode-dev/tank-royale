package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.ObserverHandshake;
import net.robocode2.json_schema.ServerHandshake;

public class ConnectionHandler {

	final ServerSetup setup;
	final ConnectionListener listener;
	final WebSocketObserver webSocketObserver;

	final Set<WebSocket> openConnections = Collections.synchronizedSet(new HashSet<>());
	final Map<WebSocket, BotHandshake> openBotConnections = Collections.synchronizedMap(new HashMap<>());
	final Map<WebSocket, ObserverHandshake> openObserverConnections = Collections.synchronizedMap(new HashMap<>());

	static final String MESSAGE_TYPE_FIELD = "message-type";

	final ExecutorService executorService;

	public ConnectionHandler(ServerSetup setup, ConnectionListener listener) {
		this.setup = setup;
		this.listener = listener;

		InetSocketAddress address = new InetSocketAddress(setup.getHostName(), setup.getPort());
		this.webSocketObserver = new WebSocketObserver(address);
		
		this.executorService = Executors.newCachedThreadPool();
	}

	public void start() {
		webSocketObserver.run();
	}

	public void stop() {
		shutdownAndAwaitTermination(executorService);
	}

	public Map<WebSocket, BotHandshake> getBotConnections() {
		return Collections.unmodifiableMap(openBotConnections);
	}

	public Map<WebSocket, ObserverHandshake> getObserverConnections() {
		return Collections.unmodifiableMap(openObserverConnections);
	}

	private void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				// Cancel currently executing tasks
				pool.shutdownNow();
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private class WebSocketObserver extends WebSocketServer {

		private WebSocketObserver(InetSocketAddress address) {
			super(address);
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

			if (openBotConnections.containsKey(conn)) {
				BotHandshake botHandshake = openBotConnections.remove(conn);
				executorService.submit(() -> listener.onBotLeft(botHandshake));

			} else if (openObserverConnections.containsKey(conn)) {
				ObserverHandshake observerHandshake = openObserverConnections.remove(conn);
				listener.onObserverLeft(observerHandshake);

				executorService.submit(() -> openObserverConnections.remove(conn));
			}
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

					executorService.submit(() -> listener.onBotJoined(botHandshake));

				} else if (ObserverHandshake.MessageType.OBSERVER_HANDSHAKE.toString().equalsIgnoreCase(messageType)) {
					System.out.println("Handling ObserverHandshake");

					ObserverHandshake observerHandshake = gson.fromJson(message, ObserverHandshake.class);
					openObserverConnections.put(conn, observerHandshake);

					executorService.submit(() -> listener.onObserverJoined(observerHandshake));

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
	}
}
