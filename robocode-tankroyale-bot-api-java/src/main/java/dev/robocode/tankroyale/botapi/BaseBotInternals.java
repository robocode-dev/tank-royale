package dev.robocode.tankroyale.botapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.neovisionaries.ws.client.*;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.factory.BotHandshakeFactory;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.botapi.mapper.ResultsMapper;
import dev.robocode.tankroyale.schema.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

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
  final BotEvents botEvents;

  BotIntent botIntent = new BotIntent();

  // Server connection:
  private WebSocket socket;
  private ServerHandshake serverHandshake;

  // Current game states:
  private Integer myId;
  private GameSetup gameSetup;
  private TickEvent currentTick;
  private Long ticksStartNanoTime;

  // Adjustment of turn rates
  boolean doAdjustGunForBodyTurn;
  boolean doAdjustRadarForGunTurn;

  private final boolean doDispatchEvents;

  private int turnWhenGunFired = -1;

  BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, URI serverUrl) {
    this.baseBot = baseBot;
    this.botEvents = new BotEvents(baseBot);
    this.botInfo = (botInfo == null) ? EnvVars.getBotInfo() : botInfo;
    this.doDispatchEvents = !(baseBot instanceof IBot);
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

    botEvents.onConnected.subscribe(baseBot::onConnected);
    botEvents.onDisconnected.subscribe(baseBot::onDisconnected);
    botEvents.onConnectionError.subscribe(baseBot::onConnectionError);
    botEvents.onGameStarted.subscribe(baseBot::onGameStarted);
    botEvents.onGameEnded.subscribe(baseBot::onGameEnded);
    botEvents.onTick.subscribe(baseBot::onTick);
    botEvents.onSkippedTurn.subscribe(baseBot::onSkippedTurn);
    botEvents.onDeath.subscribe(baseBot::onDeath);
    botEvents.onBotDeath.subscribe(baseBot::onBotDeath);
    botEvents.onHitBot.subscribe(baseBot::onHitBot);
    botEvents.onHitWall.subscribe(baseBot::onHitWall);
    botEvents.onBulletFired.subscribe(baseBot::onBulletFired);
    botEvents.onHitByBullet.subscribe(baseBot::onHitByBullet);
    botEvents.onBulletHit.subscribe(baseBot::onBulletHit);
    botEvents.onBulletHitBullet.subscribe(baseBot::onBulletHitBullet);
    botEvents.onBulletHitWall.subscribe(baseBot::onBulletHitWall);
    botEvents.onScannedBot.subscribe(baseBot::onScannedBot);
    botEvents.onWonRound.subscribe(baseBot::onWonRound);

    botEvents.onBulletFired.subscribe(this::handleBulletFired);
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
    currentTick = null;
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

  TickEvent getCurrentTick() {
    if (currentTick == null) {
      throw new BotException(TICK_NOT_AVAILABLE_MSG);
    }
    return currentTick;
  }

  long getTicksStart() {
    if (ticksStartNanoTime == null) {
      throw new BotException(TICK_NOT_AVAILABLE_MSG);
    }
    return ticksStartNanoTime;
  }

  boolean hasGunFired() {
    System.out.println(
        "hasGunFired, currentTick.getTurnNumber: "
            + currentTick.getTurnNumber()
            + ", turnWhenGunFired: "
            + turnWhenGunFired
            + ", heat: "
            + baseBot.getGunHeat());
    return (currentTick.getTurnNumber() == turnWhenGunFired);
  }

  private final class WebSocketListener extends WebSocketAdapter {

    @Override
    public final void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
      botEvents.onConnected.publish(new ConnectedEvent(websocket.getURI()));
    }

    @Override
    public final void onDisconnected(
        WebSocket websocket,
        WebSocketFrame serverCloseFrame,
        WebSocketFrame clientCloseFrame,
        boolean closedByServer) {

      botEvents.onDisconnected.publish(new DisconnectedEvent(websocket.getURI(), closedByServer));

      clearCurrentGameState();
    }

    @Override
    public final void onError(WebSocket websocket, WebSocketException cause) {
      botEvents.onConnectionError.publish(new ConnectionErrorEvent(websocket.getURI(), cause));
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

      botEvents.onGameStarted.publish(
          new GameStartedEvent(gameStartedEventForBot.getMyId(), gameSetup));
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

    botEvents.onGameEnded.publish(gameEndedEvent);
  }

  private void handleSkippedTurnEvent(JsonObject jsonMsg) {
    dev.robocode.tankroyale.schema.SkippedTurnEvent skippedTurnEvent =
        gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

    botEvents.onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent));
  }

  private void handleTickEvent(JsonObject jsonMsg) {
    TickEventForBot tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
    currentTick = EventMapper.map(tickEventForBot);

    ticksStartNanoTime = System.nanoTime();
    botEvents.onTick.publish(currentTick);

    if (doDispatchEvents) {
      botEvents.dispatchEvents(currentTick);
    }
  }

  private void handleBulletFired(BulletFiredEvent e) {
    System.out.println(
        "## handleBulletFired: "
            + e.getTurnNumber()
            + ", curentTick.turnNumber: "
            + getCurrentTick().getTurnNumber());
    botIntent.setFirepower(0d); // Reset firepower so the bot stops firing continuously
    turnWhenGunFired = currentTick.getTurnNumber();
  }
}
