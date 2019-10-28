package dev.robocode.tankroyale.botapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.neovisionaries.ws.client.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.factory.BotHandshakeFactory;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.schema.*;
import lombok.val;
import lombok.var;
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
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.botapi.mapper.ResultsMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

@SuppressWarnings("UnusedDeclaration")
public abstract class BasicBot implements IBasicBot {

  protected final __Internals __internals;

  /**
   * Constructor used when both BotInfo and serverUri are provided through environment variables.
   * This constructor should be used when starting up the bot using a bootstrap. These environment
   * variables must be set to provide the server URI and bot information, and are automatically set
   * by the bootstrap tool for Robocode. ROBOCODE_SERVER_URI, BOT_NAME, BOT_VERSION, BOT_AUTHOR,
   * BOT_DESCRIPTION, BOT_COUNTRY_CODE, BOT_GAME_TYPES, BOT_PROG_LANG.
   *
   * <p><b>Example:</b>
   *
   * <p>ROBOCODE_SERVER_URI=ws://localhost:55000<br>
   * BOT_NAME=MyBot<br>
   * BOT_VERSION=1.0<br>
   * BOT_AUTHOR=fnl<br>
   * BOT_DESCRIPTION=Sample bot<br>
   * BOT_COUNTRY_CODE=DK<br>
   * BOT_GAME_TYPES=melee,1v1<br>
   * BOT_PROG_LANG=Java<br>
   */
  @SuppressWarnings("WeakerAccess")
  public BasicBot() {
    __internals = new __Internals(null, null);
  }

  /**
   * Constructor used when serverUri is provided through the environment variable
   * ROBOCODE_SERVER_URI.
   *
   * @param botInfo is the bot info containing information about your bot.
   */
  public BasicBot(final BotInfo botInfo) {
    __internals = new __Internals(botInfo, null);
  }

  /**
   * Constructor used when you want to provide both the bot information and server URI for your bot.
   *
   * @param botInfo is the bot info containing information about your bot.
   * @param serverUri is the server URI
   */
  @SuppressWarnings("UnusedDeclaration")
  public BasicBot(final BotInfo botInfo, URI serverUri) {
    __internals = new __Internals(botInfo, serverUri);
  }

  @Override
  public final void run() {
    __internals.connect();
  }

  @Override
  public void go() {
    __internals.sendBotIntent();
  }

  @Override
  public final String getVariant() {
    return __internals.getServerHandshake().getVariant();
  }

  @Override
  public final String getVersion() {
    return __internals.getServerHandshake().getVersion();
  }

  @Override
  public final int getMyId() {
    return __internals.getMyId();
  }

  @Override
  public final String getGameType() {
    return __internals.getGameSetup().getGameType();
  }

  @Override
  public final int getArenaWidth() {
    return __internals.getGameSetup().getArenaWidth();
  }

  @Override
  public final int getArenaHeight() {
    return __internals.getGameSetup().getArenaHeight();
  }

  @Override
  public final int getNumberOfRounds() {
    return __internals.getGameSetup().getNumberOfRounds();
  }

  @Override
  public final double getGunCoolingRate() {
    return __internals.getGameSetup().getGunCoolingRate();
  }

  @Override
  public final int getMaxInactivityTurns() {
    return __internals.getGameSetup().getMaxInactivityTurns();
  }

  @Override
  public final int getTurnTimeout() {
    return __internals.getGameSetup().getTurnTimeout();
  }

  @Override
  public final int getRoundNumber() {
    return __internals.getCurrentTurn().getRoundNumber();
  }

  @Override
  public final int getTurnNumber() {
    return __internals.getCurrentTurn().getTurnNumber();
  }

  @Override
  public final double getEnergy() {
    return __internals.getCurrentTurn().getBotState().getEnergy();
  }

  @Override
  public final boolean isDisabled() {
    return getEnergy() == 0;
  }

  @Override
  public final double getX() {
    return __internals.getCurrentTurn().getBotState().getX();
  }

  @Override
  public final double getY() {
    return __internals.getCurrentTurn().getBotState().getY();
  }

  @Override
  public final double getDirection() {
    return __internals.getCurrentTurn().getBotState().getDirection();
  }

