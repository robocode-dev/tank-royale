package net.robocode2.server;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.robocode2.schema.GameSetup;
import net.robocode2.schema.BotAddress;
import net.robocode2.schema.BotHandshake;
import net.robocode2.schema.BotIntent;
import net.robocode2.schema.ControllerHandshake;
import net.robocode2.schema.Message;
import net.robocode2.schema.ObserverHandshake;
import net.robocode2.schema.ServerHandshake;
import net.robocode2.schema.Command;
import net.robocode2.schema.StartGame;
import net.robocode2.mappers.GameSetupToGameSetupMapper;

public final class ConnHandler {

	private final ServerSetup setup;
	private final ConnListener listener;
	private final WebSocketObserver webSocketObserver;

	private final Map<String /* clientKey */, WebSocket> connections = Collections.synchronizedMap(new HashMap<>());

    private final Map<String /* clientKey */, WebSocket> botConnections = Collections.synchronizedMap(new HashMap<>());
    private final Map<String /* clientKey */, WebSocket> observerAndControllerConnections = Collections.synchronizedMap(new HashMap<>());
    private final Map<String /* clientKey */, WebSocket> controllerConnections = Collections.synchronizedMap(new HashMap<>());

    private final Map<String /* clientKey */, BotHandshake> botHandshakes = Collections.synchronizedMap(new HashMap<>());
    private final Map<String /* clientKey */, ObserverHandshake> observerHandshakes = Collections.synchronizedMap(new HashMap<>());
    private final Map<String /* clientKey */, ControllerHandshake> controllerHandshakes = Collections.synchronizedMap(new HashMap<>());

    private static final String CLIENT_KEY = "clientKey";
	private static final String TYPE = "type";

	private final ExecutorService executorService;

	ConnHandler(ServerSetup setup, ConnListener listener) {
		this.setup = setup;
		this.listener = listener;

		InetSocketAddress address = new InetSocketAddress(setup.getHostName(), setup.getPort());
		this.webSocketObserver = new WebSocketObserver(address);

		this.executorService = Executors.newCachedThreadPool();
	}

	void start() {
		webSocketObserver.run();
	}

	// TODO: Call this method
	public void stop() {
		shutdownAndAwaitTermination(executorService);
	}

	public WebSocket getConnection(String clientKey) {
		return connections.get(clientKey);
	}

	Map<String /* clientKey */, WebSocket> getBotConnections() {
		return Collections.unmodifiableMap(botConnections);
	}

	Map<String /* clientKey */, WebSocket> getObserverAndControllerConnections() {
		return Collections.unmodifiableMap(observerAndControllerConnections);
	}

	Map<String /* clientKey */, WebSocket> getControllerConnections() {
		return Collections.unmodifiableMap(controllerConnections);
	}

	Map<String /* clientKey */, BotHandshake> getBotHandshakes() {
		return Collections.unmodifiableMap(botHandshakes);
	}

	Map<String /* clientKey */, ObserverHandshake> getObserverHandshakes() {
		return Collections.unmodifiableMap(observerHandshakes);
	}

	Map<String /* clientKey */, ControllerHandshake> getControllerHandshakes() {
		return Collections.unmodifiableMap(controllerHandshakes);
	}

	Set<String> getBotKeys(Collection<BotAddress> botAddresses) {
		Set<String> foundKeys = new HashSet<>();

		for (WebSocket conn : botConnections.values()) {
			InetSocketAddress addr = conn.getRemoteSocketAddress();
			if (addr != null) {
				int port = addr.getPort();
				String hostname = addr.getHostName();

				for (BotAddress botAddr : botAddresses) {
					if (botAddr.getHost().equals(hostname) && botAddr.getPort() == port) {
						foundKeys.add(getKeyFromConnection(botConnections, conn));
						break;
					}
				}
			}
		}

		return foundKeys;
	}

	public static String getKeyFromConnection(Map<String, WebSocket> connections, WebSocket conn) {
		return connections.entrySet().stream()
				.filter(entry -> Objects.equals(entry.getValue(), conn))
				.findFirst().get().getKey();
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
		public void onStart() {}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			System.out.println("onOpen(): " + conn.getRemoteSocketAddress());

			String clientKey = Long.toHexString(System.nanoTime());
			connections.put(clientKey, conn);

			ServerHandshake hs = new ServerHandshake();
			hs.setType(ServerHandshake.Type.SERVER_HANDSHAKE);
			hs.setClientKey(clientKey);
			hs.setProtocolVersion("0.1");
			hs.setGames(GameSetupToGameSetupMapper.map(setup.getGames()));

			String msg = new Gson().toJson(hs);
			send(conn, msg);
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			System.out.println("onClose(): " + conn.getRemoteSocketAddress() + ", code: " + code + ", reason: " + reason
					+ ", remote: " + remote);

			String clientKey = getKeyFromConnection(connections, conn);
			connections.remove(clientKey);

			if (botConnections.containsKey(clientKey)) {
				botConnections.remove(clientKey);
				executorService.submit(() -> listener.onBotLeft(clientKey));

			} else if (observerAndControllerConnections.containsKey(clientKey)) {
				observerAndControllerConnections.remove(clientKey);
				executorService.submit(() -> listener.onObserverLeft(clientKey));

			} else if (controllerConnections.containsKey(clientKey)) {
				controllerConnections.remove(clientKey);
				executorService.submit(() -> listener.onControllerLeft(clientKey));
			}

			if (botHandshakes.containsKey(clientKey)) {
				botHandshakes.remove(clientKey);

			} else if (observerHandshakes.containsKey(clientKey)) {
				observerHandshakes.remove(clientKey);

			} else if (controllerHandshakes.containsKey(clientKey)) {
				controllerHandshakes.remove(clientKey);
			}
		}

