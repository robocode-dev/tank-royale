package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.GameSetup;
import dev.robocode.tankroyale.botapi.IBaseBot;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.RoundEndedEvent;
import dev.robocode.tankroyale.botapi.events.RoundStartedEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.botapi.mapper.ResultsMapper;
import dev.robocode.tankroyale.schema.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import static dev.robocode.tankroyale.botapi.IBaseBot.*;
import static dev.robocode.tankroyale.botapi.internal.MathUtil.clamp;
import static java.lang.Math.*;
import static java.net.http.WebSocket.Builder;
import static java.net.http.WebSocket.Listener;

public final class BaseBotInternals {
  private static final String SERVER_URL_PROPERTY_KEY = "server.url";

  private static final String NOT_CONNECTED_TO_SERVER_MSG =
      "Not connected to a game server. Make sure onConnected() event handler has been called first";

  private static final String GAME_NOT_RUNNING_MSG =
      "Game is not running. Make sure onGameStarted() event handler has been called first";

  private static final String TICK_NOT_AVAILABLE_MSG =
      "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

  private final URI serverUrl;
  private WebSocket socket;
  private ServerHandshake serverHandshake;
  private final CountDownLatch closedLatch = new CountDownLatch(1);

  private final IBaseBot baseBot;
  private final BotInfo botInfo;
  private BotIntent botIntent = newBotIntent();

  private Integer myId;
  private dev.robocode.tankroyale.botapi.GameSetup gameSetup;

  private TickEvent tickEvent;
  private Long tickStartNanoTime;

  private final BotEventHandlers botEventHandlers;
  private final EventQueue eventQueue;
  private final Set<Condition> conditions = new HashSet<>();

  private final Object nextTurnMonitor = new Object();

  private boolean isStopped;
  private IStopResumeListener stopResumeListener;

  private double maxSpeed = MAX_SPEED;
  private double maxTurnRate = MAX_TURN_RATE;
  private double maxGunTurnRate = MAX_GUN_TURN_RATE;
  private double maxRadarTurnRate = MAX_RADAR_TURN_RATE;

  private Double savedTargetSpeed;
  private Double savedTurnRate;
  private Double savedGunTurnRate;
  private Double savedRadarTurnRate;

  private final double absDeceleration = abs(DECELERATION);

  private final Gson gson;

  {
    RuntimeTypeAdapterFactory<dev.robocode.tankroyale.schema.Event> typeFactory =
        RuntimeTypeAdapterFactory.of(dev.robocode.tankroyale.schema.Event.class, "$type")
            .registerSubtype(dev.robocode.tankroyale.schema.BotDeathEvent.class, "BotDeathEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BotHitBotEvent.class, "BotHitBotEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BotHitWallEvent.class, "BotHitWallEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BulletFiredEvent.class, "BulletFiredEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BulletHitBotEvent.class, "BulletHitBotEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BulletHitBulletEvent.class, "BulletHitBulletEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.BulletHitWallEvent.class, "BulletHitWallEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.ScannedBotEvent.class, "ScannedBotEvent")
            .registerSubtype(dev.robocode.tankroyale.schema.WonRoundEvent.class, "WonRoundEvent");

    gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
  }

  public BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, URI serverUrl) {
    this.baseBot = baseBot;
    this.botInfo = (botInfo == null) ? EnvVars.getBotInfo() : botInfo;

    this.botEventHandlers = new BotEventHandlers(baseBot);
    this.eventQueue = new EventQueue(this, botEventHandlers);

    this.serverUrl = serverUrl == null ? getServerUrlFromSetting() : serverUrl;

    init();
  }

  private void init() {
    botEventHandlers.onRoundStarted.subscribe(this::onRoundStarted, 100);
    botEventHandlers.onNextTurn.subscribe(this::onNextTurn, 100);
    botEventHandlers.onBulletFired.subscribe(this::onBulletFired, 100);
  }

