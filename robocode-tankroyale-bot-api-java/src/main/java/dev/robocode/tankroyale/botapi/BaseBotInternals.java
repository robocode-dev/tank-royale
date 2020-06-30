package dev.robocode.tankroyale.botapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.neovisionaries.ws.client.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.events.BotDeathEvent;
import dev.robocode.tankroyale.botapi.events.BotHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BotHitWallEvent;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBulletEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitWallEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.WonRoundEvent;
import dev.robocode.tankroyale.botapi.factory.BotHandshakeFactory;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.botapi.mapper.ResultsMapper;
import dev.robocode.tankroyale.schema.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class BaseBotInternals {
  private static final String SERVER_URL_PROPERTY_KEY = "server.url";

  private static final String NOT_CONNECTED_TO_SERVER_MSG =
      "Not connected to game server yes. Make sure onConnected() event handler has been called first";

  private static final String GAME_NOT_RUNNING_MSG =
      "Game is not running. Make sure onGameStarted() event handler has been called first";

  private static final String TICK_NOT_AVAILABLE_MSG =
      "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

  private final Gson gson;

  {
    RuntimeTypeAdapterFactory<dev.robocode.tankroyale.schema.Event> typeFactory =
        RuntimeTypeAdapterFactory.of(dev.robocode.tankroyale.schema.Event.class, "$type")
            .registerSubtype(dev.robocode.tankroyale.schema.BotDeathEvent.class, "BotDeathEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BotHitBotEvent.class, "BotHitBotEvent")
            .registerSubtype(
                dev.robocode.tankroyale.schema.BotHitWallEvent.class, "BotHitWallEvent")
            .registerSubtype(
                dev.robocode.tankroyale.schema.BulletFiredEvent.class, "BulletFiredEvent")
            .registerSubtype(
                dev.robocode.tankroyale.schema.BulletHitBotEvent.class, "BulletHitBotEvent")
            .registerSubtype(
                dev.robocode.tankroyale.schema.BulletHitBulletEvent.class, "BulletHitBulletEvent")
            .registerSubtype(
                dev.robocode.tankroyale.schema.BulletHitWallEvent.class, "BulletHitWallEvent")
            .registerSubtype(
                dev.robocode.tankroyale.schema.ScannedBotEvent.class, "ScannedBotEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.WonRoundEvent.class, "WonRoundEvent");

    gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
  }

  private final IBaseBot baseBot;
  private final BotInfo botInfo;

  final BotIntent botIntent = new BotIntent();

  // Server connection:
  private WebSocket socket;
  private ServerHandshake serverHandshake;

  // Current game states:
  private Integer myId;
  private GameSetup gameSetup;
  private TickEvent currentTurn;
  private Long ticksStartNanoTime;

  // Adjustment of turn rates
  boolean isAdjustGunForBodyTurn;
  boolean isAdjustRadarForGunTurn;

  // Events
  final Event<ConnectedEvent> onConnected = new Event<>();
  final Event<DisconnectedEvent> onDisconnected = new Event<>();
  final Event<ConnectionErrorEvent> onConnectionError = new Event<>();
  final Event<GameStartedEvent> onGameStarted = new Event<>();
  final Event<GameEndedEvent> onGameEnded = new Event<>();
  final Event<TickEvent> onTick = new Event<>();
  final Event<SkippedTurnEvent> onSkippedTurn = new Event<>();
  final Event<BotDeathEvent> onDeath = new Event<>();
  final Event<BotDeathEvent> onBotDeath = new Event<>();
  final Event<BotHitBotEvent> onHitBot = new Event<>();
  final Event<BotHitWallEvent> onHitWall = new Event<>();
  final Event<BulletFiredEvent> onBulletFired = new Event<>();
  final Event<BulletHitBotEvent> onHitByBullet = new Event<>();
  final Event<BulletHitBotEvent> onBulletHit = new Event<>();
  final Event<BulletHitBulletEvent> onBulletHitBullet = new Event<>();
  final Event<BulletHitWallEvent> onBulletHitWall = new Event<>();
  final Event<ScannedBotEvent> onScannedBot = new Event<>();
  final Event<WonRoundEvent> onWonRound = new Event<>();

  BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, URI serverUrl) {
    this.baseBot = baseBot;
    this.botInfo = (botInfo == null) ? EnvVars.getBotInfo() : botInfo;
    init(serverUrl == null ? getServerUrlFromSetting() : serverUrl);
  }

  private void init(URI serverUrl) {
    try {
      socket = new WebSocketFactory().createSocket(serverUrl);
    } catch (IOException ex) {
      throw new BotException("Could not create socket for URL: " + serverUrl, ex);
    }
    socket.addListener(new WebSocketListener());
    botIntent.set$type(BotReady.$type.BOT_INTENT); // must be set!

    onConnected.subscribe(baseBot::onConnected);
    onDisconnected.subscribe(baseBot::onDisconnected);
    onConnectionError.subscribe(baseBot::onConnectionError);
    onGameStarted.subscribe(baseBot::onGameStarted);
    onGameEnded.subscribe(baseBot::onGameEnded);
    onTick.subscribe(baseBot::onTick);
    onSkippedTurn.subscribe(baseBot::onSkippedTurn);
    onDeath.subscribe(baseBot::onDeath);
    onBotDeath.subscribe(baseBot::onBotDeath);
    onHitBot.subscribe(baseBot::onHitBot);
    onHitWall.subscribe(baseBot::onHitWall);
    onBulletFired.subscribe(baseBot::onBulletFired);
    onHitByBullet.subscribe(baseBot::onHitByBullet);
    onBulletHit.subscribe(baseBot::onBulletHit);
    onBulletHitBullet.subscribe(baseBot::onBulletHitBullet);
    onBulletHitWall.subscribe(baseBot::onBulletHitWall);
    onScannedBot.subscribe(baseBot::onScannedBot);
    onWonRound.subscribe(baseBot::onWonRound);
  }

  void connect() {
    if (!socket.isOpen()) {
      try {
        socket.connect();
      } catch (WebSocketException ex) {
        throw new BotException(
            "Could not connect to web socket: "
                + socket.getURI()
                + ". Setup "
                + EnvVars.SERVER_URL
                + " to point to a server that is up and running.",
            ex);
      }
    }
  }

  void sendIntent() {
    socket.sendText(gson.toJson(botIntent));
  }

  private void clearCurrentGameState() {
    // Clear setting that are only available during a running game
    currentTurn = null;
    gameSetup = null;
    myId = null;
  }

  private URI getServerUrlFromSetting() {
    String url = EnvVars.getServerUrl();
    if (url == null) {
      url = System.getProperty(SERVER_URL_PROPERTY_KEY);
      if (url == null) {
        url = EnvVars.getServerUrl();
      }
    }
    if (url == null) {
      url = "ws://localhost";
    }
    try {
      return new URI(url);
    } catch (URISyntaxException ex) {
      throw new BotException("Incorrect syntax for server URL: " + url);
    }
  }

  ServerHandshake getServerHandshake() {
    if (serverHandshake == null) {
      throw new BotException(NOT_CONNECTED_TO_SERVER_MSG);
    }
    return serverHandshake;
  }

  int getMyId() {
    if (myId == null) {
      throw new BotException(GAME_NOT_RUNNING_MSG);
    }
    return myId;
  }

  GameSetup getGameSetup() {
    if (gameSetup == null) {
      throw new BotException(GAME_NOT_RUNNING_MSG);
    }
    return gameSetup;
  }

  TickEvent getCurrentTurn() {
    if (currentTurn == null) {
      throw new BotException(TICK_NOT_AVAILABLE_MSG);
    }
    return currentTurn;
  }

  long getTicksStart() {
    if (ticksStartNanoTime == null) {
      throw new BotException(TICK_NOT_AVAILABLE_MSG);
    }
    return ticksStartNanoTime;
  }

  private final class WebSocketListener extends WebSocketAdapter {

    @Override
    public final void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
      onConnected.publish(new ConnectedEvent(websocket.getURI()));
    }

    @Override
    public final void onDisconnected(
        WebSocket websocket,
        WebSocketFrame serverCloseFrame,
        WebSocketFrame clientCloseFrame,
        boolean closedByServer) {

      onDisconnected.publish(new DisconnectedEvent(websocket.getURI(), closedByServer));

      clearCurrentGameState();
    }

    @Override
    public final void onError(WebSocket websocket, WebSocketException cause) {
      onConnectionError.publish(new ConnectionErrorEvent(websocket.getURI(), cause));
    }

    @Override
    public void onPingFrame(WebSocket websocket, WebSocketFrame frame) {
      // Make sure to send pong as reply to ping in order to stay connected to server
      socket.sendPong();
    }

    @Override
    public final void onTextMessage(WebSocket websocket, String json) {
      JsonObject jsonMsg = gson.fromJson(json, JsonObject.class);

      JsonElement jsonType = jsonMsg.get("$type");
      if (jsonType != null) {
        String type = jsonType.getAsString();

        switch (dev.robocode.tankroyale.schema.Message.$type.fromValue(type)) {
          case TICK_EVENT_FOR_BOT:
            handleTickEvent(jsonMsg);
            break;
          case SERVER_HANDSHAKE:
            handleServerHandshake(jsonMsg);
            break;
          case GAME_STARTED_EVENT_FOR_BOT:
            handleGameStartedEvent(jsonMsg);
            break;
          case GAME_ENDED_EVENT_FOR_BOT:
            handleGameEndedEvent(jsonMsg);
            break;
          case SKIPPED_TURN_EVENT:
            handleSkippedTurnEvent(jsonMsg);
            break;
          default:
            throw new BotException("Unsupported WebSocket message type: " + type);
        }
      }
    }

    private void handleServerHandshake(JsonObject jsonMsg) {
      serverHandshake = gson.fromJson(jsonMsg, ServerHandshake.class);

      // Reply by sending bot handshake
      BotHandshake botHandshake = BotHandshakeFactory.create(botInfo);
      String msg = gson.toJson(botHandshake);

      socket.sendText(msg);
    }

    private void handleGameStartedEvent(JsonObject jsonMsg) {
      GameStartedEventForBot gameStartedEventForBot =
          gson.fromJson(jsonMsg, GameStartedEventForBot.class);

      myId = gameStartedEventForBot.getMyId();
      gameSetup = GameSetupMapper.map(gameStartedEventForBot.getGameSetup());

      // Send ready signal
      BotReady ready = new BotReady();
      ready.set$type(BotReady.$type.BOT_READY);

      String msg = gson.toJson(ready);
      socket.sendText(msg);

      onGameStarted.publish(new GameStartedEvent(gameStartedEventForBot.getMyId(), gameSetup));
    }
  }

  private void handleGameEndedEvent(JsonObject jsonMsg) {
    // Clear current game state
    clearCurrentGameState();

    // Send the game ended event
    GameEndedEventForBot gameEndedEventForBot = gson.fromJson(jsonMsg, GameEndedEventForBot.class);

    GameEndedEvent gameEndedEvent =
        new GameEndedEvent(
            gameEndedEventForBot.getNumberOfRounds(),
            ResultsMapper.map(gameEndedEventForBot.getResults()));

    onGameEnded.publish(gameEndedEvent);
  }

  private void handleSkippedTurnEvent(JsonObject jsonMsg) {
    dev.robocode.tankroyale.schema.SkippedTurnEvent skippedTurnEvent =
        gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

    onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent));
  }

  private void handleTickEvent(JsonObject jsonMsg) {
    TickEventForBot tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
    currentTurn = EventMapper.map(tickEventForBot);

    // Dispatch all on the tick event before the tick event itself
    dispatchEvents(currentTurn);

    ticksStartNanoTime = System.nanoTime();
    onTick.publish(currentTurn);
  }

  // FIXME:
  // https://stackoverflow.com/questions/5579309/is-it-possible-to-use-the-instanceof-operator-in-a-switch-statement
  private void dispatchEvents(TickEvent tickEvent) {
    tickEvent
        .getEvents()
        .forEach(
            event -> {
              if (event instanceof BotDeathEvent) {
                BotDeathEvent botDeathEvent = (BotDeathEvent) event;
                if (botDeathEvent.getVictimId() == myId) {
                  onDeath.publish((BotDeathEvent) event);
                } else {
                  onBotDeath.publish((BotDeathEvent) event);
                }

              } else if (event instanceof BotHitBotEvent) {
                onHitBot.publish((BotHitBotEvent) event);

              } else if (event instanceof BotHitWallEvent) {
                onHitWall.publish((BotHitWallEvent) event);

              } else if (event instanceof BulletFiredEvent) {
                // Stop firing, when bullet has fired
                botIntent.setFirepower(0d);
                onBulletFired.publish((BulletFiredEvent) event);

              } else if (event instanceof BulletHitBotEvent) {
                BulletHitBotEvent bulletEvent = (BulletHitBotEvent) event;
                if (bulletEvent.getVictimId() == myId) {
                  onHitByBullet.publish(bulletEvent);
                } else {
                  onBulletHit.publish(bulletEvent);
                }

              } else if (event instanceof BulletHitBulletEvent) {
                onBulletHitBullet.publish((BulletHitBulletEvent) event);

              } else if (event instanceof BulletHitWallEvent) {
                onBulletHitWall.publish((BulletHitWallEvent) event);

              } else if (event instanceof ScannedBotEvent) {
                onScannedBot.publish((ScannedBotEvent) event);

              } else if (event instanceof SkippedTurnEvent) {
                onSkippedTurn.publish((SkippedTurnEvent) event);

              } else if (event instanceof WonRoundEvent) {
                onWonRound.publish((WonRoundEvent) event);
              }
            });
  }

  // Event handler which events in the order they have been added to the handler
  protected static class Event<T> {
    private final List<Consumer<T>> subscribers = Collections.synchronizedList(new ArrayList<>());

    final void subscribe(Consumer<T> subscriber) {
      subscribers.add(subscriber);
    }

    final void publish(T event) {
      for (Consumer<T> subscriber : subscribers) {
        subscriber.accept(event);
      }
    }
  }
}
