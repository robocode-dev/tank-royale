package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.neovisionaries.ws.client.*;
import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.GameSetup;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.botapi.mapper.ResultsMapper;
import dev.robocode.tankroyale.schema.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static dev.robocode.tankroyale.botapi.IBaseBot.*;
import static dev.robocode.tankroyale.botapi.internal.MathUtil.limitRange;
import static java.lang.Math.*;

public final class BaseBotInternals {
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

  private final double absDeceleration = abs(DECELERATION);

  private final IBaseBot baseBot;
  private final BotInfo botInfo;
  private final BotEventHandlers botEventHandlers;
  private final EventQueue eventQueue;
  private final Set<Condition> conditions = new HashSet<>();

  private BotIntent botIntent = newBotIntent();

  private StopResumeListener stopResumeListener;

  // Server connection:
  private WebSocket socket;
  private ServerHandshake serverHandshake;

  // Current game states:
  private Integer myId;
  private dev.robocode.tankroyale.botapi.GameSetup gameSetup;
  private TickEvent tickEvent;
  private Long tickStartNanoTime;
  private volatile boolean isStopped;

  // Maximum speed and turn rates
  private double maxSpeed = MAX_SPEED;
  private double maxTurnRate = MAX_TURN_RATE;
  private double maxGunTurnRate = MAX_GUN_TURN_RATE;
  private double maxRadarTurnRate = MAX_RADAR_TURN_RATE;

