package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.graphics.IGraphics;
import dev.robocode.tankroyale.botapi.internal.json.JsonConverter;
import dev.robocode.tankroyale.botapi.util.ColorUtil;
import dev.robocode.tankroyale.schema.BotIntent;
import dev.robocode.tankroyale.schema.Message;
import dev.robocode.tankroyale.schema.ServerHandshake;
import dev.robocode.tankroyale.schema.TeamMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static dev.robocode.tankroyale.botapi.IBaseBot.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN;
import static dev.robocode.tankroyale.botapi.IBaseBot.TEAM_MESSAGE_MAX_SIZE;
import static dev.robocode.tankroyale.botapi.util.MathUtil.clamp;
import static java.lang.Math.*;
import static java.net.http.WebSocket.Builder;

public final class BaseBotInternals {
    private static final String DEFAULT_SERVER_URL = "ws://localhost:7654";

    private static final String SERVER_URL_PROPERTY_KEY = "server.url";
    private static final String SERVER_SECRET_PROPERTY_KEY = "server.secret";

    private static final String NOT_CONNECTED_TO_SERVER_MSG =
            "Not connected to a game server. Make sure onConnected() event handler has been called first";

    private static final String GAME_NOT_RUNNING_MSG =
            "Game is not running. Make sure onGameStarted() event handler has been called first";

    private static final String TICK_NOT_AVAILABLE_MSG =
            "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

    private final URI serverUrl;
    private final String serverSecret;
    private WebSocket socket;
    private ServerHandshake serverHandshake;
    private final CountDownLatch closedLatch = new CountDownLatch(1);

    private final IBaseBot baseBot;
    private final BotInfo botInfo;
    private final BotIntent botIntent = newBotIntent();

    private Integer myId;
    private Set<Integer> teammateIds;
    private dev.robocode.tankroyale.botapi.GameSetup gameSetup;

    private InitialPosition initialPosition;

    private TickEvent tickEvent;
    private Long tickStartNanoTime;

    private final EventQueue eventQueue;

    private final BotEventHandlers botEventHandlers;
    private final InternalEventHandlers internalEventHandlers = new InternalEventHandlers();
    private final Set<Condition> conditions = new CopyOnWriteArraySet<>();

    private final Object nextTurnMonitor = new Object();

    private Thread thread;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private boolean isStopped;

    private IStopResumeListener stopResumeListener;

    private double maxSpeed = MAX_SPEED;
    private double maxTurnRate = MAX_TURN_RATE;
    private double maxGunTurnRate = MAX_GUN_TURN_RATE;
    private double maxRadarTurnRate = MAX_RADAR_TURN_RATE;

    private Double savedTargetSpeed;
    private Double savedTurnRate;
    private Double savedGunTurnRate;
    private Double savedRadarTurnRate;

    private final double absDeceleration = abs(DECELERATION);

    private int eventHandlingDisabledTurn;

    private RecordingPrintStream recordedStdOut;
    private RecordingPrintStream recordedStdErr;

    private int lastExecuteTurnNumber;

    private final GraphicsState graphicsState = new GraphicsState();