  @Override
  public final double getGunDirection() {
    return __internals.getCurrentTurn().getBotState().getGunDirection();
  }

  @Override
  public final double getRadarDirection() {
    return __internals.getCurrentTurn().getBotState().getRadarDirection();
  }

  @Override
  public final double getSpeed() {
    return __internals.getCurrentTurn().getBotState().getSpeed();
  }

  @Override
  public final double getGunHeat() {
    return __internals.getCurrentTurn().getBotState().getGunHeat();
  }

  @Override
  public final List<BulletState> getBulletStates() {
    return __internals.getCurrentTurn().getBulletStates();
  }

  @Override
  public final List<? extends Event> getEvents() {
    return __internals.getCurrentTurn().getEvents();
  }

  @Override
  public final void setTurnRate(double turnRate) {
    __internals.botIntent.setTurnRate(turnRate);
  }

  @Override
  public final void setGunTurnRate(double gunTurnRate) {
    __internals.botIntent.setGunTurnRate(gunTurnRate);
  }

  @Override
  public final void setRadarTurnRate(double radarTurnRate) {
    __internals.botIntent.setRadarTurnRate(radarTurnRate);
  }

  @Override
  public final void setTargetSpeed(double targetSpeed) {
    __internals.botIntent.setTargetSpeed(targetSpeed);
  }

  @Override
  public final void setFire(double firepower) {
    __internals.botIntent.setFirepower(firepower);
  }

  @Override
  public double calcMaxTurnRate(double speed) {
    return MAX_TURN_RATE - 0.75 * Math.abs(speed);
  }

  @Override
  public final double calcBulletSpeed(double firepower) {
    return 20 - 3 * firepower;
  }

  @Override
  public final double calcGunHeat(double firepower) {
    return 1 + (firepower / 5);
  }

  @Override
  public final double normalAbsoluteDegrees(double angle) {
    return (angle %= 360) >= 0 ? angle : (angle + 360);
  }

  @Override
  public final double normalRelativeDegrees(double angle) {
    return (angle %= 360) >= 0
        ? ((angle < 180) ? angle : (angle - 360))
        : ((angle >= -180) ? angle : (angle + 360));
  }

  private final class __Internals {
    private static final String SERVER_URI_ENV_VAR_NAME = "SERVER_URI";
    private static final String SERVER_URI_PROPERTY_KEY = "server.uri";

    private static final String NOT_CONNECTED_TO_SERVER_MSG =
        "Not connected to game server yes. Make sure onConnected() event handler has been called first";

    private static final String GAME_NOT_RUNNING_MSG =
        "Game is not running. Make sure onGameStarted() event handler has been called first";

    private static final String TICK_NOT_AVAILABLE_MSG =
        "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

    private final Gson gson;