		@Override
		public void onMessage(WebSocket conn, String message) {
			System.out.println("onMessage(): " + conn.getRemoteSocketAddress() + ", message: " + message);

			Gson gson = new Gson();
			try {
				JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

				JsonElement jsonType = jsonObject.get(TYPE);
				if (jsonType != null) {
					try {
						Message.Type type = Message.Type.fromValue(jsonType.getAsString());
						System.out.println("Handling message: " + type);

						JsonElement jsonClientKey = jsonObject.get(CLIENT_KEY);
						if (jsonClientKey == null) {
							System.out.println("Client key is missing in message");
							return;
						}
						String clientKeyNotFinal = jsonClientKey.getAsString();
						if (!connections.keySet().contains(clientKeyNotFinal)) {
							System.out.println("Client key not recognized, ignoring: " + clientKeyNotFinal);
							return;
						}
						final String clientKey = clientKeyNotFinal;

						switch (type) {
							case BOT_HANDSHAKE: {
								BotHandshake handshake = gson.fromJson(message, BotHandshake.class);
								botConnections.put(clientKey, conn);
								botHandshakes.put(clientKey, handshake);

								executorService.submit(() -> listener.onBotJoined(clientKey, handshake));
								break;
							}
							case OBSERVER_HANDSHAKE: {
								ObserverHandshake handshake = gson.fromJson(message, ObserverHandshake.class);
								observerAndControllerConnections.put(clientKey, conn);
								observerHandshakes.put(clientKey, handshake);

								executorService.submit(() -> listener.onObserverJoined(clientKey, handshake));
								break;
							}
							case CONTROLLER_HANDSHAKE: {
								ControllerHandshake handshake = gson.fromJson(message, ControllerHandshake.class);
								controllerConnections.put(clientKey, conn);
								controllerHandshakes.put(clientKey, handshake);
								observerAndControllerConnections.put(clientKey, conn); // controller is also an observer

								executorService.submit(() -> listener.onControllerJoined(clientKey, handshake));
								break;
							}
							case BOT_READY: {
								executorService.submit(() -> listener.onBotReady(clientKey));
								break;
							}
							case BOT_INTENT: {
								BotIntent intent = gson.fromJson(message, BotIntent.class);
								executorService.submit(() -> listener.onBotIntent(clientKey, intent));
								break;
							}
							default:
								notifyException(new IllegalStateException("Unhandled message type: " + type));
						}
					} catch (IllegalArgumentException e) {
						Command.Type type = Command.Type.fromValue(jsonType.getAsString());
						System.out.println("Handling command: " + type);

						JsonElement jsonClientKey = jsonObject.get(CLIENT_KEY);
						if (jsonClientKey != null) {
							String clientKey = jsonClientKey.getAsString();
							if (!connections.keySet().contains(clientKey)) {
								System.out.println("Client key not recognized, ignoring: " + clientKey);
							}
						} else {
							System.out.println("Client key not specified, ignoring");
						}
						switch (type) {
							case START_GAME: {
								StartGame startGame = gson.fromJson(message, StartGame.class);
								GameSetup gameSetup = startGame.getGameSetup();
								Collection<BotAddress> botAddresses = startGame.getBotAddresses();
								executorService.submit(() -> listener.onStartGame(gameSetup, botAddresses));
								break;
							}
							case STOP_GAME: {
								executorService.submit(() -> listener.onAbortGame());
								break;
							}
							case PAUSE_GAME: {
								executorService.submit(() -> listener.onPauseGame());
								break;
							}
							case RESUME_GAME: {
								executorService.submit(() -> listener.onResumeGame());
								break;
							}

							default:
								notifyException(new IllegalStateException("Unhandled command type: " + type));
						}
					}
				}
			} catch (JsonSyntaxException e2) {
				System.err.println("Invalid message: " + message);
			}
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			notifyException(ex);
		}
	}
}
