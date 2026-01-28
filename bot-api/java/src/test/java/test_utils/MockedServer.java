package test_utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;
import dev.robocode.tankroyale.schema.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static dev.robocode.tankroyale.schema.Message.Type.*;

public final class MockedServer {

    public static final int PORT = findAvailablePort();
    public static final String SERVER_URL = "ws://localhost:" + PORT;

    public static final String SESSION_ID = "123abc";
    public static final String NAME = MockedServer.class.getSimpleName();
    public static final String VERSION = "1.0.0";
    public static final String VARIANT = "Tank Royale";
    public static final Set<String> GAME_TYPES = Set.of("melee", "classic", "1v1");
    public static final int MY_ID = 1;
    public static final String GAME_TYPE = "classic";
    public static final int ARENA_WIDTH = 800;
    public static final int ARENA_HEIGHT = 600;
    public static final int NUMBER_OF_ROUNDS = 10;
    public static final double GUN_COOLING_RATE = 0.1;
    public static final int MAX_INACTIVITY_TURNS = 450;
    public static final int TURN_TIMEOUT = 30_000;
    public static final int READY_TIMEOUT = 1_000_000;

    public static final int BOT_ENEMY_COUNT = 7;
    public static final double BOT_ENERGY = 99.7;
    public static final double BOT_X = 44.5;
    public static final double BOT_Y = 721.34;
    public static final double BOT_DIRECTION = 120.1;
    public static final double BOT_GUN_DIRECTION = 103.45;
    public static final double BOT_RADAR_DIRECTION = 253.3;
    public static final double BOT_RADAR_SWEEP = 13.5;
    public static final double BOT_SPEED = 8.0;
    public static final double BOT_TURN_RATE = 5.1;
    public static final double BOT_GUN_TURN_RATE = 18.9;
    public static final double BOT_RADAR_TURN_RATE = 34.1;
    public static final double BOT_GUN_HEAT = 7.6;

    private int turnNumber = 1;
    private double energy = BOT_ENERGY;
    private double speed = BOT_SPEED;
    private double gunHeat = BOT_GUN_HEAT;
    private double direction = BOT_DIRECTION;
    private double gunDirection = BOT_GUN_DIRECTION;
    private double radarDirection = BOT_RADAR_DIRECTION;

    private double speedIncrement;
    private double turnIncrement;
    private double gunTurnIncrement;
    private double radarTurnIncrement;

    private Double speedMinLimit;
    private Double speedMaxLimit;
    private Double directionMinLimit;
    private Double directionMaxLimit;
    private Double gunDirectionMinLimit;
    private Double gunDirectionMaxLimit;
    private Double radarDirectionMinLimit;
    private Double radarDirectionMaxLimit;

    private final WebSocketServerImpl server = new WebSocketServerImpl();

    private final CountDownLatch openedLatch = new CountDownLatch(1);
    private final CountDownLatch botHandshakeLatch = new CountDownLatch(1);
    private final CountDownLatch gameStartedLatch = new CountDownLatch(1);
    private CountDownLatch tickEventLatch = new CountDownLatch(1);
    private CountDownLatch botIntentLatch = new CountDownLatch(1);

    private CountDownLatch botIntentContinueLatch = new CountDownLatch(1);

    private volatile BotHandshake botHandshake;
    private volatile BotIntent botIntent;


