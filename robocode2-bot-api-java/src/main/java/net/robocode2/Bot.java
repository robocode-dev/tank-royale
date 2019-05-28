package net.robocode2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import net.robocode2.events.BotDeathEvent;
import net.robocode2.events.BotHitBotEvent;
import net.robocode2.events.BotHitWallEvent;
import net.robocode2.events.BulletFiredEvent;
import net.robocode2.events.BulletHitBotEvent;
import net.robocode2.events.BulletHitBulletEvent;
import net.robocode2.events.BulletHitWallEvent;
import net.robocode2.events.Event;
import net.robocode2.events.ScannedBotEvent;
import net.robocode2.events.SkippedTurnEvent;
import net.robocode2.events.WonRoundEvent;
import net.robocode2.events.*;
import net.robocode2.factory.BotHandshakeFactory;
import net.robocode2.mapper.EventMapper;
import net.robocode2.mapper.GameSetupMapper;
import net.robocode2.mapper.ResultsMapper;
import net.robocode2.schema.*;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class Bot implements IBot {

  private final __Internals __internals;

  // Default constructor is not allowed and thus must be hidden
  private Bot() {
    __internals = new __Internals(null);
  }

  public Bot(@NonNull final BotInfo botInfo) {
    __internals = new __Internals(botInfo);
  }

  public Bot(@NonNull final BotInfo botInfo, @NonNull URI serverUri) {
    __internals = new __Internals(botInfo, serverUri);
  }

  @Override
  public final void run() {
    __internals.connect();
  }

  @Override
  public final void go() {
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
  public final List<Event> getEvents() {
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
  public final void setFire(double firePower) {
    __internals.botIntent.setFirePower(firePower);
  }

  private final class __Internals {
    private static final String SERVER_URI_PROPERTY_KEY = "server.uri";
    private static final String SERVER_URI_ENV_VAR = "ROBOCODE2_SERVER_URI";

    private static final String NOT_CONNECTED_TO_SERVER_MSG =
        "Not connected to game server yes. Make sure onConnected() event handler has been called first";

    private static final String GAME_NOT_RUNNING_MSG =
        "Game is not running. Make sure onGameStarted() event handler has been called first";

    private static final String TICK_NOT_AVAILABLE_MSG =
        "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

    private final Gson gson = new GsonBuilder().create();

    private final BotInfo botInfo;

    private final BotIntent botIntent = new BotIntent();

    // Server connection:
    private WebSocketClient webSocket;
    private ServerHandshake serverHandshake;

    // Current game states:
    private Integer myId;
    private GameSetup gameSetup;
    private TickEvent currentTurn;

    __Internals(BotInfo botInfo) {
      this.botInfo = botInfo;
      init(getServerUriSetting());
    }

    __Internals(BotInfo botInfo, URI serverUri) {
      this.botInfo = botInfo;
      init(serverUri);
    }

    private void init(URI serverUri) {
      webSocket = new WSClient(serverUri);
    }

    private void connect() {
      if (!webSocket.isOpen()) {
        webSocket.connect();
      }
    }

    private void sendBotIntent() {
      webSocket.send(gson.toJson(botIntent));
    }

    private void clearCurrentGameState() {
      // Clear setting that are only available during a running game
      currentTurn = null;
      gameSetup = null;
      myId = null;
    }

    private URI getServerUriSetting() {
      var uri = System.getProperty(SERVER_URI_PROPERTY_KEY);
      if (uri == null) {
        uri = System.getenv(SERVER_URI_ENV_VAR);
      }
      if (uri == null) {
        throw new BotException(
            String.format(
                "Property %s or system environment variable %s is not defined",
                SERVER_URI_PROPERTY_KEY, SERVER_URI_ENV_VAR));
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

    private final class WSClient extends WebSocketClient {

      private WSClient(final URI uri) {
        super(uri);
      }

      @Override
      public final void onOpen(final org.java_websocket.handshake.ServerHandshake handshake) {
        val event =
            ConnectedEvent.builder()
                .httpStatus(handshake.getHttpStatus())
                .httpStatusMessage(handshake.getHttpStatusMessage())
                .build();
        Bot.this.onConnected(event);
      }

      @Override
      public final void onClose(final int code, final String reason, final boolean remote) {
        val event = DisconnectedEvent.builder().remote(remote).build();
        Bot.this.onDisconnected(event);

        Bot.this.__internals.clearCurrentGameState();
      }

      @Override
      public final void onError(final Exception ex) {
        val event = ConnectionErrorEvent.builder().exception(ex).build();
        Bot.this.onConnectionError(event);
      }

      @Override
      public final void onMessage(final String message) {
        JsonObject jsonMsg = gson.fromJson(message, JsonObject.class);

        JsonElement jsonType = jsonMsg.get("type");
        if (jsonType != null) {
          val type = jsonType.getAsString();

          switch (Message.Type.fromValue(type)) {
            case SERVER_HANDSHAKE:
              handleServerHandshake(jsonMsg);
              break;
            case TICK_EVENT_FOR_BOT:
              handleTickEvent(jsonMsg);
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
        val botHandshake = BotHandshakeFactory.create(Bot.__Internals.this.botInfo);
        val msg = gson.toJson(botHandshake);
        send(msg);
      }

      private void handleTickEvent(JsonObject jsonMsg) {
        val tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
        currentTurn = EventMapper.map(tickEventForBot);
        Bot.this.onTick(currentTurn);
        dispatchEvents(currentTurn);
      }

      private void handleGameStartedEvent(JsonObject jsonMsg) {
        val gameStartedEventForBot = gson.fromJson(jsonMsg, GameStartedEventForBot.class);
        val gameSetup = gameStartedEventForBot.getGameSetup();

        Bot.__Internals.this.myId = gameStartedEventForBot.getMyId();
        Bot.__Internals.this.gameSetup = GameSetupMapper.map(gameSetup);

        // Send ready signal
        BotReady ready = new BotReady();
        ready.setType(BotReady.Type.BOT_READY);

        val msg = gson.toJson(ready);
        send(msg);

        val gameStartedEvent =
            GameStartedEvent.builder()
                .myId(gameStartedEventForBot.getMyId())
                .gameSetup(Bot.__Internals.this.gameSetup)
                .build();
        Bot.this.onGameStarted(gameStartedEvent);
      }
    }

    private void handleGameEndedEvent(JsonObject jsonMsg) {
      // Clear current game state
      Bot.this.__internals.clearCurrentGameState();

      // Send the game ended event
      val gameEndedEventForBot = gson.fromJson(jsonMsg, GameEndedEventForBot.class);
      val gameEndedEvent =
          GameEndedEvent.builder()
              .numberOfRounds(gameEndedEventForBot.getNumberOfRounds())
              .results(ResultsMapper.map(gameEndedEventForBot.getResults()))
              .build();
      Bot.this.onGameEnded(gameEndedEvent);
    }

    private void dispatchEvents(TickEvent tickEvent) {
      tickEvent
          .getEvents()
          .forEach(
              event -> {
                if (event instanceof BotDeathEvent) {
                  Bot.this.onBotDeath((BotDeathEvent) event);
                } else if (event instanceof BotHitBotEvent) {
                  Bot.this.onHitByBot((BotHitBotEvent) event);
                } else if (event instanceof BotHitWallEvent) {
                  Bot.this.onHitWall((BotHitWallEvent) event);
                } else if (event instanceof BulletFiredEvent) {
                  Bot.this.onBulletFired((BulletFiredEvent) event);
                } else if (event instanceof BulletHitBotEvent) {
                  Bot.this.onHitByBullet((BulletHitBotEvent) event);
                } else if (event instanceof BulletHitBulletEvent) {
                  Bot.this.onBulletHitBullet((BulletHitBulletEvent) event);
                } else if (event instanceof BulletHitWallEvent) {
                  Bot.this.onBulletHitWall((BulletHitWallEvent) event);
                } else if (event instanceof ScannedBotEvent) {
                  Bot.this.onScannedBot((ScannedBotEvent) event);
                } else if (event instanceof SkippedTurnEvent) {
                  Bot.this.onSkippedTurn((SkippedTurnEvent) event);
                } else if (event instanceof WonRoundEvent) {
                  Bot.this.onWonRound((WonRoundEvent) event);
                }
              });
    }
  }
}
