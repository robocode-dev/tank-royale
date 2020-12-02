package dev.robocode.tankroyale.server.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.robocode.tankroyale.schema.*;
import dev.robocode.tankroyale.server.Server;
import dev.robocode.tankroyale.server.util.Version;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("WeakerAccess")
public final class ConnHandler {

  private static final Logger logger = LoggerFactory.getLogger(ConnHandler.class);

  private final ServerSetup setup;
  private final ConnListener listener;
  private final String clientSecret;

  private final WebSocketObserver webSocketObserver;

  private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

  private final Set<WebSocket> botConnections = Collections.synchronizedSet(new HashSet<>());
  private final Set<WebSocket> observerConnections = Collections.synchronizedSet(new HashSet<>());
  private final Set<WebSocket> controllerConnections = Collections.synchronizedSet(new HashSet<>());

  private final Map<WebSocket, BotHandshake> botHandshakes =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<WebSocket, ObserverHandshake> observerHandshakes =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<WebSocket, ControllerHandshake> controllerHandshakes =
      Collections.synchronizedMap(new HashMap<>());

  private final ExecutorService executorService;

  ConnHandler(ServerSetup setup, ConnListener listener, String clientSecret) {
    this.setup = setup;
    this.listener = listener;
    this.clientSecret = clientSecret == null ? null : clientSecret.trim();

    InetSocketAddress address = new InetSocketAddress("localhost", Server.getPort());
    this.webSocketObserver = new WebSocketObserver(address);
    this.webSocketObserver.setConnectionLostTimeout(10 /* second */); // TODO: Put this in a config file.

    this.executorService = Executors.newCachedThreadPool();
  }

  void start() {
    webSocketObserver.run();
  }

  void stop() {
    shutdownAndAwaitTermination(executorService);
  }

  void broadcastToBots(String message) {
    broadcast(getBotConnections(), message);
  }

  void broadcastToObserverAndControllers(String message) {
    broadcast(getObserverAndControllerConnections(), message);
  }

  Set<WebSocket> getBotConnections() {
    return Collections.unmodifiableSet(botConnections);
  }

  Set<WebSocket> getObserverAndControllerConnections() {
    Set<WebSocket> combined = new HashSet<>();
    combined.addAll(observerConnections);
    combined.addAll(controllerConnections);
    return Collections.unmodifiableSet(combined);
  }

  Map<WebSocket, BotHandshake> getBotHandshakes() {
    return Collections.unmodifiableMap(botHandshakes);
  }

