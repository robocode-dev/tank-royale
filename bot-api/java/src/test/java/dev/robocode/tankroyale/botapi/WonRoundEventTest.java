package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.WonRoundEvent;
import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;
import dev.robocode.tankroyale.schema.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static dev.robocode.tankroyale.schema.Message.Type.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that WonRoundEvent is delivered to the bot when the server includes it
 * in the final tick's event list and then sends RoundEndedEventForBot.
 * (TR-API-TCK-005)
 */
@Tag("LEGACY")
@Tag("TCK")
@Tag("TR-API-TCK-005")
class WonRoundEventTest {

    /**
     * BaseBot test: no go() loop — only the handleRoundEnded→dispatchEvents() fix path can deliver the event.
     * Server sends RoundEnded 200ms after the tick, WITHOUT waiting for BotIntent.
     */
    @Test
    void baseBot_whenTickContainsWonRoundEvent_thenOnWonRoundIsCalled() throws Exception {
        var wonRoundLatch = new CountDownLatch(1);

        var server = new WinTestServer(false);
        server.start();
        Thread.sleep(100);

        try {
            var botInfo = BotInfo.builder()
                    .setName("TestBot")
                    .setVersion("1.0")
                    .addAuthor("Author")
                    .build();

            var bot = new BaseBot(botInfo, new URI(WinTestServer.URL)) {
                @Override
                public void onWonRound(WonRoundEvent event) {
                    System.out.println("[BOT/BaseBot] onWonRound called! turn=" + event.getTurnNumber());
                    wonRoundLatch.countDown();
                }
            };
            new Thread(bot::start).start();

            boolean received = wonRoundLatch.await(5, TimeUnit.SECONDS);
            assertThat(received)
                    .as("BaseBot: onWonRound() should be called within 5 seconds")
                    .isTrue();
        } finally {
            server.stop();
        }
    }

    /**
     * Bot test: simulates real game flow — server waits for BotIntent before sending RoundEnded.
     * The bot's go() loop (dispatchEvents) is the primary delivery path here.
     */
    @Test
    void bot_whenTickContainsWonRoundEvent_thenOnWonRoundIsCalled() throws Exception {
        var wonRoundLatch = new CountDownLatch(1);

        var server = new WinTestServer(true); // wait for BotIntent before sending RoundEnded
        server.start();
        Thread.sleep(100);

        try {
            var botInfo = BotInfo.builder()
                    .setName("TestBot")
                    .setVersion("1.0")
                    .addAuthor("Author")
                    .build();

            var bot = new Bot(botInfo, new URI(WinTestServer.URL)) {
                @Override
                public void run() {
                    // Minimal loop — just call go() so dispatchEvents fires each turn
                    while (isRunning()) {
                        go();
                    }
                }

                @Override
                public void onWonRound(WonRoundEvent event) {
                    System.out.println("[BOT/Bot] onWonRound called! turn=" + event.getTurnNumber());
                    wonRoundLatch.countDown();
                }
            };
            new Thread(bot::start).start();

            boolean received = wonRoundLatch.await(8, TimeUnit.SECONDS);
            assertThat(received)
                    .as("Bot: onWonRound() should be called within 8 seconds")
                    .isTrue();
        } finally {
            server.stop();
        }
    }

    // -------------------------------------------------------------------------
    // Minimal mocked server. Two modes:
    //   waitForIntent=false (BaseBot): sends RoundEnded 200ms after tick, no BotIntent needed.
    //   waitForIntent=true  (Bot):     waits for BotIntent after the winning tick, then sends RoundEnded.
    // Both modes drive the bot through:
    //   ServerHandshake → GameStarted → BotReady → RoundStarted
    //   → TickEventForBot(with WonRoundEvent) → [BotIntent] → RoundEndedEventForBot
    // -------------------------------------------------------------------------
    static class WinTestServer extends WebSocketServer {

        static final int PORT;
        static final String URL;

        static {
            int p;
            try (var s = new java.net.ServerSocket(0)) {
                p = s.getLocalPort();
            } catch (Exception e) {
                p = 7919;
            }
            PORT = p;
            URL = "ws://localhost:" + PORT;
        }

        private final boolean waitForIntent;
        // Tracks how many BotIntents we've received (used in Bot mode to sequence turns)
        private volatile int intentCount = 0;

        WinTestServer(boolean waitForIntent) {
            super(new InetSocketAddress(PORT));
            setReuseAddr(true);
            this.waitForIntent = waitForIntent;
        }

        @Override public void onStart() {}
        @Override public void onClose(WebSocket conn, int code, String r, boolean remote) {}
        @Override public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("[SERVER] onOpen — sending ServerHandshake");
            send(conn, buildServerHandshake());
        }