    {
      val typeFactory =
          RuntimeTypeAdapterFactory.of(dev.robocode.tankroyale.schema.Event.class)
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
    private WebSocket webSocket;
    private ServerHandshake serverHandshake;

    // Current game states:
    private Integer myId;
    private GameSetup gameSetup;
    private TickEvent currentTurn;

    private final Event<ConnectedEvent> onConnected = new Event<>();
    private final Event<DisconnectedEvent> onDisconnected = new Event<>();
    private final Event<ConnectionErrorEvent> onConnectionError = new Event<>();
    private final Event<GameStartedEvent> onGameStarted = new Event<>();
    private final Event<GameEndedEvent> onGameEnded = new Event<>();
    private final Event<TickEvent> onTick = new Event<>();
    private final Event<SkippedTurnEvent> onSkippedTurn = new Event<>();
    private final Event<BotDeathEvent> onBotDeath = new Event<>();
    private final Event<BotHitBotEvent> onHitBot = new Event<>();
    private final Event<BotHitWallEvent> onHitWall = new Event<>();
    private final Event<BulletFiredEvent> onBulletFired = new Event<>();
    private final Event<BulletHitBotEvent> onHitByBullet = new Event<>();
    private final Event<BulletHitBotEvent> onBulletHit = new Event<>();
    private final Event<BulletHitBulletEvent> onBulletHitBullet = new Event<>();
    private final Event<BulletHitWallEvent> onBulletHitWall = new Event<>();
    private final Event<ScannedBotEvent> onScannedBot = new Event<>();
    private final Event<WonRoundEvent> onWonRound = new Event<>();

    __Internals(BotInfo botInfo, URI serverUri) {
      this.botInfo = (botInfo == null) ? Env.getBotInfo() : botInfo;
      init(serverUri == null ? getServerUriFromSetting() : serverUri);
    }

    private void init(URI serverUri) {
      try {
        onConnected.subscribe(
            event -> {
              BasicBot.this.onConnected(event);
              return null;
            });
        onDisconnected.subscribe(
            event -> {
              BasicBot.this.onDisconnected(event);
              return null;
            });
        onConnectionError.subscribe(
            event -> {
              BasicBot.this.onConnectionError(event);
              return null;
            });
        onGameStarted.subscribe(
            event -> {
              BasicBot.this.onGameStarted(event);
              return null;
            });
        onGameEnded.subscribe(
            event -> {
              BasicBot.this.onGameEnded(event);
              return null;
            });
        onTick.subscribe(
            event -> {
              BasicBot.this.onTick(event);
              return null;
            });
        onSkippedTurn.subscribe(
            event -> {
              BasicBot.this.onSkippedTurn(event);
              return null;
            });
        onBotDeath.subscribe(
            event -> {
              BasicBot.this.onBotDeath(event);
              return null;
            });
        onHitBot.subscribe(
            event -> {
              BasicBot.this.onHitBot(event);
              return null;
            });
        onHitWall.subscribe(
            event -> {
              BasicBot.this.onHitWall(event);
              return null;
            });
        onBulletFired.subscribe(
            event -> {
              BasicBot.this.onBulletFired(event);
              return null;
            });
        onHitByBullet.subscribe(
            event -> {
              BasicBot.this.onHitByBullet(event);
              return null;
            });
        onBulletHit.subscribe(
            event -> {
              BasicBot.this.onBulletHit(event);
              return null;
            });
        onBulletHitBullet.subscribe(
            event -> {
              BasicBot.this.onBulletHitBullet(event);
              return null;
            });
        onBulletHitWall.subscribe(
            event -> {
              BasicBot.this.onBulletHitWall(event);
              return null;
            });
        onScannedBot.subscribe(
            event -> {
              BasicBot.this.onScannedBot(event);
              return null;
            });
        onWonRound.subscribe(
            event -> {
              BasicBot.this.onWonRound(event);
              return null;
            });

        webSocket = new WebSocketFactory().createSocket(serverUri);
        webSocket.addListener(new WebSocketListener());

      } catch (IOException ex) {
        throw new RuntimeException("Could not create web socket for URI: " + serverUri, ex);
      }
      botIntent.setType(BotReady.Type.BOT_INTENT); // must be set!
    }

    private void connect() {
      if (!webSocket.isOpen()) {
        try {
          webSocket.connect();
        } catch (WebSocketException ex) {
          throw new RuntimeException("Could not connect to web socket", ex);
        }
      }
    }

    private void sendBotIntent() {
      webSocket.sendText(gson.toJson(botIntent));
    }

    private void clearCurrentGameState() {
      // Clear setting that are only available during a running game
      currentTurn = null;
      gameSetup = null;
      myId = null;
    }

    private URI getServerUriFromSetting() {
      var uri = System.getenv(SERVER_URI_ENV_VAR_NAME);
      if (uri == null) {
        uri = System.getProperty(SERVER_URI_PROPERTY_KEY);
        if (uri == null) {
          uri = Env.getServerUri();
        }
      }
      if (uri == null) {
        throw new BotException(
            String.format(
                "Property %s or system environment variable %s is not defined",
                SERVER_URI_PROPERTY_KEY, Env.SERVER_URI));
      }
      try {
        return new URI(uri);
      } catch (URISyntaxException ex) {
        throw new BotException("Incorrect syntax for server uri: " + uri);
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

    private final class WebSocketListener extends WebSocketAdapter {

      @Override
      public final void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
        val event = ConnectedEvent.builder().build();
        onConnected.publish(event);
      }

      @Override
      public final void onDisconnected(
          WebSocket websocket,
          WebSocketFrame serverCloseFrame,
          WebSocketFrame clientCloseFrame,
          boolean closedByServer) {

        val event = DisconnectedEvent.builder().remote(closedByServer).build();
        onDisconnected.publish(event);

        BasicBot.this.__internals.clearCurrentGameState();
      }

      @Override
      public final void onError(WebSocket websocket, WebSocketException cause) {
        val event = ConnectionErrorEvent.builder().exception(cause).build();
        onConnectionError.publish(event);
      }

      @Override
      public final void onTextMessage(WebSocket websocket, String text) {
        JsonObject jsonMsg = gson.fromJson(text, JsonObject.class);

        JsonElement jsonType = jsonMsg.get("type");
        if (jsonType != null) {
          val type = jsonType.getAsString();

          switch (dev.robocode.tankroyale.schema.Message.Type.fromValue(type)) {
            case SKIPPED_TURN_EVENT: // FIXME: Already handled somewhere else
              handleSkippedTurnEvent(jsonMsg);
              break;
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
            default:
              throw new BotException("Unsupported WebSocket message type: " + type);
          }
        }
      }

      private void handleServerHandshake(JsonObject jsonMsg) {
        serverHandshake = gson.fromJson(jsonMsg, ServerHandshake.class);

        // Send bot handshake
        val botHandshake = BotHandshakeFactory.create(BasicBot.__Internals.this.botInfo);
        val msg = gson.toJson(botHandshake);

        webSocket.sendText(msg);
      }

      private void handleGameStartedEvent(JsonObject jsonMsg) {
        val gameStartedEventForBot = gson.fromJson(jsonMsg, GameStartedEventForBot.class);
        val gameSetup = gameStartedEventForBot.getGameSetup();

        BasicBot.__Internals.this.myId = gameStartedEventForBot.getMyId();
        BasicBot.__Internals.this.gameSetup = GameSetupMapper.map(gameSetup);

        // Send ready signal
        BotReady ready = new BotReady();
        ready.setType(BotReady.Type.BOT_READY);

        val msg = gson.toJson(ready);
        webSocket.sendText(msg);

        val gameStartedEvent =
            GameStartedEvent.builder()
                .myId(gameStartedEventForBot.getMyId())
                .gameSetup(BasicBot.__Internals.this.gameSetup)
                .build();

        onGameStarted.publish(gameStartedEvent);
      }
    }

    private void handleGameEndedEvent(JsonObject jsonMsg) {
      // Clear current game state
      BasicBot.this.__internals.clearCurrentGameState();

      // Send the game ended event
      val gameEndedEventForBot = gson.fromJson(jsonMsg, GameEndedEventForBot.class);
      val gameEndedEvent =
          GameEndedEvent.builder()
              .numberOfRounds(gameEndedEventForBot.getNumberOfRounds())
              .results(ResultsMapper.map(gameEndedEventForBot.getResults()))
              .build();

      onGameEnded.publish(gameEndedEvent);
    }

    private void handleTickEvent(JsonObject jsonMsg) {
      val tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
      currentTurn = EventMapper.map(tickEventForBot);

      // Dispatch all on the tick event before the tick event itself
      dispatchEvents(currentTurn);

      onTick.publish(currentTurn);
    }

    private void handleSkippedTurnEvent(JsonObject jsonMsg) {
      val skippedTurnEvent =
          gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

      onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent));
    }

    private void dispatchEvents(TickEvent tickEvent) {
      tickEvent
          .getEvents()
          .forEach(
              event -> {
                if (event instanceof BotDeathEvent) {
                  onBotDeath.publish((BotDeathEvent) event);
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
                } else if (event
                    instanceof SkippedTurnEvent) { // FIXME: Already handled somewhere else
                  onSkippedTurn.publish((SkippedTurnEvent) event);
                } else if (event instanceof WonRoundEvent) {
                  onWonRound.publish((WonRoundEvent) event);
                }
              });
    }

    private class Event<T> {
      private List<UnaryOperator<T>> subscribers = Collections.synchronizedList(new ArrayList<>());

      void subscribe(UnaryOperator<T> subscriber) {
        subscribers.add(subscriber);
      }

      void publish(T source) {
        for (UnaryOperator<T> subscriber : subscribers) {
          subscriber.apply(source);
        }
      }
    }
  }
}
