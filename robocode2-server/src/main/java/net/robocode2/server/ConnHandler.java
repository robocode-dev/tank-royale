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

import net.robocode2.json_schema.messages.BotHandshake;
import net.robocode2.json_schema.messages.BotReady;
import net.robocode2.json_schema.messages.ObserverHandshake;
import net.robocode2.json_schema.messages.ServerHandshake;

public final class ConnHandler {

	private final ServerSetup setup;
	private final ConnListener listener;
	private final WebSocketObserver webSocketObserver;

	private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());
	private final Map<WebSocket, BotHandshake> bots = Collections.synchronizedMap(new HashMap<>());
	private final Map<WebSocket, ObserverHandshake> observers = Collections.synchronizedMap(new HashMap<>());

	private static final String MESSAGE_TYPE_FIELD = "message-type";

	private final ExecutorService executorService;

	public ConnHandler(ServerSetup setup, ConnListener listener) {
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
		return Collections.unmodifiableMap(bots);
	}

	public Map<WebSocket, ObserverHandshake> getObserverConnections() {
		return Collections.unmodifiableMap(observers);
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

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);
	}

	private void notifyException(Exception exception) {
		executorService.submit(() -> listener.onException(exception));
	}

	private class WebSocketObserver extends WebSocketServer {

		private WebSocketObserver(InetSocketAddress address) {
			super(address);
		}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			System.out.println("onOpen(): " + conn.getRemoteSocketAddress());

			connections.add(conn);

			ServerHandshake hs = new ServerHandshake();
			hs.setMessageType(ServerHandshake.MessageType.SERVER_HANDSHAKE);
			hs.setGames(setup.getGames());

			String msg = new Gson().toJson(hs);
			send(conn, msg);
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			System.out.println("onClose(): " + conn.getRemoteSocketAddress() + ", code: " + code + ", reason: " + reason
					+ ", remote: " + remote);

			connections.remove(conn);

			if (bots.containsKey(conn)) {
				BotHandshake handshake = bots.remove(conn);
				executorService.submit(() -> listener.onBotLeft(new BotConn(conn, handshake)));

			} else if (observers.containsKey(conn)) {
				ObserverHandshake handshake = observers.remove(conn);
				executorService.submit(() -> listener.onObserverLeft(new ObserverConn(conn, handshake)));
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

					BotHandshake handshake = gson.fromJson(message, BotHandshake.class);
					bots.put(conn, handshake);

					executorService.submit(() -> listener.onBotJoined(new BotConn(conn, handshake)));

				} else if (ObserverHandshake.MessageType.OBSERVER_HANDSHAKE.toString().equalsIgnoreCase(messageType)) {
					System.out.println("Handling ObserverHandshake");

					ObserverHandshake handshake = gson.fromJson(message, ObserverHandshake.class);
					observers.put(conn, handshake);

					executorService.submit(() -> listener.onObserverJoined(new ObserverConn(conn, handshake)));

				} else if (BotReady.MessageType.BOT_READY.toString().equalsIgnoreCase(messageType)) {
					System.out.println("Handling BotReady");

					BotHandshake handshake = bots.get(conn);
					executorService.submit(() -> listener.onBotReady(new BotConn(conn, handshake)));

				} else {
					notifyException(new IllegalStateException("Unhandled message type: " + messageType));
				}
			}
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			notifyException(ex);
		}
	}
}
