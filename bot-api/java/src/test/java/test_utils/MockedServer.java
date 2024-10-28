package test_utils;

import com.google.gson.Gson;
import dev.robocode.tankroyale.schema.game.*;
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

import static dev.robocode.tankroyale.schema.game.Message.Type.*;

public final class MockedServer {

    public static final int PORT = 7913;
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
    private final CountDownLatch tickEventLatch = new CountDownLatch(1);
    private final CountDownLatch botIntentLatch = new CountDownLatch(1);

    private CountDownLatch botIntentContinueLatch = new CountDownLatch(1);

    private final Gson gson = new Gson();

    private BotHandshake botHandshake;
    private BotIntent botIntent;


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
            e.printStackTrace();
        }
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setGunHeat(double gunHeat) {
        this.gunHeat = gunHeat;
    }

    public void setSpeedIncrement(double increment) {
        this.speedIncrement = increment;
    }

    public void setTurnIncrement(double increment) {
        this.turnIncrement = increment;
    }

    public void setGunTurnIncrement(double increment) {
        this.gunTurnIncrement = increment;
    }

    public void setRadarTurnIncrement(double increment) {
        this.radarTurnIncrement = increment;
    }

    public void setSpeedMinLimit(double minLimit) {
        this.speedMinLimit = minLimit;
    }

    public void setSpeedMaxLimit(double maxLimit) {
        this.speedMaxLimit = maxLimit;
    }

    public void setDirectionMinLimit(double minLimit) {
        this.directionMinLimit = minLimit;
    }

    public void setDirectionMaxLimit(double maxLimit) {
        this.directionMaxLimit = maxLimit;
    }

    public void setGunDirectionMinLimit(double minLimit) {
        this.gunDirectionMinLimit = minLimit;
    }

    public void setGunDirectionMaxLimit(double maxLimit) {
        this.gunDirectionMaxLimit = maxLimit;
    }

    public void setRadarDirectionMinLimit(double minLimit) {
        this.radarDirectionMinLimit = minLimit;
    }

    public void setRadarDirectionMaxLimit(double maxLimit) {
        this.radarDirectionMaxLimit = maxLimit;
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
            sendServerHandshake(conn);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        }

        @Override
        public void onMessage(WebSocket conn, String text) {
            var message = gson.fromJson(text, Message.class);
            switch (message.getType()) {
                case BOT_HANDSHAKE:
                    System.out.println("BOT_HANDSHAKE");

                    botHandshake = gson.fromJson(text, BotHandshake.class);
                    botHandshakeLatch.countDown();

                    sendGameStartedForBot(conn);
                    gameStartedLatch.countDown();
                    break;

                case BOT_READY:
                    System.out.println("BOT_READY");

                    sendRoundStarted(conn);

                    sendTickEventForBot(conn, turnNumber++);
                    tickEventLatch.countDown();
                    break;

                case BOT_INTENT:
                    System.out.println("BOT_INTENT");

                    if (speedMinLimit != null && speed < speedMinLimit) return;
                    if (speedMaxLimit != null && speed > speedMaxLimit) return;

                    if (directionMinLimit != null && direction < directionMinLimit) return;
                    if (directionMaxLimit != null && direction > directionMaxLimit) return;

                    if (gunDirectionMinLimit != null && gunDirection < gunDirectionMinLimit) return;
                    if (gunDirectionMaxLimit != null && gunDirection > gunDirectionMaxLimit) return;

                    if (radarDirectionMinLimit != null && radarDirection < radarDirectionMinLimit) return;
                    if (radarDirectionMaxLimit != null && radarDirection > radarDirectionMaxLimit) return;

                    try {
                        botIntentContinueLatch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    botIntentContinueLatch = new CountDownLatch(1);

                    botIntent = gson.fromJson(text, BotIntent.class);
                    botIntentLatch.countDown();

                    sendTickEventForBot(conn, turnNumber++);
                    tickEventLatch.countDown();

                    // Update states
                    speed += speedIncrement;
                    direction += turnIncrement;
                    gunDirection += gunTurnIncrement;
                    radarDirection += radarTurnIncrement;
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

            send(conn, gameStarted);
        }

        private void sendRoundStarted(WebSocket conn) {
            var roundStarted = new RoundStartedEvent();
            roundStarted.setType(ROUND_STARTED_EVENT);
            roundStarted.setRoundNumber(1);
            send(conn, roundStarted);
        }

        private void sendTickEventForBot(WebSocket conn, int turnNumber) {
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

            if (botIntent != null) {
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
                    tickEvent.getEvents().add(new BulletFiredEvent()); // bullet event is completely empty
                }
            }

            var bulletState1 = createBulletState(1);
            var bulletState2 = createBulletState(2);
            tickEvent.setBulletStates(List.of(bulletState1, bulletState2));

            var event = new ScannedBotEvent();
            event.setType(SCANNED_BOT_EVENT);
            event.setDirection(45.0);
            event.setX(134.56);
            event.setY(256.7);
            event.setEnergy(56.9);
            event.setSpeed(9.6);
            event.setTurnNumber(1);
            event.setScannedBotId(2);
            event.setScannedByBotId(1);
            tickEvent.setEvents(List.of(event));

            send(conn, tickEvent);
        }

        private void send(WebSocket conn, Message message) {
            conn.send(gson.toJson(message));
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
    }
}