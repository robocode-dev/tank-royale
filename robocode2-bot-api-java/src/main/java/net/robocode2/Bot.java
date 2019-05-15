package net.robocode2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.val;
import lombok.var;
import net.robocode2.events.*;
import net.robocode2.schema.*;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

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

          switch (Event.Type.fromValue(type)) {
            case GAME_STARTED_EVENT_FOR_BOT:
              handleGameStartedEvent(jsonMsg);
              break;
            case GAME_ENDED_EVENT_FOR_BOT:
              handleGameEndedEvent(jsonMsg);
              break;
            default:
              switch (Message.Type.fromValue(type)) {
                case SERVER_HANDSHAKE:
                  handleServerHandshake(jsonMsg);
                  break;
              }
          }
        }
        // TODO
      }

      private BotHandshake createBotHandshake() {
        BotHandshake handshake = new BotHandshake();
        handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
        handshake.setClientKey(clientKey);
        handshake.setName(botInfo.getName());
        handshake.setVersion(botInfo.getVersion());
        handshake.setAuthor(botInfo.getAuthor());
        handshake.setCountryCode(botInfo.getCountryCode());
        handshake.setGameTypes(botInfo.getGameTypes());
        handshake.setProgrammingLang(botInfo.getProgrammingLang());
        return handshake;
      }

      private void handleServerHandshake(JsonObject jsonMsg) {
        // The client key is assigned to the bot from the server only
        Bot.__Internals.this.clientKey = jsonMsg.get("clientKey").getAsString();

        // Send bot handshake
        val msg = gson.toJson(createBotHandshake());
        send(msg);
      }

      private void handleGameStartedEvent(JsonObject jsonMsg) {
        val gameStartedEventForBot = gson.fromJson(jsonMsg, GameStartedEventForBot.class);
        val gameSetup = gameStartedEventForBot.getGameSetup();

        Bot.__Internals.this.myId = gameStartedEventForBot.getMyId();

        Bot.__Internals.this.gameSetup =
            GameSetup.builder()
                .gameType(gameSetup.getGameType())
                .arenaWidth(gameSetup.getArenaWidth())
                .arenaHeight(gameSetup.getArenaHeight())
                .numberOfRounds(gameSetup.getNumberOfRounds())
                .gunCoolingRate(gameSetup.getGunCoolingRate())
                .inactivityTurns(gameSetup.getInactivityTurns())
                .turnTimeout(gameSetup.getTurnTimeout())
                .readyTimeout(gameSetup.getReadyTimeout())
                .build();

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

      val results = new ArrayList<BotResults>();
      gameEndedEventForBot
          .getResults()
          .forEach(
              r ->
                  results.add(
                      BotResults.builder()
                          .id(r.getId())
                          .rank(r.getRank())
                          .survival(r.getSurvival())
                          .lastSurvivorBonus(r.getLastSurvivorBonus())
                          .bulletDamage(r.getBulletDamage())
                          .bulletKillBonus(r.getBulletKillBonus())
                          .ramDamage(r.getRamDamage())
                          .ramKillBonus(r.getRamKillBonus())
                          .totalScore(r.getTotalScore())
                          .firstPlaces(r.getFirstPlaces())
                          .secondPlaces(r.getSecondPlaces())
                          .thirdPlaces(r.getThirdPlaces())
                          .build()));

      val newBattleEndedEvent =
          GameEndedEvent.builder()
              .numberOfRounds(gameEndedEventForBot.getNumberOfRounds())
              .results(results)
              .build();
      Bot.this.onGameEnded(newBattleEndedEvent);
    }
  }

  public static final class BotException extends RuntimeException {
    BotException(final String message) {
      super(message);
    }
  }
}