        @Override
        public void onMessage(WebSocket conn, String text) {
            var msg = JsonConverter.fromJson(text, Message.class);
            System.out.println("[SERVER] received: " + msg.getType());
            switch (msg.getType()) {
                case BOT_HANDSHAKE:
                    send(conn, buildGameStarted());
                    break;
                case BOT_READY:
                    send(conn, buildRoundStarted());
                    if (!waitForIntent) {
                        // BaseBot mode: send the winning tick immediately, then RoundEnded after delay
                        sendRaw(conn, buildTick(5, true));
                        new Thread(() -> {
                            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                            System.out.println("[SERVER] sending RoundEndedEventForBot (no-intent mode)");
                            send(conn, buildRoundEnded(5));
                        }).start();
                    } else {
                        // Bot mode: send turn 1 first so BotInternals starts the bot thread
                        sendRaw(conn, buildTick(1, false));
                    }
                    break;
                case BOT_INTENT:
                    if (!waitForIntent) break; // BaseBot mode — ignore intents
                    intentCount++;
                    System.out.println("[SERVER] BotIntent #" + intentCount);
                    if (intentCount == 1) {
                        // After turn-1 intent, send the winning tick
                        sendRaw(conn, buildTick(2, true));
                    } else if (intentCount == 2) {
                        // After winning-tick intent, send RoundEnded
                        System.out.println("[SERVER] sending RoundEndedEventForBot (after winning BotIntent)");
                        send(conn, buildRoundEnded(2));
                    }
                    break;
                default:
                    System.out.println("[SERVER] ignoring message type: " + msg.getType());
            }
        }

        // ------- builders -------

        private void send(WebSocket conn, Message m) {
            String json = JsonConverter.toJson(m);
            System.out.println("[SERVER] → " + m.getType() + " : " + json);
            conn.send(json);
        }

        /** Send a raw JSON string without going through the bot-api Gson (avoids RuntimeTypeAdapterFactory conflicts). */
        private void sendRaw(WebSocket conn, String json) {
            System.out.println("[SERVER] → raw : " + json);
            conn.send(json);
        }

        private ServerHandshake buildServerHandshake() {
            var m = new ServerHandshake();
            m.setType(SERVER_HANDSHAKE);
            m.setSessionId("test-session");
            m.setName("TestServer");
            m.setVersion("1.0");
            m.setVariant("Tank Royale");
            m.setGameTypes(Set.of("classic"));
            return m;
        }

        private GameStartedEventForBot buildGameStarted() {
            var m = new GameStartedEventForBot();
            m.setType(GAME_STARTED_EVENT_FOR_BOT);
            m.setMyId(1);
            m.setTeammateIds(List.of());
            var setup = new dev.robocode.tankroyale.schema.GameSetup();
            setup.setGameType("classic");
            setup.setArenaWidth(800);
            setup.setArenaHeight(600);
            setup.setNumberOfRounds(5);
            setup.setGunCoolingRate(0.1);
            setup.setMaxInactivityTurns(450);
            setup.setTurnTimeout(30_000);
            setup.setReadyTimeout(1_000_000);
            m.setGameSetup(setup);
            return m;
        }

        private RoundStartedEvent buildRoundStarted() {
            var m = new RoundStartedEvent();
            m.setType(ROUND_STARTED_EVENT);
            m.setRoundNumber(1);
            return m;
        }

        /**
         * Build a TickEventForBot JSON string manually to avoid RuntimeTypeAdapterFactory
         * conflicts (Event subclasses inherit 'type' from Message, which the adapter rejects
         * during serialization).
         */
        private String buildTick(int turn, boolean withWonRound) {
            String events = withWonRound
                    ? "[{\"type\":\"WonRoundEvent\",\"turnNumber\":" + turn + "}]"
                    : "[]";
            return "{"
                    + "\"type\":\"TickEventForBot\","
                    + "\"roundNumber\":1,"
                    + "\"turnNumber\":" + turn + ","
                    + "\"botState\":{"
                    + "\"isDroid\":false,\"energy\":100.0,\"x\":100.0,\"y\":100.0,"
                    + "\"direction\":0.0,\"gunDirection\":0.0,\"radarDirection\":0.0,"
                    + "\"radarSweep\":0.0,\"speed\":0.0,\"turnRate\":0.0,"
                    + "\"gunTurnRate\":0.0,\"radarTurnRate\":0.0,\"gunHeat\":0.0,"
                    + "\"enemyCount\":0,\"isDebuggingEnabled\":false"
                    + "},"
                    + "\"bulletStates\":[],"
                    + "\"events\":" + events
                    + "}";
        }

        private RoundEndedEventForBot buildRoundEnded(int turn) {
            var m = new RoundEndedEventForBot();
            m.setType(ROUND_ENDED_EVENT_FOR_BOT);
            m.setRoundNumber(1);
            m.setTurnNumber(turn);
            // Omit results — handleRoundEnded handles null results gracefully
            return m;
        }
    }
}
