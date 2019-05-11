package net.robocode2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.val;
import lombok.var;
import net.robocode2.events.ConnectedEvent;
import net.robocode2.events.ConnectionErrorEvent;
import net.robocode2.events.DisconnectedEvent;
import net.robocode2.schema.BotHandshake;
import net.robocode2.schema.BotInfo;
import net.robocode2.schema.ServerHandshake;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public abstract class Bot implements IBot {

  private final __Internals __internals;

  // Default constructor is not allowed, and thus must be hidden
  private Bot() {
    __internals = null;
  }

  public Bot(final BotInfo botInfo) {
    __internals = new __Internals(botInfo);
  }

  public Bot(final BotInfo botInfo, URI serverUri) {
    __internals = new __Internals(botInfo, serverUri);
  }

  private final class __Internals {
    private static final String SERVER_URI_PROPERTY_KEY = "server.uri";
    private static final String SERVER_URI_ENV_VAR = "ROBOCODE2_SERVER_URI";

    private final BotInfo botInfo;

    private WebSocketClient wsClient;

    private final Gson gson = new GsonBuilder().create();

    private String clientKey;

    public __Internals(BotInfo botInfo) {
      this.botInfo = botInfo;

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
        init(new URI(uri));
      } catch (URISyntaxException ex) {
        throw new BotException("Incorrect syntax for server uri: " + uri);
      }
    }

    public __Internals(BotInfo botInfo, URI serverUri) {
      this.botInfo = botInfo;
      init(serverUri);
    }

    private void init(URI serverUri) {
      wsClient = new WSClient(serverUri);
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
        JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);

        JsonElement jsonType = jsonMessage.get("type");
        if (jsonType != null) {
          String type = jsonType.getAsString();

          if (ServerHandshake.Type.SERVER_HANDSHAKE.toString().equalsIgnoreCase(type)) {
            // The client key is assigned to the bot from the server only
            clientKey = jsonMessage.get("clientKey").getAsString();

            // Send bot handshake
            BotHandshake handshake = new BotHandshake();
            handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
            handshake.setClientKey(clientKey);
            handshake.setName("Bot name");
            handshake.setVersion("0.1");
            handshake.setAuthor("Author name");
            handshake.setCountryCode("DK");
            handshake.setGameTypes(Arrays.asList("melee", "1v1"));
            handshake.setProgrammingLang("Java");

            String msg = gson.toJson(handshake);
            send(msg);
          }
        }
        // TODO
      }
    }
  }

  public final class BotException extends RuntimeException {
    BotException(final String message) {
      super(message);
    }
  }
}
