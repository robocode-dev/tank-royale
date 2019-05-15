package net.robocode2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import net.robocode2.events.BotDeathEvent;
import net.robocode2.events.*;
import net.robocode2.factory.BotHandshakeFactory;
import net.robocode2.mapper.EventMapper;
import net.robocode2.mapper.GameSetupMapper;
import net.robocode2.mapper.ResultsMapper;
import net.robocode2.schema.*;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

import static net.robocode2.schema.Message.Type.SERVER_HANDSHAKE;

public abstract class Bot implements IBot {

  private final __Internals __internals;

  // Default constructor is not allowed, and thus must be hidden
  private Bot() {
    __internals = new __Internals(null);
  }

  public Bot(@NonNull final BotInfo botInfo) {
    __internals = new __Internals(botInfo);
  }

  public Bot(@NonNull final BotInfo botInfo, @NonNull URI serverUri) {
    __internals = new __Internals(botInfo, serverUri);
  }

  public void run() {
    val wsClient = __internals.wsClient;
    if (!wsClient.isOpen()) {
      wsClient.connect();
    }
  }

  public int getMyId() {
    return __internals.myId;
  }

  public String getGameType() {
    return __internals.gameSetup.getGameType();
  }

  public int getArenaWidth() {
    return __internals.gameSetup.getArenaWidth();
  }

  public int getArenaHeight() {
    return __internals.gameSetup.getArenaHeight();
  }

  public int getNumberOfRounds() {
    return __internals.gameSetup.getNumberOfRounds();
  }

  public double getGunCoolingRate() {
    return __internals.gameSetup.getGunCoolingRate();
  }

  public int getInactivityTurns() {
    return __internals.gameSetup.getInactivityTurns();
  }

  public int getTurnTimeout() {
    return __internals.gameSetup.getTurnTimeout();
  }

  public int getReadyTimeout() {
    return __internals.gameSetup.getReadyTimeout();
  }

  private final class __Internals {
    private static final String SERVER_URI_PROPERTY_KEY = "server.uri";
    private static final String SERVER_URI_ENV_VAR = "ROBOCODE2_SERVER_URI";

    private final BotInfo botInfo;

    private WebSocketClient wsClient;

    private final Gson gson = new GsonBuilder().create();

    private String clientKey;

    private int myId;
    private GameSetup gameSetup;

    __Internals(BotInfo botInfo) {
      this.botInfo = botInfo;
      init(getServerUriSetting());
    }

    __Internals(BotInfo botInfo, URI serverUri) {
      this.botInfo = botInfo;
      init(serverUri);
    }

    private void init(URI serverUri) {
      wsClient = new WSClient(serverUri);
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
        val event = DisconnectedEvent.builder().code(code).reason(reason).remote(remote).build();
        Bot.this.onDisconnected(event);
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

          if (SERVER_HANDSHAKE == Message.Type.fromValue(type)) {
            handleServerHandshake(jsonMsg);
          } else
            switch (Event.Type.fromValue(type)) {
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
                throw new BotException("Unsupported websocket message type: " + type);
            }
        } else {
          throw new BotException("No type is defined for the websocket message");
        }
        // TODO
      }

      private void handleServerHandshake(JsonObject jsonMsg) {
        // The client key is assigned to the bot from the server only
        val clientKey = jsonMsg.get("clientKey").getAsString();
        Bot.__Internals.this.clientKey = clientKey;

        // Send bot handshake
        val botHandshake = BotHandshakeFactory.create(clientKey, Bot.__Internals.this.botInfo);
        val msg = gson.toJson(botHandshake);
        send(msg);
      }

      private void handleTickEvent(JsonObject jsonMsg) {
        val tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);
        val tickEvent = EventMapper.map(tickEventForBot);
        Bot.this.onTickEvent(tickEvent);

        // TODO: Loop through all game events and trigger the individual event handlers

        tickEvent
            .getEvents()
            .forEach(
                gameEvent -> {
                  if (gameEvent instanceof BotDeathEvent) {
                    Bot.this.onBotDeathEvent((BotDeathEvent) gameEvent);
                  }
                });
      }

      private void handleGameStartedEvent(JsonObject jsonMsg) {
        val gameStartedEventForBot = gson.fromJson(jsonMsg, GameStartedEventForBot.class);
        val gameSetup = gameStartedEventForBot.getGameSetup();

        Bot.__Internals.this.myId = gameStartedEventForBot.getMyId();
        Bot.__Internals.this.gameSetup = GameSetupMapper.map(gameSetup);

        // Send ready signal
        BotReady ready = new BotReady();
        ready.setType(BotReady.Type.BOT_READY);
        ready.setClientKey(__Internals.this.clientKey);

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
      val gameEndedEventForBot = gson.fromJson(jsonMsg, GameEndedEventForBot.class);
      val newBattleEndedEvent =
          GameEndedEvent.builder()
              .numberOfRounds(gameEndedEventForBot.getNumberOfRounds())
              .results(ResultsMapper.map(gameEndedEventForBot.getResults()))
              .build();
      Bot.this.onGameEnded(newBattleEndedEvent);
    }
  }
}