    public BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, URI serverUrl, String serverSecret) {
        this.baseBot = baseBot;

        if (botInfo == null) {
            // use environment variables for configuration
            botInfo = EnvVars.getBotInfo();
        }

        this.botInfo = botInfo;

        this.botEventHandlers = new BotEventHandlers(baseBot);
        this.eventQueue = new EventQueue(this, botEventHandlers);

        this.serverUrl = serverUrl == null ? getServerUrlFromSetting() : serverUrl;
        this.serverSecret = serverSecret == null ? getServerSecretFromSetting() : serverSecret;

        init();
    }

    private void init() {
        redirectStdOutAndStdErr();
        subscribeToEvents();
    }

    @SuppressWarnings("java:S106") // Standard outputs should not be used directly to log anything
    private void redirectStdOutAndStdErr() {
        recordedStdOut = new RecordingPrintStream(System.out);
        recordedStdErr = new RecordingPrintStream(System.err);

        System.setOut(recordedStdOut);
        System.setErr(recordedStdErr);
    }

    private void subscribeToEvents() {
        internalEventHandlers.onRoundStarted.subscribe(this::onRoundStarted, 100);
        internalEventHandlers.onNextTurn.subscribe(this::onNextTurn, 100);
        internalEventHandlers.onBulletFired.subscribe(this::onBulletFired, 100);
    }

    public void setRunning(boolean isRunning) {
        this.isRunning.set(isRunning);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    void startThread(IBot bot) {
        thread = new Thread(createRunnable(bot));
        thread.start();
    }

    private Runnable createRunnable(IBot bot) {
        return () -> {
            setRunning(true);
            try {
                enableEventHandling(true);

                try {
                    bot.run();
                } catch (ThreadInterruptedException e) {
                    return;
                }

                // Skip every turn after the run method has exited
                while (isRunning()) {
                    try {
                        bot.go();
                    } catch (ThreadInterruptedException e) {
                        return;
                    }
                }
            } finally {
                enableEventHandling(false); // prevent event queue max limit to be reached
            }
        };
    }

    void stopThread() {
        if (!isRunning())
            return;

        setRunning(false);

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public void enableEventHandling(boolean enable) {
        eventHandlingDisabledTurn = enable ? 0 : getCurrentTickOrThrow().getTurnNumber();
    }

    public boolean getEventHandlingDisabledTurn() {
        // Important! Allow an additional turn so events like RoundStarted can be handled
        return eventHandlingDisabledTurn != 0 && eventHandlingDisabledTurn < (getCurrentTickOrThrow().getTurnNumber() - 1);
    }

    void setStopResumeHandler(IStopResumeListener listener) {
        stopResumeListener = listener;
    }

    private static BotIntent newBotIntent() {
        var botIntent = new BotIntent();
        botIntent.setType(Message.Type.BOT_INTENT); // must be set!
        return botIntent;
    }

    private void resetMovement() {
        botIntent.setTurnRate(null);
        botIntent.setGunTurnRate(null);
        botIntent.setRadarTurnRate(null);
        botIntent.setTargetSpeed(null);
        botIntent.setFirepower(null);
    }

    InternalEventHandlers getInstantEventHandlers() {
        return internalEventHandlers;
    }

    public List<BotEvent> getEvents() {
        final var turnNumber = getCurrentTickOrThrow().getTurnNumber();
        return eventQueue.getEvents(turnNumber);
    }

    public void clearEvents() {
        eventQueue.clearEvents();
    }

    public void setInterruptible(boolean interruptible) {
        eventQueue.setCurrentEventInterruptible(interruptible);
    }

    Set<Condition> getConditions() {
        return conditions;
    }

    private void onRoundStarted(RoundStartedEvent e) {
        resetMovement();
        eventQueue.clear();
        isStopped = false;
        eventHandlingDisabledTurn = 0;
        lastExecuteTurnNumber = -1;
    }

    private void onNextTurn(TickEvent e) {
        synchronized (nextTurnMonitor) {
            // Unblock methods waiting for the next turn
            nextTurnMonitor.notifyAll();
        }
    }

    private void onBulletFired(BulletFiredEvent e) {
        botIntent.setFirepower(0d); // Reset firepower so the bot stops firing continuously
    }

    public void start() {
        connect();
        try {
            closedLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void connect() {
        sanitizeUrl(serverUrl);
        try {
            HttpClient httpClient = HttpClient.newBuilder().build();
            Builder webSocketBuilder = httpClient.newWebSocketBuilder();
            var webSocketHandler = new WebSocketHandler(
                    this,
                    serverUrl,
                    serverSecret,
                    baseBot,
                    botInfo,
                    botEventHandlers,
                    internalEventHandlers,
                    closedLatch
            );
            socket = webSocketBuilder.buildAsync(serverUrl, webSocketHandler).join();
        } catch (Exception ex) {
            throw new BotException("Could not create web socket for URL: " + serverUrl);
        }
    }

    private static void sanitizeUrl(URI uri) {
        var scheme = uri.getScheme();
        if (!List.of("ws", "wss").contains(scheme)) {
            throw new BotException("Wrong scheme used with server URL: " + uri);
        }
    }

    public void execute() {
        // If we are running at this point, make sure this method and the thread running it is stopped by force
        if (!isRunning())
            return;

        final var turnNumber = getCurrentTickOrThrow().getTurnNumber();
        if (turnNumber != lastExecuteTurnNumber) {
            lastExecuteTurnNumber = turnNumber;

            sendIntent();
        }
        waitForNextTurn(turnNumber);
    }

    private void sendIntent() {
        synchronized (this) {
            renderGraphicsToBotIntent();
            transferStdOutToBotIntent();
            socket.sendText(JsonConverter.toJson(botIntent), true);
            botIntent.getTeamMessages().clear();
        }
    }

    private void transferStdOutToBotIntent() {
        if (recordedStdOut != null) {
            String output = recordedStdOut.readNext();
            botIntent.setStdOut(output);
        }
        if (recordedStdErr != null) {
            String error = recordedStdErr.readNext();
            botIntent.setStdErr(error);
        }
    }

    private void renderGraphicsToBotIntent() {
        if (getCurrentTickOrThrow().getBotState().isDebuggingEnabled()) {
            botIntent.setDebugGraphics(graphicsState.getSvgOutput());
            graphicsState.clear();
        }
    }

    private void waitForNextTurn(int turnNumber) {
        // Most bot methods will call waitForNextTurn(), and hence this is a central place to stop a rogue thread that
        // cannot be killed any other way.
        stopRogueThread();

        synchronized (nextTurnMonitor) {
            while (isRunning() &&
                    turnNumber == getCurrentTickOrThrow().getTurnNumber() &&
                    Thread.currentThread() == thread &&
                    !Thread.currentThread().isInterrupted()
            ) {
                try {
                    nextTurnMonitor.wait(); // Wait for the next turn
                } catch (InterruptedException ex) {
                    throw new ThreadInterruptedException();
                }
            }
        }
    }

    private void stopRogueThread() {
        if (Thread.currentThread() != thread) {
            throw new ThreadInterruptedException();
        }
    }

    public void dispatchEvents(int turnNumber) {
        try {
            eventQueue.dispatchEvents(turnNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVariant() {
        return getServerHandshake().getVariant();
    }

    public String getVersion() {
        return getServerHandshake().getVersion();
    }

    public int getMyId() {
        if (myId == null) {
            throw new BotException(GAME_NOT_RUNNING_MSG);
        }
        return myId;
    }

    void setMyId(Integer myId) {
        this.myId = myId;
    }

    public GameSetup getGameSetup() {
        if (gameSetup == null) {
            throw new BotException(GAME_NOT_RUNNING_MSG);
        }
        return gameSetup;
    }

    void setGameSetup(GameSetup gameSetup) {
        this.gameSetup = gameSetup;
    }

    public InitialPosition getInitialPosition() {
        return initialPosition;
    }

    void setInitialPosition(InitialPosition initialPosition) {
        this.initialPosition = initialPosition;
    }

    public BotIntent getBotIntent() {
        return botIntent;
    }

    public TickEvent getCurrentTickOrThrow() {
        if (tickEvent == null) {
            throw new BotException(TICK_NOT_AVAILABLE_MSG);
        }
        return tickEvent;
    }

    public TickEvent getCurrentTickOrNull() {
        return tickEvent;
    }

    void setTickEvent(TickEvent tickEvent) {
        this.tickEvent = tickEvent;
    }

    private long getTicksStart() {
        if (tickStartNanoTime == null) {
            throw new BotException(TICK_NOT_AVAILABLE_MSG);
        }
        return tickStartNanoTime;
    }

    void setTickStartNanoTime(Long tickStartNanoTime) {
        this.tickStartNanoTime = tickStartNanoTime;
    }

    void addEventsFromTick(TickEvent event) {
        eventQueue.addEventsFromTick(event);
    }

    public int getTimeLeft() {
        long passesMicroSeconds = (System.nanoTime() - getTicksStart()) / 1000;
        return (int) (getGameSetup().getTurnTimeout() - passesMicroSeconds);
    }

    public boolean setFire(double firepower) {
        if (Double.isNaN(firepower)) {
            throw new IllegalArgumentException("'firepower' cannot be NaN");
        }
        if (baseBot.getEnergy() < firepower || baseBot.getGunHeat() > 0) {
            return false; // cannot fire yet
        }
        botIntent.setFirepower(firepower);
        return true;
    }

    public double getGunHeat() {
        return tickEvent == null ? 0 : tickEvent.getBotState().getGunHeat();
    }

    public double getSpeed() {
        return tickEvent == null ? 0 : tickEvent.getBotState().getSpeed();
    }

    public void setTurnRate(double turnRate) {
        if (Double.isNaN(turnRate)) {
            throw new IllegalArgumentException("'turnRate' cannot be NaN");
        }
        botIntent.setTurnRate(clamp(turnRate, -maxTurnRate, maxTurnRate));
    }

    public void setGunTurnRate(double gunTurnRate) {
        if (Double.isNaN(gunTurnRate)) {
            throw new IllegalArgumentException("'gunTurnRate' cannot be NaN");
        }
        botIntent.setGunTurnRate(clamp(gunTurnRate, -maxGunTurnRate, maxGunTurnRate));
    }

    public void setRadarTurnRate(double radarTurnRate) {
        if (Double.isNaN(radarTurnRate)) {
            throw new IllegalArgumentException("'radarTurnRate' cannot be NaN");
        }
        botIntent.setRadarTurnRate(clamp(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate));
    }

    public void setTargetSpeed(double targetSpeed) {
        if (Double.isNaN(targetSpeed)) {
            throw new IllegalArgumentException("'targetSpeed' cannot be NaN");
        }
        botIntent.setTargetSpeed(clamp(targetSpeed, -maxSpeed, maxSpeed));
    }

    public double getTurnRate() {
        if (botIntent.getTurnRate() != null) { // if the turn rate was modified during the turn
            return botIntent.getTurnRate();
        }
        return tickEvent == null ? 0 : tickEvent.getBotState().getTurnRate();
    }

    public double getGunTurnRate() {
        if (botIntent.getGunTurnRate() != null) { // if the turn rate was modified during the turn
            return botIntent.getGunTurnRate();
        }
        return tickEvent == null ? 0 : tickEvent.getBotState().getGunTurnRate();
    }

    public double getRadarTurnRate() {
        if (botIntent.getRadarTurnRate() != null) { // if the turn rate was modified during the turn
            return botIntent.getRadarTurnRate();
        }
        return tickEvent == null ? 0 : tickEvent.getBotState().getRadarTurnRate();
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = clamp(maxSpeed, 0, MAX_SPEED);
    }

    public double getMaxTurnRate() {
        return maxTurnRate;
    }

    public void setMaxTurnRate(double maxTurnRate) {
        this.maxTurnRate = clamp(maxTurnRate, 0, MAX_TURN_RATE);
    }

    public double getMaxGunTurnRate() {
        return maxGunTurnRate;
    }

    public void setMaxGunTurnRate(double maxGunTurnRate) {
        this.maxGunTurnRate = clamp(maxGunTurnRate, 0, MAX_GUN_TURN_RATE);
    }

    public double getMaxRadarTurnRate() {
        return maxRadarTurnRate;
    }

    public void setMaxRadarTurnRate(double maxRadarTurnRate) {
        this.maxRadarTurnRate = clamp(maxRadarTurnRate, 0, MAX_RADAR_TURN_RATE);
    }

    /**
     * Returns the new speed based on the current speed and distance to move.
     *
     * @param speed    is the current speed
     * @param distance is the move distance
     * @return The new speed
     */
    // Credits for this algorithm go to Patrick Cupka (aka Voidious),
    // Julian Kent (aka Skilgannon), and Positive for the original version:
    // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
    double getNewTargetSpeed(double speed, double distance) {
        if (distance < 0) {
            return -getNewTargetSpeed(-speed, -distance);
        }
        var targetSpeed = (distance == Double.POSITIVE_INFINITY) ?
                maxSpeed : min(maxSpeed, getMaxSpeed(distance));

        return (speed >= 0) ?
                clamp(targetSpeed, speed - absDeceleration, speed + ACCELERATION) :
                clamp(targetSpeed, speed - ACCELERATION, speed + getMaxDeceleration(-speed));
    }

    private double getMaxSpeed(double distance) {
        double decelerationTime =
                max(1, Math.ceil((Math.sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
        if (decelerationTime == Double.POSITIVE_INFINITY) {
            return MAX_SPEED;
        }
        double decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * absDeceleration;
        return ((decelerationTime - 1) * absDeceleration) + ((distance - decelerationDistance) / decelerationTime);
    }

    private double getMaxDeceleration(double speed) {
        double decelerationTime = speed / absDeceleration;
        double accelerationTime = 1 - decelerationTime;

        return min(1, decelerationTime) * absDeceleration + max(0, accelerationTime) * ACCELERATION;
    }

    double getDistanceTraveledUntilStop(double speed) {
        speed = abs(speed);
        double distance = 0;
        while (speed > 0) {
            distance += (speed = getNewTargetSpeed(speed, 0));
        }
        return distance;
    }

    public boolean addCondition(Condition condition) {
        return conditions.add(condition);
    }

    public boolean removeCondition(Condition condition) {
        return conditions.remove(condition);
    }

    public void setStop(boolean overwrite) {
        if (!isStopped || overwrite) {
            isStopped = true;

            savedTargetSpeed = botIntent.getTargetSpeed();
            savedTurnRate = botIntent.getTurnRate();
            savedGunTurnRate = botIntent.getGunTurnRate();
            savedRadarTurnRate = botIntent.getRadarTurnRate();

            botIntent.setTargetSpeed(0d);
            botIntent.setTurnRate(0d);
            botIntent.setGunTurnRate(0d);
            botIntent.setRadarTurnRate(0d);

            if (stopResumeListener != null) {
                stopResumeListener.onStop();
            }
        }
    }

    public void setResume() {
        if (isStopped) {
            botIntent.setTargetSpeed(savedTargetSpeed);
            botIntent.setTurnRate(savedTurnRate);
            botIntent.setGunTurnRate(savedGunTurnRate);
            botIntent.setRadarTurnRate(savedRadarTurnRate);

            if (stopResumeListener != null) {
                stopResumeListener.onResume();
            }
            isStopped = false; // must be last step
        }
    }

    public boolean isStopped() {
        return isStopped;
    }

    public Set<Integer> getTeammateIds() {
        if (teammateIds == null) {
            throw new BotException(GAME_NOT_RUNNING_MSG);
        }
        return teammateIds;
    }

    void setTeammateIds(Set<Integer> teammateIds) {
        this.teammateIds = teammateIds;
    }

    public boolean isTeammate(int botId) {
        return getTeammateIds().stream().anyMatch(teammateId -> botId == teammateId);
    }

    public void broadcastTeamMessage(Object message) {
        sendTeamMessage(null, message);
    }

    public void sendTeamMessage(Integer teammateId, Object message) {
        if (teammateId != null && !getTeammateIds().contains(teammateId)) {
            throw new IllegalArgumentException("No teammate was found with the specified 'teammateId': " + teammateId);
        }
        if (botIntent.getTeamMessages().size() == MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN) {
            throw new BotException(
                    "The maximum number team massages has already been reached: " + MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN);
        }
        if (message == null) {
            throw new IllegalArgumentException("The 'message' of a team message cannot be null");
        }

        var json = JsonConverter.toJson(message);
        if (json.getBytes().length > TEAM_MESSAGE_MAX_SIZE) {
            throw new IllegalArgumentException(
                    "The team message is larger than the limit of " + TEAM_MESSAGE_MAX_SIZE + " bytes (compact JSON format)");
        }
        var teamMessage = new TeamMessage();
        teamMessage.setMessageType(message.getClass().getName());
        teamMessage.setReceiverId(teammateId);
        teamMessage.setMessage(json);

        botIntent.getTeamMessages().add(teamMessage);
    }

    public Color getBodyColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getBodyColor();
    }

    public Color getTurretColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getTurretColor();
    }

    public Color getRadarColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getRadarColor();
    }

    public Color getBulletColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getBulletColor();
    }

    public Color getScanColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getScanColor();
    }

    public Color getTracksColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getTracksColor();
    }

    public Color getGunColor() {
        return tickEvent == null ? null : tickEvent.getBotState().getGunColor();
    }

    public void setBodyColor(Color color) {
        botIntent.setBodyColor(toIntentColor(color));
    }

    public void setTurretColor(Color color) {
        botIntent.setTurretColor(toIntentColor(color));
    }

    public void setRadarColor(Color color) {
        botIntent.setRadarColor(toIntentColor(color));
    }

    public void setBulletColor(Color color) {
        botIntent.setBulletColor(toIntentColor(color));
    }

    public void setScanColor(Color color) {
        botIntent.setScanColor(toIntentColor(color));
    }

    public void setTracksColor(Color color) {
        botIntent.setTracksColor(toIntentColor(color));
    }

    public void setGunColor(Color color) {
        botIntent.setGunColor(toIntentColor(color));
    }

    public IGraphics getGraphics() {
        return graphicsState.getGraphics();
    }

    private static String toIntentColor(Color color) {
        return color == null ? null : "#" + ColorUtil.toHex(color);
    }

    public Collection<BulletState> getBulletStates() {
        return tickEvent == null ? Collections.emptySet() : tickEvent.getBulletStates();
    }

    private ServerHandshake getServerHandshake() {
        if (serverHandshake == null) {
            throw new BotException(NOT_CONNECTED_TO_SERVER_MSG);
        }
        return serverHandshake;
    }

    void setServerHandshake(ServerHandshake serverHandshake) {
        this.serverHandshake = serverHandshake;
    }

    private URI getServerUrlFromSetting() {
        String url = System.getProperty(SERVER_URL_PROPERTY_KEY);
        if (url == null) {
            url = EnvVars.getServerUrl();
        }
        if (url == null) {
            url = DEFAULT_SERVER_URL;
        }
        try {
            return new URI(url);
        } catch (URISyntaxException ex) {
            throw new BotException("Incorrect syntax for server URL: " + url + ". Default is: " + DEFAULT_SERVER_URL);
        }
    }

    private String getServerSecretFromSetting() {
        String secret = System.getProperty(SERVER_SECRET_PROPERTY_KEY);
        if (secret == null) {
            secret = EnvVars.getServerSecret();
        }
        return secret;
    }
}