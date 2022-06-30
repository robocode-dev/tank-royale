package test_utils;

import com.google.gson.Gson;
import dev.robocode.tankroyale.schema.BotHandshake;
import dev.robocode.tankroyale.schema.Message;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class MockedServer {

    public static final int PORT = 7913;

    private final WebSocketServerImpl server = new WebSocketServerImpl();

    private BotHandshake botHandshake;

    private final CountDownLatch connectedLatch = new CountDownLatch(1);
    private final CountDownLatch botHandshakeLatch = new CountDownLatch(1);

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

    public boolean awaitConnection(int milliSeconds) {
        try {
            return connectedLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitConnection() was interrupted");
        }
        return false;
    }

    public boolean awaitBotHandshake(int milliSeconds) {
        try {
            return botHandshakeLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitBotHandshake() was interrupted");
        }
        return false;
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
            connectedLatch.countDown();

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

                botHandshakeLatch.countDown();
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