  public void setStopResumeHandler(IStopResumeListener listener) {
    stopResumeListener = listener;
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

  private void onRoundStarted(RoundStartedEvent e) {
    botIntent = newBotIntent();
    eventQueue.clear();
    isStopped = false;
  }

  private void onNextTurn(TickEvent e) {
    synchronized (nextTurnMonitor) {
      // Unblock methods waiting for the next turn
      nextTurnMonitor.notifyAll();
    }
  }

  private void onBulletFired(BulletFiredEvent e) {
    botIntent.setFirepower(0d); // Reset firepower so the bot stops firing continuously
  }

  public void start() {
    connect();
    try {
      closedLatch.await();
    } catch (InterruptedException ignore) {
    }
  }

  private void connect() {
    try {
      HttpClient httpClient = HttpClient.newBuilder().build();
      Builder webSocketBuilder = httpClient.newWebSocketBuilder();
      socket = webSocketBuilder.buildAsync(serverUrl, new WebSocketListener()).join();
    } catch (Exception ex) {
      throw new BotException("Could not create socket for URL: " + serverUrl, ex);
    }
  }

  public void execute() {
    sendIntent();
    waitForNextTurn();
    dispatchEvents();
  }

  public void sendIntent() {
    limitTargetSpeedAndTurnRates();
    socket.sendText(gson.toJson(botIntent), true);
  }

  private void waitForNextTurn() {
    int turnNumber = getCurrentTick().getTurnNumber();

    synchronized (nextTurnMonitor) {
      try {
        while (turnNumber >= getCurrentTick().getTurnNumber()) {
          nextTurnMonitor.wait(); // Wait for next turn
        }
      } catch (InterruptedException ignore) {
      }
    }
  }

  private void dispatchEvents() {
    try {
      eventQueue.dispatchEvents(getCurrentTick().getTurnNumber());
    } catch (RescanException e) {
      // Do nothing (event handler was stopped by this exception)
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void limitTargetSpeedAndTurnRates() {
    Double targetSpeed = botIntent.getTargetSpeed();
    if (targetSpeed != null) {
      botIntent.setTargetSpeed(clamp(targetSpeed, -maxSpeed, maxSpeed));
    }
    Double turnRate = botIntent.getTurnRate();
    if (turnRate != null) {
      botIntent.setTurnRate(clamp(turnRate, -maxTurnRate, maxTurnRate));
    }
    Double gunTurnRate = botIntent.getGunTurnRate();
    if (gunTurnRate != null) {
      botIntent.setGunTurnRate(clamp(gunTurnRate, -maxGunTurnRate, maxGunTurnRate));
    }
    Double radarTurnRate = botIntent.getRadarTurnRate();
    if (radarTurnRate != null) {
      botIntent.setRadarTurnRate(clamp(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate));
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
    this.maxSpeed = clamp(maxSpeed, 0, MAX_SPEED);
  }

  public void setMaxTurnRate(double maxTurnRate) {
    this.maxTurnRate = clamp(maxTurnRate, 0, MAX_TURN_RATE);
  }

  public void setMaxGunTurnRate(double maxGunTurnRate) {
    this.maxGunTurnRate = clamp(maxGunTurnRate, 0, MAX_GUN_TURN_RATE);
  }

  public void setMaxRadarTurnRate(double maxRadarTurnRate) {
    this.maxRadarTurnRate = clamp(maxRadarTurnRate, 0, MAX_RADAR_TURN_RATE);
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
      return clamp(targetSpeed, speed - absDeceleration, speed + ACCELERATION);
    }
    return clamp(targetSpeed, speed - ACCELERATION, speed + getMaxDeceleration(-speed));
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

  public void setScan(boolean doScan) {
    botIntent.setScan(doScan);
  }

  public void setStop() {
    if (!isStopped) {
      isStopped = true;

      savedTargetSpeed = botIntent.getTargetSpeed();
      savedTurnRate = botIntent.getTurnRate();
      savedGunTurnRate = botIntent.getGunTurnRate();
      savedRadarTurnRate = botIntent.getRadarTurnRate();

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
      botIntent.setTargetSpeed(savedTargetSpeed);
      botIntent.setTurnRate(savedTurnRate);
      botIntent.setGunTurnRate(savedGunTurnRate);
      botIntent.setRadarTurnRate(savedRadarTurnRate);

      if (stopResumeListener != null) {
        stopResumeListener.onResume();
      }
      isStopped = false; // must be last step
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

  private final class WebSocketListener implements Listener {

    StringBuffer payload = new StringBuffer();

    @Override
    public void onOpen(WebSocket websocket) {
      BaseBotInternals.this.socket = websocket; // To prevent null pointer exception

      botEventHandlers.onConnected.publish(new ConnectedEvent(serverUrl));
      Listener.super.onOpen(websocket);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket websocket, int statusCode, String reason) {
      botEventHandlers.onDisconnected.publish(new DisconnectedEvent(serverUrl, true));
      closedLatch.countDown();
      return null;
    }

    @Override
    public void onError(WebSocket websocket, Throwable error) {
      botEventHandlers.onConnectionError.publish(new ConnectionErrorEvent(serverUrl, error));
      System.err.println(error.getLocalizedMessage());
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
      payload.append(data);
      if (last) {
        JsonObject jsonMsg = gson.fromJson(payload.toString(), JsonObject.class);
        payload.delete(0, payload.length()); // clear payload buffer

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
            case ROUND_STARTED_EVENT:
              handleRoundStartedEvent(jsonMsg);
              break;
            case ROUND_ENDED_EVENT:
              handleRoundEndedEvent(jsonMsg);
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
            case GAME_ABORTED_EVENT:
              break;
            default:
              throw new BotException("Unsupported WebSocket message type: " + type);
          }
        }
      }
      return Listener.super.onText(webSocket, data, last);
    }

    private void handleTickEvent(JsonObject jsonMsg) {
      TickEventForBot tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
      tickEvent = EventMapper.map(tickEventForBot);

      tickStartNanoTime = System.nanoTime();

      if (botIntent.getScan() != null && botIntent.getScan()) {
        setScan(false);
      }

      eventQueue.addEventsFromTick(tickEvent, baseBot);

      // Trigger next turn (not tick-event!)
      botEventHandlers.onNextTurn.publish(tickEvent);
    }

    private void handleRoundStartedEvent(JsonObject jsonMsg) {
      dev.robocode.tankroyale.schema.RoundStartedEvent roundStartedEvent =
              gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.RoundStartedEvent.class);

      botEventHandlers.onRoundStarted.publish(new RoundStartedEvent(roundStartedEvent.getRoundNumber()));
    }

    private void handleRoundEndedEvent(JsonObject jsonMsg) {
      dev.robocode.tankroyale.schema.RoundEndedEvent roundEndedEvent =
              gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.RoundEndedEvent.class);

      botEventHandlers.onRoundEnded.publish(new RoundEndedEvent(
              roundEndedEvent.getRoundNumber(), roundEndedEvent.getTurnNumber()));
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
      socket.sendText(msg, true);

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

  private void handleServerHandshake(JsonObject jsonMsg) {
    serverHandshake = gson.fromJson(jsonMsg, ServerHandshake.class);

    // Reply by sending bot handshake
    BotHandshake botHandshake = BotHandshakeFactory.create(botInfo);
    String msg = gson.toJson(botHandshake);

    socket.sendText(msg, true);
  }

  private void handleSkippedTurnEvent(JsonObject jsonMsg) {
    dev.robocode.tankroyale.schema.SkippedTurnEvent skippedTurnEvent =
            gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

    botEventHandlers.onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent));
  }
}