  public BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, URI serverUrl) {
    this.baseBot = baseBot;
    this.botInfo = (botInfo == null) ? EnvVars.getBotInfo() : botInfo;

    this.botEventHandlers = new BotEventHandlers(baseBot);
    this.eventQueue = new EventQueue(this, botEventHandlers);

    init(serverUrl == null ? getServerUrlFromSetting() : serverUrl);
  }

  public void setStopResumeHandler(StopResumeListener listener) {
    stopResumeListener = listener;
  }

  private void init(URI serverUrl) {
    try {
      socket = new WebSocketFactory().createSocket(serverUrl);
    } catch (IOException ex) {
      throw new BotException("Could not create socket for URL: " + serverUrl, ex);
    }
    socket.addListener(new WebSocketListener());

    botEventHandlers.onNewRound.subscribe(this::handleNewRound, 100);
    botEventHandlers.onBulletFired.subscribe(this::handleBulletFired, 100);
  }

  private static BotIntent newBotIntent() {
    BotIntent botIntent = new BotIntent();
    botIntent.set$type(BotReady.$type.BOT_INTENT); // must be set!
    return botIntent;
  }

  BotEventHandlers getBotEventHandlers() {
    return botEventHandlers;
  }

  Set<Condition> getConditions() {
    return conditions;
  }

  public void start() {
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

  public void execute() {
    // Send the bot intent to the server
    sendIntent();

    // Clear rescanning
    botIntent.setScan(false);

    // Dispatch all bot events
    new Thread(this::dispatchEvents).start();
  }

  private void dispatchEvents() {
    try {
      eventQueue.dispatchEvents(getCurrentTick().getTurnNumber());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendIntent() {
    limitTargetSpeedAndTurnRates();
    socket.sendText(gson.toJson(botIntent));
  }

  private void limitTargetSpeedAndTurnRates() {
    Double targetSpeed = botIntent.getTargetSpeed();
    if (targetSpeed != null) {
      botIntent.setTargetSpeed(limitRange(targetSpeed, -maxSpeed, maxSpeed));
    }
    Double turnRate = botIntent.getTurnRate();
    if (turnRate != null) {
      botIntent.setTurnRate(limitRange(turnRate, -maxTurnRate, maxTurnRate));
    }
    Double gunTurnRate = botIntent.getGunTurnRate();
    if (gunTurnRate != null) {
      botIntent.setGunTurnRate(limitRange(gunTurnRate, -maxGunTurnRate, maxGunTurnRate));
    }
    Double radarTurnRate = botIntent.getRadarTurnRate();
    if (radarTurnRate != null) {
      botIntent.setRadarTurnRate(limitRange(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate));
    }
  }

  public String getVariant() {
    return getServerHandshake().getVariant();
  }

  public String getVersion() {
    return getServerHandshake().getVersion();
  }

  public int getMyId() {
    if (myId == null) {
      throw new BotException(GAME_NOT_RUNNING_MSG);
    }
    return myId;
  }

  public GameSetup getGameSetup() {
    if (gameSetup == null) {
      throw new BotException(GAME_NOT_RUNNING_MSG);
    }
    return gameSetup;
  }

  public BotIntent getBotIntent() {
    if (botIntent == null) {
      throw new BotException(GAME_NOT_RUNNING_MSG);
    }
    return botIntent;
  }

  public TickEvent getCurrentTick() {
    if (tickEvent == null) {
      throw new BotException(TICK_NOT_AVAILABLE_MSG);
    }
    return tickEvent;
  }

  private long getTicksStart() {
    if (tickStartNanoTime == null) {
      throw new BotException(TICK_NOT_AVAILABLE_MSG);
    }
    return tickStartNanoTime;
  }

  public int getTimeLeft() {
    long passesMicroSeconds = (System.nanoTime() - getTicksStart()) / 1000;
    return (int) (getGameSetup().getTurnTimeout() - passesMicroSeconds);
  }

  public boolean setFire(double firepower) {
    if (Double.isNaN(firepower)) {
      throw new IllegalArgumentException("firepower cannot be NaN");
    }
    if (baseBot.getEnergy() < firepower || baseBot.getGunHeat() > 0) {
      return false; // cannot fire yet
    }
    botIntent.setFirepower(firepower);
    return true;
  }

  public void setMaxSpeed(double maxSpeed) {
    this.maxSpeed = limitRange(maxSpeed, 0, MAX_SPEED);
  }

  public void setMaxTurnRate(double maxTurnRate) {
    this.maxTurnRate = limitRange(maxTurnRate, 0, MAX_TURN_RATE);
  }

  public void setMaxGunTurnRate(double maxGunTurnRate) {
    this.maxGunTurnRate = limitRange(maxGunTurnRate, 0, MAX_GUN_TURN_RATE);
  }

  public void setMaxRadarTurnRate(double maxRadarTurnRate) {
    this.maxRadarTurnRate = limitRange(maxRadarTurnRate, 0, MAX_RADAR_TURN_RATE);
  }

  /**
   * Returns the new speed based on the current speed and distance to move.
   *
   * @param speed is the current speed
   * @param distance is the distance to move
   * @return The new speed
   */
  // Credits for this algorithm goes to Patrick Cupka (aka Voidious),
  // Julian Kent (aka Skilgannon), and Positive:
  // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
  public double getNewSpeed(double speed, double distance) {
    if (distance < 0) {
      return -getNewSpeed(-speed, -distance);
    }

    final double targetSpeed;
    if (distance == Double.POSITIVE_INFINITY) {
      targetSpeed = maxSpeed;
    } else {
      targetSpeed = min(getMaxSpeed(distance), maxSpeed);
    }
    if (speed >= 0) {
      return limitRange(targetSpeed, speed - absDeceleration, speed + ACCELERATION);
    } else {
      return limitRange(targetSpeed, speed - ACCELERATION, speed + getMaxDeceleration(-speed));
    }
  }

  private double getMaxSpeed(double distance) {
    double decelTime =
            max(1, Math.ceil((Math.sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
    if (decelTime == Double.POSITIVE_INFINITY) {
      return MAX_SPEED;
    }
    double decelDist = (decelTime / 2) * (decelTime - 1) * absDeceleration;
    return ((decelTime - 1) * absDeceleration) + ((distance - decelDist) / decelTime);
  }

  private double getMaxDeceleration(double speed) {
    double decelTime = speed / absDeceleration;
    double accelTime = 1 - decelTime;

    return min(1, decelTime) * absDeceleration + max(0, accelTime) * ACCELERATION;
  }

  double getDistanceTraveledUntilStop(double speed) {
    speed = abs(speed);
    double distance = 0;
    while (speed > 0) {
      distance += (speed = getNewSpeed(speed, 0));
    }
    return distance;
  }

  public void addCondition(Condition condition) {
    conditions.add(condition);
  }

  public void removeCondition(Condition condition) {
    conditions.remove(condition);
  }

  public void setStop() {
    if (!isStopped) {
      isStopped = true;

      botIntent.setTargetSpeed(0d);
      botIntent.setTurnRate(0d);
      botIntent.setGunTurnRate(0d);
      botIntent.setRadarTurnRate(0d);

      if (stopResumeListener != null) {
        stopResumeListener.onStop();
      }
    }
  }

  public void setResume() {
    if (isStopped) {
      isStopped = false;

      if (stopResumeListener != null) {
        stopResumeListener.onResume();
      }
    }
  }

  public boolean isStopped() {
    return isStopped;
  }

  private ServerHandshake getServerHandshake() {
    if (serverHandshake == null) {
      throw new BotException(NOT_CONNECTED_TO_SERVER_MSG);
    }
    return serverHandshake;
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

  private final class WebSocketListener extends WebSocketAdapter {

    @Override
    public final void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
      botEventHandlers.onConnected.publish(new ConnectedEvent(websocket.getURI()));
    }

    @Override
    public final void onDisconnected(
        WebSocket websocket,
        WebSocketFrame serverCloseFrame,
        WebSocketFrame clientCloseFrame,
        boolean closedByServer) {

      botEventHandlers.onDisconnected.publish(
          new DisconnectedEvent(websocket.getURI(), closedByServer));
    }

    @Override
    public final void onError(WebSocket websocket, WebSocketException cause) {
      botEventHandlers.onConnectionError.publish(
          new ConnectionErrorEvent(websocket.getURI(), cause));
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

      botEventHandlers.onGameStarted.publish(
          new GameStartedEvent(gameStartedEventForBot.getMyId(), gameSetup));
    }
  }

  private void handleGameEndedEvent(JsonObject jsonMsg) {
    // Send the game ended event
    GameEndedEventForBot gameEndedEventForBot = gson.fromJson(jsonMsg, GameEndedEventForBot.class);

    GameEndedEvent gameEndedEvent =
        new GameEndedEvent(
            gameEndedEventForBot.getNumberOfRounds(),
            ResultsMapper.map(gameEndedEventForBot.getResults()));

    botEventHandlers.onGameEnded.publish(gameEndedEvent);
  }

  private void handleSkippedTurnEvent(JsonObject jsonMsg) {
    dev.robocode.tankroyale.schema.SkippedTurnEvent skippedTurnEvent =
        gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

    botEventHandlers.onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent));
  }

  private void handleTickEvent(JsonObject jsonMsg) {
    TickEventForBot tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
    tickEvent = EventMapper.map(tickEventForBot);

    tickStartNanoTime = System.nanoTime();

    eventQueue.addEventsFromTick(tickEvent, baseBot);

    // Trigger new round
    if (tickEvent.getTurnNumber() == 1) {
      botEventHandlers.onNewRound.publish(tickEvent);
    }

    // Trigger processing turn
    botEventHandlers.onProcessTurn.publish(tickEvent);
  }

  private void handleNewRound(TickEvent e) {
    tickEvent = e; // use new bot coordinate, rates and directions etc.
    botIntent = newBotIntent();
    eventQueue.clear();
  }

  private void handleBulletFired(BulletFiredEvent e) {
    botIntent.setFirepower(0d); // Reset firepower so the bot stops firing continuously
  }
}
