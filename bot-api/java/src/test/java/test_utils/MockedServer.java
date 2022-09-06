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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class MockedServer {

    public static final int PORT = 7913;

    private WebSocketServerImpl server = new WebSocketServerImpl();

    private BotHandshake botHandshake;

    private CountDownLatch openedLatch = new CountDownLatch(1);
    private CountDownLatch botHandshakeLatch = new CountDownLatch(1);

    private Gson gson;

    public void start() {
        init();
        server.start();
    }

    public void stop() {
        try {
            server.stop();
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        openedLatch = new CountDownLatch(1);
        botHandshakeLatch = new CountDownLatch(1);

        gson = new Gson();

        server = new WebSocketServerImpl();
    }

    public boolean awaitConnection(int milliSeconds) {
        try {
            return openedLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
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
            openedLatch.countDown();

            var serverHandshake = new dev.robocode.tankroyale.schema.ServerHandshake();
            serverHandshake.setType(Message.Type.SERVER_HANDSHAKE);
            serverHandshake.setSessionId("123abc");
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
            var message = gson.fromJson(text, Message.class);

            if (message.getType().equals(Message.Type.BOT_HANDSHAKE)) {
                botHandshake = gson.fromJson(text, BotHandshake.class);

                botHandshakeLatch.countDown();
            }
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            throw new IllegalStateException("MockedServer error", ex);
        }
    }
}