  Set<WebSocket> getBotConnections(Collection<BotAddress> botAddresses) {
    Set<WebSocket> foundConnections = new HashSet<>();

    for (WebSocket conn : botHandshakes.keySet()) {
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
          logger.warn("Pool did not terminate");
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
    logger.debug("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

    conn.send(message);
  }

  private void broadcast(Collection<WebSocket> clients, String message) {
    logger.debug("Broadcast message: " + message);

    webSocketObserver.broadcast(message, clients);
  }

  private void notifyException(Exception exception) {
    logger.debug("Exception occurred: " + exception);

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
      logger.debug("onOpen(): " + conn.getRemoteSocketAddress());

      connections.add(conn);

      ServerHandshake hs = new ServerHandshake();
      hs.set$type(ServerHandshake.$type.SERVER_HANDSHAKE);
      hs.setVariant("Tank Royale"); // Robocode Tank Royale
      hs.setVersion(Version.getVersion());
      hs.setGameTypes(setup.getGameTypes());

      String msg = new Gson().toJson(hs);
      send(conn, msg);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
      logger.debug(
          "onClose(): "
              + conn.getRemoteSocketAddress()
              + ", code: "
              + code
              + ", reason: "
              + reason
              + ", remote: "
              + remote);

      connections.remove(conn);

      if (botConnections.remove(conn)) {
        BotHandshake handshake = botHandshakes.get(conn);
        executorService.submit(() -> listener.onBotLeft(conn, handshake));
        botHandshakes.remove(conn);

      } else if (observerConnections.remove(conn)) {
        ObserverHandshake handshake = observerHandshakes.get(conn);
        executorService.submit(() -> listener.onObserverLeft(conn, handshake));
        observerHandshakes.remove(conn);

      } else if (controllerConnections.remove(conn)) {
        ControllerHandshake handshake = controllerHandshakes.get(conn);
        executorService.submit(() -> listener.onControllerLeft(conn, handshake));
        controllerHandshakes.remove(conn);
      }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
      logger.debug("onMessage(): " + conn.getRemoteSocketAddress() + ", message: " + message);

      Gson gson = new Gson();
      try {
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

        JsonElement jsonType = jsonObject.get("$type");
        if (jsonType != null) {
          Message.$type $type;
          try {
            $type = Message.$type.fromValue(jsonType.getAsString());
          } catch (IllegalArgumentException ex) {
            notifyException(new IllegalStateException("Unhandled message type: " + jsonType.getAsString()));
            return;
          }

          logger.debug("Handling message: " + $type);

          switch ($type) {
            case BOT_HANDSHAKE:
              {
                BotHandshake handshake = gson.fromJson(message, BotHandshake.class);
                botConnections.add(conn);
                botHandshakes.put(conn, handshake);

                executorService.submit(() -> listener.onBotJoined(conn, handshake));
                break;
              }
            case OBSERVER_HANDSHAKE:
              {
                ObserverHandshake handshake = gson.fromJson(message, ObserverHandshake.class);

                // Validate client secret before continuing
                if (clientSecret != null
                    && !clientSecret.isEmpty()
                    && !handshake.getSecret().equals(clientSecret)) {
                  logger.info(
                      "Ignoring observer using invalid secret. Name: "
                          + handshake.getName()
                          + ", Version: "
                          + handshake.getVersion());
                  return; // Ignore client with wrong secret
                }

                observerConnections.add(conn);
                observerHandshakes.put(conn, handshake);

                executorService.submit(() -> listener.onObserverJoined(conn, handshake));
                break;
              }
            case CONTROLLER_HANDSHAKE:
              {
                ControllerHandshake handshake = gson.fromJson(message, ControllerHandshake.class);

                // Validate client secret before continuing
                if (clientSecret != null
                        && !clientSecret.isEmpty()
                        && !handshake.getSecret().equals(clientSecret)) {
                  logger.info(
                          "Ignoring controller using invalid secret. Name: "
                                  + handshake.getName()
                                  + ", Version: "
                                  + handshake.getVersion());
                  return; // Ignore client with wrong secret
                }

                controllerConnections.add(conn);
                controllerHandshakes.put(conn, handshake);

                executorService.submit(() -> listener.onControllerJoined(conn, handshake));
                break;
              }
            case BOT_READY:
              {
                executorService.submit(() -> listener.onBotReady(conn));
                break;
              }
            case BOT_INTENT:
              {
                BotIntent intent = gson.fromJson(message, BotIntent.class);
                executorService.submit(() -> listener.onBotIntent(conn, intent));
                break;
              }
            case START_GAME:
              {
                StartGame startGame = gson.fromJson(message, StartGame.class);
                GameSetup gameSetup = startGame.getGameSetup();
                Collection<BotAddress> botAddresses = startGame.getBotAddresses();
                executorService.submit(() -> listener.onStartGame(gameSetup, botAddresses));
                break;
              }
            case STOP_GAME:
              {
                executorService.submit(listener::onAbortGame);
                break;
              }
            case PAUSE_GAME:
              {
                executorService.submit(listener::onPauseGame);
                break;
              }
            case RESUME_GAME:
              {
                executorService.submit(listener::onResumeGame);
                break;
              }
            case CHANGE_TPS:
              {
                ChangeTps changeTps = gson.fromJson(message, ChangeTps.class);
                executorService.submit(() -> listener.onChangeTps(changeTps.getTps()));
                break;
              }
            default:
              notifyException(new IllegalStateException("Unhandled message type: " + $type));
          }
        }
      } catch (JsonSyntaxException e2) {
        logger.error("Invalid message: " + message, e2);
      }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
      notifyException(ex);
    }
  }
}
