package test_utils;

import com.google.gson.Gson;
import dev.robocode.tankroyale.schema.BotHandshake;
import dev.robocode.tankroyale.schema.Message;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;

public final class MockedServer {

    public static final int PORT = 7913;

    private final WebSocketServerImpl server = new WebSocketServerImpl();

    private boolean isConnected;
    private BotHandshake botHandshake;

    private final Gson gson = new Gson();

    public MockedServer() {
        server.start();
    }

    public void close() {
        try {
            server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public BotHandshake getBotHandshake() {
        return botHandshake;
    }

    private class WebSocketServerImpl extends WebSocketServer {

        public WebSocketServerImpl() {
            super(new InetSocketAddress(PORT));
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            isConnected = true;

            var serverHandshake = new dev.robocode.tankroyale.schema.ServerHandshake();
            serverHandshake.set$type(Message.$type.SERVER_HANDSHAKE);
            serverHandshake.setName(MockedServer.class.getSimpleName());
            serverHandshake.setVersion("1.0.0");
            serverHandshake.setVariant("Tank Royale");
            serverHandshake.setGameTypes(Set.of("melee", "classic", "1v1"));

            conn.send(gson.toJson(serverHandshake));
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String text) {
            var message = gson.fromJson(text, dev.robocode.tankroyale.schema.Message.class);

            if (message.get$type().equals(dev.robocode.tankroyale.schema.Message.$type.BOT_HANDSHAKE)) {
                botHandshake = gson.fromJson(text, dev.robocode.tankroyale.schema.BotHandshake.class);
            }
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
        }
    }
}