package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.Collection;
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

import net.robocode2.json_schema.BotAddress;
import net.robocode2.json_schema.GameSetup;
import net.robocode2.json_schema.controller.Command;
import net.robocode2.json_schema.controller.StartGame;
import net.robocode2.json_schema.messages.BotHandshake;
import net.robocode2.json_schema.messages.BotIntent;
import net.robocode2.json_schema.messages.ControllerHandshake;
import net.robocode2.json_schema.messages.Message;
import net.robocode2.json_schema.messages.ObserverHandshake;
import net.robocode2.json_schema.messages.ServerHandshake;
import net.robocode2.model.mappers.GameSetupToGameSetupMapper;

public final class ConnHandler {

	private final ServerSetup setup;
	private final ConnListener listener;
	private final WebSocketObserver webSocketObserver;

	private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

	private final Map<WebSocket, BotHandshake> botConnections = Collections.synchronizedMap(new HashMap<>());
	private final Map<WebSocket, ObserverHandshake> observerConnections = Collections.synchronizedMap(new HashMap<>());
	private final Map<WebSocket, ControllerHandshake> controllerConnections = Collections.synchronizedMap(new HashMap<>());

	private static final String TYPE = "type";

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

	// TODO: Call this method
	public void stop() {
		shutdownAndAwaitTermination(executorService);
	}

	public Map<WebSocket, BotHandshake> getBotConnections() {
		return Collections.unmodifiableMap(botConnections);
	}

	public Map<WebSocket, ObserverHandshake> getObserverConnections() {
		return Collections.unmodifiableMap(observerConnections);
	}

	public Map<WebSocket, ControllerHandshake> getControllerConnections() {
		return Collections.unmodifiableMap(controllerConnections);
	}

	public Set<WebSocket> getBotConnections(Collection<BotAddress> botAddresses) {
		Set<WebSocket> foundConnections = new HashSet<>();

		if (botConnections != null) {
			for (WebSocket conn : botConnections.keySet()) {
				InetSocketAddress addr = conn.getRemoteSocketAddress();
				if (addr != null) {
					int port = addr.getPort();
					String hostname = addr.getHostName();

					for (BotAddress botAddr : botAddresses) {
						if (botAddr.getHost().equals(hostname) && botAddr.getPort() == port) {
							foundConnections.add(conn);
							break;
						}
					}
				}
			}
		}

		return foundConnections;
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

		conn.send(message);
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
			hs.setType(ServerHandshake.Type.SERVER_HANDSHAKE);
			hs.setGames(GameSetupToGameSetupMapper.map(setup.getGames()));

			String msg = new Gson().toJson(hs);
			send(conn, msg);
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			System.out.println("onClose(): " + conn.getRemoteSocketAddress() + ", code: " + code + ", reason: " + reason
					+ ", remote: " + remote);

			connections.remove(conn);

			if (botConnections.containsKey(conn)) {
				botConnections.remove(conn);
				executorService.submit(() -> listener.onBotLeft(conn));

			} else if (observerConnections.containsKey(conn)) {
				observerConnections.remove(conn);
				executorService.submit(() -> listener.onObserverLeft(conn));

			} else if (controllerConnections.containsKey(conn)) {
				controllerConnections.remove(conn);
				executorService.submit(() -> listener.onControllerLeft(conn));
			}
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			System.out.println("onMessage(): " + conn.getRemoteSocketAddress() + ", message: " + message);

			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

			JsonElement jsonElement = jsonObject.get(TYPE);
			if (jsonElement != null) {
				try {
					final Message.Type type = Message.Type.fromValue(jsonElement.getAsString());

					System.out.println("Handling message: " + type);

					switch (type) {
					case BOT_HANDSHAKE: {
						BotHandshake handshake = gson.fromJson(message, BotHandshake.class);
						botConnections.put(conn, handshake);

						executorService.submit(() -> listener.onBotJoined(conn, handshake));
						break;
					}
					case OBSERVER_HANDSHAKE: {
						ObserverHandshake handshake = gson.fromJson(message, ObserverHandshake.class);
						observerConnections.put(conn, handshake);

						executorService.submit(() -> listener.onObserverJoined(conn, handshake));
						break;
					}
					case CONTROLLER_HANDSHAKE: {
						ControllerHandshake handshake = gson.fromJson(message, ControllerHandshake.class);
						controllerConnections.put(conn, handshake);

						executorService.submit(() -> listener.onControllerJoined(conn, handshake));
						break;
					}
					case BOT_READY: {
						BotHandshake handshake = botConnections.get(conn);
						if (handshake != null) {
							executorService.submit(() -> listener.onBotReady(conn));
						}
						break;
					}
					case BOT_INTENT: {
						BotHandshake handshake = botConnections.get(conn);
						if (handshake != null) {
							BotIntent intent = gson.fromJson(message, BotIntent.class);
							executorService.submit(() -> listener.onBotIntent(conn, intent));
						}
						break;
					}
					default:
						notifyException(new IllegalStateException("Unhandled message type: " + type));
					}
				} catch (IllegalArgumentException e) {
					final Command.Type type = Command.Type.fromValue(jsonElement.getAsString());

					System.out.println("Handling command: " + type);

					switch (type) {
					case START_GAME: {
						ControllerHandshake handshake = controllerConnections.get(conn);
						StartGame startGame = gson.fromJson(message, StartGame.class);
						GameSetup gameSetup = startGame.getGameSetup();
						Collection<BotAddress> botAddresses = startGame.getBotAddresses();
						if (handshake != null) {
							executorService.submit(() -> listener.onStartGame(conn, gameSetup, botAddresses));
						}
						break;
					}
					case STOP_GAME: {
						ControllerHandshake handshake = controllerConnections.get(conn);
						if (handshake != null) {
							executorService.submit(() -> listener.onStopGame(conn));
						}
						break;
					}
					case PAUSE_GAME: {
						ControllerHandshake handshake = controllerConnections.get(conn);
						if (handshake != null) {
							executorService.submit(() -> listener.onPauseGame(conn));
						}
						break;
					}
					case RESUME_GAME: {
						ControllerHandshake handshake = controllerConnections.get(conn);
						if (handshake != null) {
							executorService.submit(() -> listener.onResumeGame(conn));
						}
						break;
					}

					default:
						notifyException(new IllegalStateException("Unhandled command type: " + type));
					}
				}
			}
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			notifyException(ex);
		}
	}
}