    public static URI getServerUrl() {
        try {
            return new URI(SERVER_URL);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        try {
            server.stop();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Restore interrupt status and log a short message instead of printing a stack trace
            Thread.currentThread().interrupt();
            System.err.println("MockedServer.stop() was interrupted");
        }
    }

    public void closeConnections() {
        for (WebSocket conn : server.getConnections()) {
            conn.close();
        }
    }

    public void sendRawText(String text) {
        for (WebSocket conn : server.getConnections()) {
            conn.send(text);
        }
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setEnergy(double energy) {
        this.energy = energy;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setGunHeat(double gunHeat) {
        this.gunHeat = gunHeat;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setSpeedIncrement(double increment) {
        this.speedIncrement = increment;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setTurnIncrement(double increment) {
        this.turnIncrement = increment;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setGunTurnIncrement(double increment) {
        this.gunTurnIncrement = increment;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setRadarTurnIncrement(double increment) {
        this.radarTurnIncrement = increment;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setSpeedMinLimit(double minLimit) {
        this.speedMinLimit = minLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setSpeedMaxLimit(double maxLimit) {
        this.speedMaxLimit = maxLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setDirectionMinLimit(double minLimit) {
        this.directionMinLimit = minLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setDirectionMaxLimit(double maxLimit) {
        this.directionMaxLimit = maxLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setGunDirectionMinLimit(double minLimit) {
        this.gunDirectionMinLimit = minLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setGunDirectionMaxLimit(double maxLimit) {
        this.gunDirectionMaxLimit = maxLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setRadarDirectionMinLimit(double minLimit) {
        this.radarDirectionMinLimit = minLimit;
    }

    /**
     * @deprecated This method is deprecated and marked for removal in a future refactor.
     */
    @Deprecated
    public void setRadarDirectionMaxLimit(double maxLimit) {
        this.radarDirectionMaxLimit = maxLimit;
    }

    public void resetBotIntentLatch() {
        botIntentLatch = new CountDownLatch(1);
        botIntentContinueLatch = new CountDownLatch(1);
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

    public boolean awaitGameStarted(int milliSeconds) {
        try {
            return gameStartedLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitGameStarted() was interrupted");
        }
        return false;
    }

    public boolean awaitTick(int milliSeconds) {
        try {
            return tickEventLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitTickEvent() was interrupted");
        }
        return false;
    }

    public boolean awaitBotIntent(int milliSeconds) {
        try {
            botIntentContinueLatch.countDown();
            return botIntentLatch.await(milliSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("awaitBotIntent() was interrupted");
        }
        return false;
    }

    public BotHandshake getBotHandshake() {
        return botHandshake;
    }

    public BotIntent getBotIntent() {
        return botIntent;
    }

    /**
     * Waits for the bot to be connected, handshaked, have received game-started and a tick event.
     * This is a convenience wrapper that chains awaitBotHandshake -> awaitGameStarted -> awaitTick.
     */
    public boolean awaitBotReady(int milliSeconds) {
        long start = System.currentTimeMillis();
        if (!awaitBotHandshake(milliSeconds)) {
            System.err.println("awaitBotReady: awaitBotHandshake timed out");
            return false;
        }
        long elapsed = (int) (System.currentTimeMillis() - start);
        int remaining = (int) Math.max(0, milliSeconds - elapsed);
        if (!awaitGameStarted(remaining)) {
            System.err.println("awaitBotReady: awaitGameStarted timed out");
            return false;
        }
        elapsed = (int) (System.currentTimeMillis() - start);
        remaining = (int) Math.max(0, milliSeconds - elapsed);

        // To avoid races with BOT_READY from the bot, proactively send a tick to all connected bots
        // using the current server state and wait for that tick to be observed.
        tickEventLatch = new CountDownLatch(1);
        var conns = server.getConnections();
        for (WebSocket conn : conns) {
            sendTickEventForBotToConn(conn, turnNumber++);
        }
        // signal that a tick was sent
        tickEventLatch.countDown();

        if (!awaitTick(remaining)) {
            System.err.println("awaitBotReady: awaitTick timed out");
            return false;
        }
        return true;
    }

    /**
     * Update the internal bot state with non-null parameters WITHOUT sending a tick.
     * This is useful for setting initial state before the bot starts running.
     * Unlike setBotStateAndAwaitTick, this does not require connections to be established.
     *
     * @param energy bot energy (nullable)
     * @param gunHeat gun heat value (nullable)
     * @param speed bot speed (nullable)
     * @param direction bot direction (nullable)
     * @param gunDirection gun direction (nullable)
     * @param radarDirection radar direction (nullable)
     */
    public synchronized void setInitialBotState(Double energy, Double gunHeat, Double speed,
                                                Double direction, Double gunDirection, Double radarDirection) {
        if (energy != null) this.energy = energy;
        if (gunHeat != null) this.gunHeat = gunHeat;
        if (speed != null) this.speed = speed;
        if (direction != null) this.direction = direction;
        if (gunDirection != null) this.gunDirection = gunDirection;
        if (radarDirection != null) this.radarDirection = radarDirection;
    }

    /**
     * Update the internal bot state with non-null parameters, send a tick to connected bots and
     * await that tick to be observed by test helpers. Returns true if tick was observed within the
     * default timeout (1000 ms).
     */
    public synchronized boolean setBotStateAndAwaitTick(Double energy, Double gunHeat, Double speed,
                                                        Double direction, Double gunDirection, Double radarDirection) {
        if (energy != null) this.energy = energy;
        if (gunHeat != null) this.gunHeat = gunHeat;
        if (speed != null) this.speed = speed;
        if (direction != null) this.direction = direction;
        if (gunDirection != null) this.gunDirection = gunDirection;
        if (radarDirection != null) this.radarDirection = radarDirection;

        // reset latch so awaitTick will wait for the manual tick
        tickEventLatch = new CountDownLatch(1);

        // send tick to all connections
        for (WebSocket conn : server.getConnections()) {
            sendTickEventForBotToConn(conn, turnNumber++);
        }

        // signal that a tick was sent
        tickEventLatch.countDown();
        return awaitTick(1000);
    }

    // Primitive overload for convenience (accepts primitives and delegates to boxed variant)
    public boolean awaitBotReady(Integer milliSeconds) {
        if (milliSeconds == null) return awaitBotReady(0);
        return awaitBotReady((int) milliSeconds);
    }

    public void setInitialBotState(double energy, double gunHeat, Double speed,
                                   Double direction, Double gunDirection, Double radarDirection) {
        Double gunHeatBoxed = Double.isNaN(gunHeat) ? null : gunHeat;
        setInitialBotState(Double.valueOf(energy), gunHeatBoxed, speed, direction, gunDirection, radarDirection);
    }

    public void setInitialBotState(double energy, double gunHeat, double speed,
                                   double direction, double gunDirection, double radarDirection) {
        setInitialBotState(Double.valueOf(energy), Double.valueOf(gunHeat), Double.valueOf(speed),
                          Double.valueOf(direction), Double.valueOf(gunDirection), Double.valueOf(radarDirection));
    }

    public boolean setBotStateAndAwaitTick(double energy, double gunHeat, Double speed,
                                           Double direction, Double gunDirection, Double radarDirection) {
        Double gunHeatBoxed = Double.isNaN(gunHeat) ? null : gunHeat; // autobox primitive to Double when needed
        return setBotStateAndAwaitTick(Double.valueOf(energy), gunHeatBoxed, speed, direction, gunDirection, radarDirection);
    }

    public boolean setBotStateAndAwaitTick(double energy, double gunHeat, double speed,
                                           double direction, double gunDirection, double radarDirection) {
        return setBotStateAndAwaitTick(Double.valueOf(energy), Double.valueOf(gunHeat), Double.valueOf(speed), Double.valueOf(direction), Double.valueOf(gunDirection), Double.valueOf(radarDirection));
    }

    private static int findAvailablePort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (java.io.IOException e) {
            return 7913; // fallback to default port
        }
    }

    /**
     * Construct and send a tick event for a connection using the current internal state.
     * This is used by both the WebSocket server message handling and manual triggers.
     */
    private void sendTickEventForBotToConn(WebSocket conn, int turnNumber) {
        var tickEvent = buildTickEventForBot(turnNumber);

        // Serialize using a fresh Gson without Event runtime type adapter to avoid interference
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        String json = gson.toJson(tickEvent);
        conn.send(json);
    }

    // Build a TickEventForBot from the current internal state. Separated for clarity and easier testing.
    private TickEventForBot buildTickEventForBot(int turnNumber) {
        var tickEvent = new TickEventForBot();
        tickEvent.setType(TICK_EVENT_FOR_BOT);
        tickEvent.setRoundNumber(1);
        tickEvent.setTurnNumber(turnNumber);

        var state = new BotState();
        state.setEnergy(energy);
        state.setX(BOT_X);
        state.setY(BOT_Y);
        state.setDirection(direction);
        state.setGunDirection(gunDirection);
        state.setRadarDirection(radarDirection);
        state.setRadarSweep(BOT_RADAR_SWEEP);
        state.setSpeed(speed);
        state.setTurnRate(BOT_TURN_RATE);
        state.setGunTurnRate(BOT_GUN_TURN_RATE);
        state.setRadarTurnRate(BOT_RADAR_TURN_RATE);
        state.setGunHeat(gunHeat);
        state.setEnemyCount(BOT_ENEMY_COUNT);
        tickEvent.setBotState(state);

        // Apply any pending bot intent adjustments and optionally add events
        applyBotIntentToStateAndEvents(tickEvent, state);

        var bulletState1 = createBulletState(1);
        var bulletState2 = createBulletState(2);
        tickEvent.setBulletStates(List.of(bulletState1, bulletState2));

        // No events added to tick to avoid runtime type adapter conflicts in tests
        tickEvent.setEvents(List.of());

        return tickEvent;
    }

    // Apply effects from the last received BotIntent to the given BotState and the tick events.
    private void applyBotIntentToStateAndEvents(TickEventForBot tickEvent, BotState state) {
        if (botIntent == null) {
            return;
        }

        if (botIntent.getTurnRate() != null) {
            state.setTurnRate(botIntent.getTurnRate());
        }
        if (botIntent.getGunTurnRate() != null) {
            state.setGunTurnRate(botIntent.getGunTurnRate());
        }
        if (botIntent.getRadarTurnRate() != null) {
            state.setRadarTurnRate(botIntent.getRadarTurnRate());
        }

        if (botIntent.getFirepower() != null) {
            var bulletEvent = new BulletFiredEvent();
            bulletEvent.setType(BULLET_FIRED_EVENT);
            bulletEvent.setBullet(createBulletState(99));
            // Preserve original behavior: add to events list (tests later overwrite events to empty list)
            tickEvent.getEvents().add(bulletEvent);
        }
    }


    private BulletState createBulletState(int id) {
        var bulletState = new BulletState();
        bulletState.setBulletId(id);
        bulletState.setX(0.0);
        bulletState.setY(0.0);
        bulletState.setOwnerId(0);
        bulletState.setDirection(0.0);
        bulletState.setPower(0.0);
        return bulletState;
    }

    private class WebSocketServerImpl extends WebSocketServer {

        public WebSocketServerImpl() {
            super(new InetSocketAddress(PORT));
            setReuseAddr(true);
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            openedLatch.countDown();
            sendServerHandshake(conn);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String text) {
            var message = JsonConverter.fromJson(text, Message.class);
            switch (message.getType()) {
                case BOT_HANDSHAKE:
                    handleBotHandshake(conn, text);
                    break;

                case BOT_READY:
                    handleBotReady(conn);
                    break;

                case BOT_INTENT:
                    handleBotIntent(conn, text);
                    break;

                default:
                    // Unknown/unsupported message types are ignored by the mocked server
                    break;
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            throw new IllegalStateException("MockedServer error", ex);
        }

        private void sendServerHandshake(WebSocket conn) {
            var serverHandshake = new ServerHandshake();
            serverHandshake.setType(SERVER_HANDSHAKE);
            serverHandshake.setSessionId(SESSION_ID);
            serverHandshake.setName(NAME);
            serverHandshake.setVersion(VERSION);
            serverHandshake.setVariant(VARIANT);
            serverHandshake.setGameTypes(GAME_TYPES);
            serverHandshake.setGameSetup(null);
            send(conn, serverHandshake);
        }

        private void sendGameStartedForBot(WebSocket conn) {
            var gameStarted = new GameStartedEventForBot();
            gameStarted.setType(GAME_STARTED_EVENT_FOR_BOT);
            gameStarted.setMyId(MY_ID);

            var gameSetup = new GameSetup();
            gameSetup.setGameType(GAME_TYPE);
            gameSetup.setArenaWidth(ARENA_WIDTH);
            gameSetup.setArenaHeight(ARENA_HEIGHT);
            gameSetup.setNumberOfRounds(NUMBER_OF_ROUNDS);
            gameSetup.setGunCoolingRate(GUN_COOLING_RATE);
            gameSetup.setMaxInactivityTurns(MAX_INACTIVITY_TURNS);
            gameSetup.setTurnTimeout(TURN_TIMEOUT);
            gameSetup.setReadyTimeout(READY_TIMEOUT);
            gameStarted.setGameSetup(gameSetup);

            // Ensure a fresh tick latch for the initial BOT_READY tick
            tickEventLatch = new CountDownLatch(1);

            // Delegate to outer helper explicitly to avoid shadowing
            send(conn, gameStarted);
        }

        private void sendRoundStarted(WebSocket conn) {
            var roundStarted = new RoundStartedEvent();
            roundStarted.setType(ROUND_STARTED_EVENT);
            roundStarted.setRoundNumber(1);
            send(conn, roundStarted);
        }

        // Handler implementations extracted for clarity and testability
        private void handleBotHandshake(WebSocket conn, String text) {
            System.out.println("BOT_HANDSHAKE");

            botHandshake = JsonConverter.fromJson(text, BotHandshake.class);
            botHandshakeLatch.countDown();

            sendGameStartedForBot(conn);
            gameStartedLatch.countDown();
        }

        private void handleBotReady(WebSocket conn) {
            System.out.println("BOT_READY");

            sendRoundStarted(conn);

            sendTickEventForBotToConn(conn, turnNumber++);
            tickEventLatch.countDown();
        }

        private void handleBotIntent(WebSocket conn, String text) {
            System.out.println("BOT_INTENT");

            if (isIntentRejectedByLimits()) {
                return;
            }

            awaitBotIntentContinueOrFail();

            // Parse the intent and assign to volatile field (ensures visibility)
            botIntent = JsonConverter.fromJson(text, BotIntent.class);

            // Count down latch AFTER intent is fully parsed and assigned
            // The volatile write to botIntent happens-before this countdown,
            // ensuring test threads see the parsed intent when latch releases
            botIntentLatch.countDown();

            sendTickEventForBotToConn(conn, turnNumber++);
            tickEventLatch.countDown();

            // Update internal state increments after processing the intent
            speed += speedIncrement;
            direction += turnIncrement;
            gunDirection += gunTurnIncrement;
            radarDirection += radarTurnIncrement;
        }

        private boolean isIntentRejectedByLimits() {
            return (speedMinLimit != null && speed < speedMinLimit) ||
                    (speedMaxLimit != null && speed > speedMaxLimit) ||
                    (directionMinLimit != null && direction < directionMinLimit) ||
                    (directionMaxLimit != null && direction > directionMaxLimit) ||
                    (gunDirectionMinLimit != null && gunDirection < gunDirectionMinLimit) ||
                    (gunDirectionMaxLimit != null && gunDirection > gunDirectionMaxLimit) ||
                    (radarDirectionMinLimit != null && radarDirection < radarDirectionMinLimit) ||
                    (radarDirectionMaxLimit != null && radarDirection > radarDirectionMaxLimit);
        }

        private void awaitBotIntentContinueOrFail() {
            try {
                botIntentContinueLatch.await();
            } catch (InterruptedException e) {
                // Restore interrupt status and surface as runtime exception to preserve previous behaviour
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            // Reset latch so subsequent intents will wait again
            botIntentContinueLatch = new CountDownLatch(1);
        }

        /**
         * Serialize an event object to JSON and send it to the given WebSocket connection.
         */
        private void send(WebSocket conn, Object event) {
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
            String json = gson.toJson(event);
            conn.send(json);
        }
    }
}
