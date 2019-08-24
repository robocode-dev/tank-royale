package dev.robocode.tankroyale.server.server;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonSyntaxException;
import dev.robocode.tankroyale.server.mappers.GameSetupToGameSetupMapper;
import dev.robocode.tankroyale.server.Server;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.robocode.tankroyale.schema.GameSetup;
import dev.robocode.tankroyale.schema.BotAddress;
import dev.robocode.tankroyale.schema.BotHandshake;
import dev.robocode.tankroyale.schema.BotIntent;
import dev.robocode.tankroyale.schema.ControllerHandshake;
import dev.robocode.tankroyale.schema.Message;
import dev.robocode.tankroyale.schema.ObserverHandshake;
import dev.robocode.tankroyale.schema.ServerHandshake;
import dev.robocode.tankroyale.schema.StartGame;
import dev.robocode.tankroyale.server.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnHandler {

  private static Logger logger = LoggerFactory.getLogger(ConnHandler.class);

  private final ServerSetup setup;
  private final ConnListener listener;
  private final WebSocketObserver webSocketObserver;

  private final Set<WebSocket> connections = Collections.synchronizedSet(new HashSet<>());

  private final Set<WebSocket> botConnections = Collections.synchronizedSet(new HashSet<>());
  private final Set<WebSocket> observerAndControllerConnections =
      Collections.synchronizedSet(new HashSet<>());
  private final Set<WebSocket> controllerConnections = Collections.synchronizedSet(new HashSet<>());

  private final Map<WebSocket, BotHandshake> botHandshakes =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<WebSocket, ObserverHandshake> observerHandshakes =
      Collections.synchronizedMap(new HashMap<>());
  private final Map<WebSocket, ControllerHandshake> controllerHandshakes =
      Collections.synchronizedMap(new HashMap<>());

  private static final String TYPE = "type";

  private final ExecutorService executorService;

  ConnHandler(ServerSetup setup, ConnListener listener) {
    this.setup = setup;
    this.listener = listener;

    InetSocketAddress address = new InetSocketAddress("localhost", Server.getPort());
    this.webSocketObserver = new WebSocketObserver(address);
    this.webSocketObserver.setConnectionLostTimeout(1 /* second */);

    this.executorService = Executors.newCachedThreadPool();
  }

  void start() {
    webSocketObserver.run();
  }

  // TODO: Call this method
  public void stop() {
    shutdownAndAwaitTermination(executorService);
  }

  Set<WebSocket> getBotConnections() {
    return Collections.unmodifiableSet(botConnections);
  }

  Set<WebSocket> getObserverAndControllerConnections() {
    return Collections.unmodifiableSet(observerAndControllerConnections);
  }

  Map<WebSocket, BotHandshake> getBotHandshakes() {
    return Collections.unmodifiableMap(botHandshakes);
  }

  public Set<WebSocket> getBotConnections(Collection<BotAddress> botAddresses) {
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
      hs.setType(ServerHandshake.Type.SERVER_HANDSHAKE);
      hs.setVariant("Tank Royale"); // Robocode Tank Royale
      hs.setVersion(Version.getVersion());
      hs.setGames(GameSetupToGameSetupMapper.map(setup.getGames()));

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
        botHandshakes.remove(conn);
        executorService.submit(() -> listener.onBotLeft(conn));
      } else if (observerAndControllerConnections.remove(conn)) {
        observerHandshakes.remove(conn);
        executorService.submit(() -> listener.onObserverLeft(conn));
      } else if (controllerConnections.remove(conn)) {
        controllerHandshakes.remove(conn);
        executorService.submit(() -> listener.onControllerLeft(conn));
      }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
      logger.debug("onMessage(): " + conn.getRemoteSocketAddress() + ", message: " + message);

      Gson gson = new Gson();
      try {
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

        JsonElement jsonType = jsonObject.get(TYPE);
        if (jsonType != null) {
          Message.Type type = Message.Type.fromValue(jsonType.getAsString());
          logger.debug("Handling message: " + type);

          switch (type) {
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
                observerAndControllerConnections.add(conn);
                observerHandshakes.put(conn, handshake);

                executorService.submit(() -> listener.onObserverJoined(conn, handshake));
                break;
              }
            case CONTROLLER_HANDSHAKE:
              {
                ControllerHandshake handshake = gson.fromJson(message, ControllerHandshake.class);
                controllerConnections.add(conn);
                controllerHandshakes.put(conn, handshake);
                // Controller is also an observer
                observerAndControllerConnections.add(conn);

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
            default:
              notifyException(new IllegalStateException("Unhandled message type: " + type));
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
