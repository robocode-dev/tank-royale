package dev.robocode.tankroyale.botapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.neovisionaries.ws.client.*;
import dev.robocode.tankroyale.botapi.events.BotDeathEvent;
import dev.robocode.tankroyale.botapi.events.BotHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BotHitWallEvent;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBulletEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitWallEvent;
import dev.robocode.tankroyale.botapi.events.Event;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.WonRoundEvent;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.factory.BotHandshakeFactory;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.botapi.mapper.ResultsMapper;
import dev.robocode.tankroyale.schema.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Abstract bot class that takes care of communication between the bot and the server, and sends
 * notifications through the event handlers. Most bots can inherit from this class to get access to
 * basic methods.
 */
public abstract class BaseBot implements IBaseBot {

  final __Internals __internals;

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * both BotInfo and server URL is provided through environment variables, i.e., when starting up
   * the bot using a bootstrap. These environment variables must be set to provide the server URL
   * and bot information, and are automatically set by the bootstrap tool for Robocode.
   *
   * <p><b>Example of how to set the predefined environment variables:</b>
   *
   * <p>ROBOCODE_SERVER_URL=ws://localhost<br>
   * BOT_NAME=MyBot<br>
   * BOT_VERSION=1.0<br>
   * BOT_AUTHOR=fnl<br>
   * BOT_DESCRIPTION=Sample bot<br>
   * BOT_URL=https://mybot.somewhere.net
   * BOT_COUNTRY_CODE=DK<br>
   * BOT_GAME_TYPES=melee,1v1<br>
   * BOT_PLATFORM=Java<br>
   * BOT_PROG_LANG=Java 8<br>
   */
  public BaseBot() {
    __internals = new __Internals(null, null);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * server URL is provided through the environment variable ROBOCODE_SERVER_URL.
   *
   * @param botInfo is the bot info containing information about your bot.
   */
  public BaseBot(final BotInfo botInfo) {
    __internals = new __Internals(botInfo, null);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used
   * providing both the bot information and server URL for your bot.
   *
   * @param botInfo is the bot info containing information about your bot.
   * @param serverUrl is the server URL
   */
  public BaseBot(final BotInfo botInfo, URI serverUrl) {
    __internals = new __Internals(botInfo, serverUrl);
  }

  /** {@inheritDoc} */
  @Override
  public final void start() {
    __internals.connect();
  }

  /** {@inheritDoc} */
  @Override
  public final void go() {
    // Send the bot intent to the server
    __internals.sendIntent();
  }

  /** {@inheritDoc} */
  @Override
  public final String getVariant() {
    return __internals.getServerHandshake().getVariant();
  }

  /** {@inheritDoc} */
  @Override
  public final String getVersion() {
    return __internals.getServerHandshake().getVersion();
  }

  /** {@inheritDoc} */
  @Override
  public final int getMyId() {
    return __internals.getMyId();
  }

  /** {@inheritDoc} */
  @Override
  public final String getGameType() {
    return __internals.getGameSetup().getGameType();
  }

  /** {@inheritDoc} */
  @Override
  public final int getArenaWidth() {
    return __internals.getGameSetup().getArenaWidth();
  }

  /** {@inheritDoc} */
  @Override
  public final int getArenaHeight() {
    return __internals.getGameSetup().getArenaHeight();
  }

  /** {@inheritDoc} */
  @Override
  public final int getNumberOfRounds() {
    return __internals.getGameSetup().getNumberOfRounds();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunCoolingRate() {
    return __internals.getGameSetup().getGunCoolingRate();
  }

  /** {@inheritDoc} */
  @Override
  public final int getMaxInactivityTurns() {
    return __internals.getGameSetup().getMaxInactivityTurns();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTurnTimeout() {
    return __internals.getGameSetup().getTurnTimeout();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTimeLeft() {
    long passesMicroSeconds = (System.nanoTime() - __internals.getTicksStart()) / 1000;
    return (int) (__internals.getGameSetup().getTurnTimeout() - passesMicroSeconds);
  }

  /** {@inheritDoc} */
  @Override
  public final int getRoundNumber() {
    return __internals.getCurrentTurn().getRoundNumber();
  }

  /** {@inheritDoc} */
  @Override
  public final int getTurnNumber() {
    return __internals.getCurrentTurn().getTurnNumber();
  }

  /** {@inheritDoc} */
  @Override
  public final double getEnergy() {
    return __internals.getCurrentTurn().getBotState().getEnergy();
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isDisabled() {
    return getEnergy() == 0;
  }

  /** {@inheritDoc} */
  @Override
  public final double getX() {
    return __internals.getCurrentTurn().getBotState().getX();
  }

  /** {@inheritDoc} */
  @Override
  public final double getY() {
    return __internals.getCurrentTurn().getBotState().getY();
  }

  /** {@inheritDoc} */
  @Override
  public final double getDirection() {
    return __internals.getCurrentTurn().getBotState().getDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunDirection() {
    return __internals.getCurrentTurn().getBotState().getGunDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarDirection() {
    return __internals.getCurrentTurn().getBotState().getRadarDirection();
  }

  /** {@inheritDoc} */
  @Override
  public final double getSpeed() {
    return __internals.getCurrentTurn().getBotState().getSpeed();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunHeat() {
    return __internals.getCurrentTurn().getBotState().getGunHeat();
  }

  /** {@inheritDoc} */
  @Override
  public final Collection<BulletState> getBulletStates() {
    return __internals.getCurrentTurn().getBulletStates();
  }

  /** {@inheritDoc} */
  @Override
  public final Collection<? extends Event> getEvents() {
    return __internals.getCurrentTurn().getEvents();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRate(double turnRate) {
    if (Double.isNaN(turnRate)) {
      throw new IllegalArgumentException("turnRate cannot be NaN");
    }
    if (Math.abs(turnRate) > MAX_TURN_RATE) {
      turnRate = MAX_TURN_RATE * (turnRate > 0 ? 1 : -1);
    }
    __internals.botIntent.setTurnRate(turnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getTurnRate() {
    Double turnRate = __internals.botIntent.getTurnRate();
    if (turnRate == null) {
      return 0;
    }
    return turnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setGunTurnRate(double gunTurnRate) {
    if (Double.isNaN(gunTurnRate)) {
      throw new IllegalArgumentException("gunTurnRate cannot be NaN");
    }
    if (isAdjustGunForBodyTurn()) {
      gunTurnRate -= getTurnRate();
    }
    if (Math.abs(gunTurnRate) > MAX_GUN_TURN_RATE) {
      gunTurnRate = MAX_GUN_TURN_RATE * (gunTurnRate > 0 ? 1 : -1);
    }
    __internals.botIntent.setGunTurnRate(gunTurnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunTurnRate() {
    Double turnRate = __internals.botIntent.getGunTurnRate();
    if (turnRate == null) {
      return 0;
    }
    return turnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setRadarTurnRate(double radarTurnRate) {
    if (Double.isNaN(radarTurnRate)) {
      throw new IllegalArgumentException("radarTurnRate cannot be NaN");
    }
    if (isAdjustRadarForGunTurn()) {
      radarTurnRate -= getGunTurnRate();
    }
    if (Math.abs(radarTurnRate) > MAX_RADAR_TURN_RATE) {
      radarTurnRate = MAX_RADAR_TURN_RATE * (radarTurnRate > 0 ? 1 : -1);
    }
    __internals.botIntent.setRadarTurnRate(radarTurnRate);
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarTurnRate() {
    Double turnRate = __internals.botIntent.getRadarTurnRate();
    if (turnRate == null) {
      return 0;
    }
    return turnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTargetSpeed(double targetSpeed) {
    if (Double.isNaN(targetSpeed)) {
      throw new IllegalArgumentException("targetSpeed cannot be NaN");
    }
    if (targetSpeed > MAX_FORWARD_SPEED) {
      targetSpeed = MAX_FORWARD_SPEED;
    } else if (targetSpeed < MAX_BACKWARD_SPEED) {
      targetSpeed = MAX_BACKWARD_SPEED;
    }
    __internals.botIntent.setTargetSpeed(targetSpeed);
  }

  /** {@inheritDoc} */
  @Override
  public final double getTargetSpeed() {
    return __internals.botIntent.getTargetSpeed();
  }

  /** {@inheritDoc} */
  @Override
  public final void setFirepower(double firepower) {
    if (Double.isNaN(firepower)) {
      throw new IllegalArgumentException("firepower cannot be NaN");
    }
    if (getGunHeat() == 0) {
      if (firepower < MIN_FIREPOWER) {
        firepower = 0;
      } else if (firepower > MAX_FIREPOWER) {
        firepower = MAX_FIREPOWER;
      }
      __internals.botIntent.setFirepower(firepower);
    }
  }

  /** {@inheritDoc} */
  @Override
  public final double getFirepower() {
    return __internals.botIntent.getFirepower();
  }

  /** {@inheritDoc} */
  @Override
  public final void setAdjustGunForBodyTurn(boolean adjust) {
    __internals.isAdjustGunForBodyTurn = adjust;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isAdjustGunForBodyTurn() {
    return __internals.isAdjustGunForBodyTurn;
  }

  /** {@inheritDoc} */
  @Override
  public final void setAdjustRadarForGunTurn(boolean adjust) {
    __internals.isAdjustRadarForGunTurn = adjust;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isAdjustRadarForGunTurn() {
    return __internals.isAdjustRadarForGunTurn;
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getBodyColor() {
    return __internals.getCurrentTurn().getBotState().getBodyColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBodyColor(String bodyColor) {
    __internals.botIntent.setBodyColor(bodyColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getTurretColor() {
    return __internals.getCurrentTurn().getBotState().getTurretColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurretColor(String turretColor) {
    __internals.botIntent.setTurretColor(turretColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getRadarColor() {
    return __internals.getCurrentTurn().getBotState().getRadarColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setRadarColor(String radarColor) {
    __internals.botIntent.setRadarColor(radarColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getBulletColor() {
    return __internals.getCurrentTurn().getBotState().getBulletColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBulletColor(String bulletColor) {
    __internals.botIntent.setBulletColor(bulletColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getScanColor() {
    return __internals.getCurrentTurn().getBotState().getScanColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setScanColor(String scanColor) {
    __internals.botIntent.setScanColor(scanColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getTracksColor() {
    return __internals.getCurrentTurn().getBotState().getTracksColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTracksColor(String tracksColor) {
    __internals.botIntent.setTracksColor(tracksColor);
  }

  /** {@inheritDoc} */
  @Override
  public final Integer getGunColor() {
    return __internals.getCurrentTurn().getBotState().getGunColor();
  }

  /** {@inheritDoc} */
  @Override
  public final void setGunColor(String gunColor) {
    __internals.botIntent.setGunColor(gunColor);
  }

  /** {@inheritDoc} */
  @Override
  public final double calcMaxTurnRate(double speed) {
    return MAX_TURN_RATE - 0.75 * Math.abs(speed);
  }

  /** {@inheritDoc} */
  @Override
  public final double calcBulletSpeed(double firepower) {
    return 20 - 3 * firepower;
  }

  /** {@inheritDoc} */
  @Override
  public final double calcGunHeat(double firepower) {
    return 1 + (firepower / 5);
  }

  protected final class __Internals {
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
              .registerSubtype(
                  dev.robocode.tankroyale.schema.BotHitBotEvent.class, "BotHitBotEvent")
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

    private final BotInfo botInfo;

    private final BotIntent botIntent = new BotIntent();

    // Server connection:
    private WebSocket socket;
    private ServerHandshake serverHandshake;

    // Current game states:
    private Integer myId;
    private GameSetup gameSetup;
    private TickEvent currentTurn;
    private Long ticksStartNanoTime;

    // Adjustment of turn rates
    private boolean isAdjustGunForBodyTurn;
    private boolean isAdjustRadarForGunTurn;

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

    __Internals(BotInfo botInfo, URI serverUrl) {
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

      onConnected.subscribe(BaseBot.this::onConnected);
      onDisconnected.subscribe(BaseBot.this::onDisconnected);
      onConnectionError.subscribe(BaseBot.this::onConnectionError);
      onGameStarted.subscribe(BaseBot.this::onGameStarted);
      onGameEnded.subscribe(BaseBot.this::onGameEnded);
      onTick.subscribe(BaseBot.this::onTick);
      onSkippedTurn.subscribe(BaseBot.this::onSkippedTurn);
      onDeath.subscribe(BaseBot.this::onDeath);
      onBotDeath.subscribe(BaseBot.this::onBotDeath);
      onHitBot.subscribe(BaseBot.this::onHitBot);
      onHitWall.subscribe(BaseBot.this::onHitWall);
      onBulletFired.subscribe(BaseBot.this::onBulletFired);
      onHitByBullet.subscribe(BaseBot.this::onHitByBullet);
      onBulletHit.subscribe(BaseBot.this::onBulletHit);
      onBulletHitBullet.subscribe(BaseBot.this::onBulletHitBullet);
      onBulletHitWall.subscribe(BaseBot.this::onBulletHitWall);
      onScannedBot.subscribe(BaseBot.this::onScannedBot);
      onWonRound.subscribe(BaseBot.this::onWonRound);
    }

    private void connect() {
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

    private void sendIntent() {
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

    private ServerHandshake getServerHandshake() {
      if (serverHandshake == null) {
        throw new BotException(NOT_CONNECTED_TO_SERVER_MSG);
      }
      return serverHandshake;
    }

    private int getMyId() {
      if (myId == null) {
        throw new BotException(GAME_NOT_RUNNING_MSG);
      }
      return myId;
    }

    private GameSetup getGameSetup() {
      if (gameSetup == null) {
        throw new BotException(GAME_NOT_RUNNING_MSG);
      }
      return gameSetup;
    }

    private TickEvent getCurrentTurn() {
      if (currentTurn == null) {
        throw new BotException(TICK_NOT_AVAILABLE_MSG);
      }
      return currentTurn;
    }

    private long getTicksStart() {
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

        onDisconnected.publish(
            new DisconnectedEvent(websocket.getURI(), closedByServer));

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
      GameEndedEventForBot gameEndedEventForBot =
          gson.fromJson(jsonMsg, GameEndedEventForBot.class);

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
    protected class Event<T> {
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
}
