package dev.robocode.tankroyale.botapi.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.BulletState;
import dev.robocode.tankroyale.botapi.GameSetup;
import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.BotDeathEvent;
import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitBulletEvent;
import dev.robocode.tankroyale.botapi.events.BulletHitWallEvent;
import dev.robocode.tankroyale.botapi.events.HitByBulletEvent;
import dev.robocode.tankroyale.botapi.events.RoundStartedEvent;
import dev.robocode.tankroyale.botapi.events.ScannedBotEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;
import dev.robocode.tankroyale.botapi.events.WonRoundEvent;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.mapper.EventMapper;
import dev.robocode.tankroyale.botapi.mapper.GameSetupMapper;
import dev.robocode.tankroyale.schema.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static dev.robocode.tankroyale.botapi.IBaseBot.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN;
import static dev.robocode.tankroyale.botapi.IBaseBot.TEAM_MESSAGE_MAX_SIZE;
import static dev.robocode.tankroyale.botapi.events.DefaultEventPriority.*;
import static dev.robocode.tankroyale.botapi.mapper.ResultsMapper.map;
import static dev.robocode.tankroyale.botapi.util.MathUtil.clamp;
import static java.lang.Math.*;
import static java.net.http.WebSocket.Builder;
import static java.net.http.WebSocket.Listener;

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

    private TickEvent tickEvent;
    private Long tickStartNanoTime;

    private final EventQueue eventQueue;

    private final BotEventHandlers botEventHandlers;
    private final Set<Condition> conditions = new CopyOnWriteArraySet<>();

    private final Object nextTurnMonitor = new Object();

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

    private final double ABS_DECELERATION = abs(DECELERATION);

    private final Gson gson;

    private boolean eventHandlingDisabled;

    private RecordingPrintStream recordedStdOut;
    private RecordingPrintStream recordedStdErr;

    private final Map<Class<? extends BotEvent>, Integer> eventPriorities = new HashMap<>();

    {
        eventPriorities.put(WonRoundEvent.class, WON_ROUND);
        eventPriorities.put(SkippedTurnEvent.class, SKIPPED_TURN);
        eventPriorities.put(TickEvent.class, TICK);
        eventPriorities.put(CustomEvent.class, CUSTOM);
        eventPriorities.put(TeamMessageEvent.class, TEAM_MESSAGE);
        eventPriorities.put(BotDeathEvent.class, BOT_DEATH);
        eventPriorities.put(BulletHitWallEvent.class, BULLET_HIT_WALL);
        eventPriorities.put(BulletHitBulletEvent.class, BULLET_HIT_BULLET);
        eventPriorities.put(BulletHitBotEvent.class, BULLET_HIT_BOT);
        eventPriorities.put(BulletFiredEvent.class, BULLET_FIRED);
        eventPriorities.put(HitByBulletEvent.class, HIT_BY_BULLET);
        eventPriorities.put(HitWallEvent.class, HIT_WALL);
        eventPriorities.put(HitBotEvent.class, HIT_BOT);
        eventPriorities.put(ScannedBotEvent.class, SCANNED_BOT);
        eventPriorities.put(DeathEvent.class, DEATH);

        RuntimeTypeAdapterFactory<dev.robocode.tankroyale.schema.Event> typeFactory =
                RuntimeTypeAdapterFactory.of(dev.robocode.tankroyale.schema.Event.class, "type")
                        .registerSubtype(dev.robocode.tankroyale.schema.BotDeathEvent.class, "BotDeathEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.BotHitBotEvent.class, "BotHitBotEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.BotHitWallEvent.class, "BotHitWallEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.BulletFiredEvent.class, "BulletFiredEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.BulletHitBotEvent.class, "BulletHitBotEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.BulletHitBulletEvent.class, "BulletHitBulletEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.BulletHitWallEvent.class, "BulletHitWallEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.ScannedBotEvent.class, "ScannedBotEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.WonRoundEvent.class, "WonRoundEvent")
                        .registerSubtype(dev.robocode.tankroyale.schema.TeamMessageEvent.class, "TeamMessageEvent");

        gson = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                // to avoid IllegalArgumentException: -Infinity is not a valid double value as per JSON specification
                .serializeSpecialFloatingPointValues()
                .create();
    }

    public BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, URI serverUrl, String serverSecret) {
        this.baseBot = baseBot;
        this.botInfo = (botInfo == null) ? EnvVars.getBotInfo() : botInfo;

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

    private void redirectStdOutAndStdErr() {
        recordedStdOut = new RecordingPrintStream(System.out);
        recordedStdErr = new RecordingPrintStream(System.err);

        System.setOut(recordedStdOut);
        System.setErr(recordedStdErr);
    }

    private void subscribeToEvents() {
        botEventHandlers.onRoundStarted.subscribe(this::onRoundStarted, 100);
        botEventHandlers.onNextTurn.subscribe(this::onNextTurn, 100);
        botEventHandlers.onBulletFired.subscribe(this::onBulletFired, 100);
    }

    public void setRunning(boolean isRunning) {
        this.isRunning.set(isRunning);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void enableEventHandling(boolean enable) {
        eventHandlingDisabled = !enable;
    }

    public void setStopResumeHandler(IStopResumeListener listener) {
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

    BotEventHandlers getBotEventHandlers() {
        return botEventHandlers;
    }

    public List<BotEvent> getEvents() {
        return eventQueue.getEvents();
    }

    public void clearEvents() {
        eventQueue.clearEvents();
    }

    public void setInterruptible(boolean interruptible) {
        eventQueue.setInterruptible(interruptible);
    }

    void setScannedBotEventInterruptible() {
        eventQueue.setInterruptible(ScannedBotEvent.class, true);
    }

    Set<Condition> getConditions() {
        return conditions;
    }

    private void onRoundStarted(RoundStartedEvent e) {
        resetMovement();
        eventQueue.clear();
        isStopped = false;
        eventHandlingDisabled = false;
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
        setRunning(true);
        connect();
        try {
            closedLatch.await();
        } catch (InterruptedException ignore) {
        }
    }

    private void connect() {
        sanitizeUrl(serverUrl);
        try {
            HttpClient httpClient = HttpClient.newBuilder().build();
            Builder webSocketBuilder = httpClient.newWebSocketBuilder();
            socket = webSocketBuilder.buildAsync(serverUrl, new WebSocketListener()).join();
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

        final var turnNumber = getCurrentTick().getTurnNumber();

        dispatchEvents(turnNumber);
        sendIntent();
        waitForNextTurn(turnNumber);
    }

    private void sendIntent() {
        transferStdOutToBotIntent();
        socket.sendText(gson.toJson(botIntent), true);
        botIntent.getTeamMessages().clear();
    }

    private void transferStdOutToBotIntent() {
        if (recordedStdOut != null) {
            botIntent.setStdOut(recordedStdOut.readNext());
        }
        if (recordedStdErr != null) {
            botIntent.setStdErr(recordedStdErr.readNext());
        }
    }

    private void waitForNextTurn(int turnNumber) {
        synchronized (nextTurnMonitor) {
            while (isRunning() && turnNumber == getCurrentTick().getTurnNumber()) {
                try {
                    nextTurnMonitor.wait(); // Wait for next turn
                } catch (InterruptedException ex) {
                    return; // stop waiting, thread has been interrupted (stopped)
                }
            }
        }
    }

    private void dispatchEvents(int turnNumber) {
        try {
            eventQueue.dispatchEvents(turnNumber);
        } catch (InterruptEventHandlerException e) {
            // Do nothing (event handler was stopped by this exception)
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

    public GameSetup getGameSetup() {
        if (gameSetup == null) {
            throw new BotException(GAME_NOT_RUNNING_MSG);
        }
        return gameSetup;
    }

    public BotIntent getBotIntent() {
        return botIntent;
    }

    public TickEvent getCurrentTick() {
        if (tickEvent == null) {
            throw new BotException(TICK_NOT_AVAILABLE_MSG);
        }
        return tickEvent;
    }

    private long getTicksStart() {
        if (tickStartNanoTime == null) {
            throw new BotException(TICK_NOT_AVAILABLE_MSG);
        }
        return tickStartNanoTime;
    }

    public int getTimeLeft() {
        long passesMicroSeconds = (System.nanoTime() - getTicksStart()) / 1000;
        return (int) (getGameSetup().getTurnTimeout() - passesMicroSeconds);
    }

    public boolean setFire(double firepower) {
        if (Double.isNaN(firepower)) {
            throw new IllegalArgumentException("firepower cannot be NaN");
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
            throw new IllegalArgumentException("turnRate cannot be NaN");
        }
        botIntent.setTurnRate(clamp(turnRate, -maxTurnRate, maxTurnRate));
    }

    public void setGunTurnRate(double gunTurnRate) {
        if (Double.isNaN(gunTurnRate)) {
            throw new IllegalArgumentException("gunTurnRate cannot be NaN");
        }
        botIntent.setGunTurnRate(clamp(gunTurnRate, -maxGunTurnRate, maxGunTurnRate));
    }

    public void setRadarTurnRate(double radarTurnRate) {
        if (Double.isNaN(radarTurnRate)) {
            throw new IllegalArgumentException("radarTurnRate cannot be NaN");
        }
        botIntent.setRadarTurnRate(clamp(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate));
    }

    public void setTargetSpeed(double targetSpeed) {
        if (Double.isNaN(targetSpeed)) {
            throw new IllegalArgumentException("targetSpeed cannot be NaN");
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
     * @param distance is the distance to move
     * @return The new speed
     */
    // Credits for this algorithm goes to Patrick Cupka (aka Voidious),
    // Julian Kent (aka Skilgannon), and Positive for the original version:
    // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
    double getNewTargetSpeed(double speed, double distance) {
        if (distance < 0) {
            return -getNewTargetSpeed(-speed, -distance);
        }
        var targetSpeed = (distance == Double.POSITIVE_INFINITY) ?
                maxSpeed : min(maxSpeed, getMaxSpeed(distance));

        return (speed >= 0) ?
                clamp(targetSpeed, speed - ABS_DECELERATION, speed + ACCELERATION) :
                clamp(targetSpeed, speed - ACCELERATION, speed + getMaxDeceleration(-speed));
    }

    private double getMaxSpeed(double distance) {
        double decelerationTime =
                max(1, Math.ceil((Math.sqrt((4 * 2 / ABS_DECELERATION) * distance + 1) - 1) / 2));
        if (decelerationTime == Double.POSITIVE_INFINITY) {
            return MAX_SPEED;
        }
        double decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * ABS_DECELERATION;
        return ((decelerationTime - 1) * ABS_DECELERATION) + ((distance - decelerationDistance) / decelerationTime);
    }

    private double getMaxDeceleration(double speed) {
        double decelerationTime = speed / ABS_DECELERATION;
        double accelerationTime = 1 - decelerationTime;

        return min(1, decelerationTime) * ABS_DECELERATION + max(0, accelerationTime) * ACCELERATION;
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

        var json = gson.toJson(message);
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

    public int getPriority(Class<BotEvent> eventClass) {
        if (!eventPriorities.containsKey(eventClass)) {
            throw new IllegalStateException("Could not get event priority for the class: " + eventClass.getSimpleName());
        }
        return eventPriorities.get(eventClass);
    }

    public void setPriority(Class<BotEvent> eventClass, int priority) {
        eventPriorities.put(eventClass, priority);
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

    private static String toIntentColor(Color color) {
        return color == null ? null : "#" + color.toHex();
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

    private final class WebSocketListener implements Listener {

        StringBuffer payload = new StringBuffer();

        @Override
        public void onOpen(WebSocket websocket) {
            BaseBotInternals.this.socket = websocket; // To prevent null pointer exception

            botEventHandlers.onConnected.publish(new ConnectedEvent(serverUrl));
            Listener.super.onOpen(websocket);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket websocket, int statusCode, String reason) {
            botEventHandlers.onDisconnected.publish(new DisconnectedEvent(serverUrl, true, statusCode, reason));
            closedLatch.countDown();
            return null;
        }

        @Override
        public void onError(WebSocket websocket, Throwable error) {
            botEventHandlers.onConnectionError.publish(new ConnectionErrorEvent(serverUrl, error));
            closedLatch.countDown();
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            payload.append(data);
            if (last) {
                JsonObject jsonMsg = gson.fromJson(payload.toString(), JsonObject.class);
                payload.delete(0, payload.length()); // clear payload buffer

                JsonElement jsonType = jsonMsg.get("type");
                if (jsonType != null) {
                    String type = jsonType.getAsString();

                    switch (dev.robocode.tankroyale.schema.Message.Type.fromValue(type)) {
                        case TICK_EVENT_FOR_BOT:
                            handleTick(jsonMsg);
                            break;
                        case ROUND_STARTED_EVENT:
                            handleRoundStarted(jsonMsg);
                            break;
                        case ROUND_ENDED_EVENT_FOR_BOT:
                            handleRoundEnded(jsonMsg);
                            break;
                        case GAME_STARTED_EVENT_FOR_BOT:
                            handleGameStarted(jsonMsg);
                            break;
                        case GAME_ENDED_EVENT_FOR_BOT:
                            handleGameEnded(jsonMsg);
                            break;
                        case SKIPPED_TURN_EVENT:
                            handleSkippedTurn(jsonMsg);
                            break;
                        case SERVER_HANDSHAKE:
                            handleServerHandshake(jsonMsg);
                            break;
                        case GAME_ABORTED_EVENT:
                            handleGameAborted();
                            break;
                        default:
                            throw new BotException("Unsupported WebSocket message type: " + type);
                    }
                }
            }
            return Listener.super.onText(webSocket, data, last);
        }

        private void handleTick(JsonObject jsonMsg) {
            if (eventHandlingDisabled) return;

            tickStartNanoTime = System.nanoTime();

            var tickEventForBot = gson.fromJson(jsonMsg, TickEventForBot.class);

            var newTickEvent = EventMapper.map(tickEventForBot, baseBot);
            eventQueue.addEventsFromTick(newTickEvent);

            if (botIntent.getRescan() != null && botIntent.getRescan()) {
                botIntent.setRescan(false);
            }

            tickEvent = newTickEvent;

            // Trigger next turn (not tick-event!)
            botEventHandlers.onNextTurn.publish(tickEvent);
        }

        private void handleRoundStarted(JsonObject jsonMsg) {
            var roundStartedEvent = gson.fromJson(jsonMsg, RoundStartedEvent.class);

            botEventHandlers.onRoundStarted.publish(new RoundStartedEvent(roundStartedEvent.getRoundNumber()));
        }

        private void handleRoundEnded(JsonObject jsonMsg) {
            var roundEndedEvent = gson.fromJson(jsonMsg, RoundEndedEvent.class);

            botEventHandlers.onRoundEnded.publish(new RoundEndedEvent(
                    roundEndedEvent.getRoundNumber(), roundEndedEvent.getTurnNumber(), roundEndedEvent.getResults()));
        }

        private void handleGameStarted(JsonObject jsonMsg) {
            var gameStartedEventForBot = gson.fromJson(jsonMsg, GameStartedEventForBot.class);

            myId = gameStartedEventForBot.getMyId();

            teammateIds = gameStartedEventForBot.getTeammateIds() == null ?
                    Set.of() : new HashSet<>(gameStartedEventForBot.getTeammateIds());

            gameSetup = GameSetupMapper.map(gameStartedEventForBot.getGameSetup());

            // Send ready signal
            var ready = new BotReady();
            ready.setType(BotReady.Type.BOT_READY);

            String msg = gson.toJson(ready);
            socket.sendText(msg, true);

            botEventHandlers.onGameStarted.publish(
                    new GameStartedEvent(gameStartedEventForBot.getMyId(), gameSetup));
        }

        private void handleGameEnded(JsonObject jsonMsg) {
            // Send the game ended event
            var gameEndedEventForBot = gson.fromJson(jsonMsg, GameEndedEventForBot.class);

            var gameEndedEvent = new GameEndedEvent(
                    gameEndedEventForBot.getNumberOfRounds(),
                    map(gameEndedEventForBot.getResults()));

            botEventHandlers.onGameEnded.publish(gameEndedEvent);
        }

        private void handleGameAborted() {
            botEventHandlers.onGameAborted.publish(null);
        }

        private void handleSkippedTurn(JsonObject jsonMsg) {
            if (eventHandlingDisabled) return;

            var skippedTurnEvent = gson.fromJson(jsonMsg, dev.robocode.tankroyale.schema.SkippedTurnEvent.class);

            botEventHandlers.onSkippedTurn.publish((SkippedTurnEvent) EventMapper.map(skippedTurnEvent, baseBot));
        }

        private void handleServerHandshake(JsonObject jsonMsg) {
            serverHandshake = gson.fromJson(jsonMsg, ServerHandshake.class);

            // Reply by sending bot handshake
            var isDroid = baseBot instanceof Droid;
            var botHandshake = BotHandshakeFactory.create(serverHandshake.getSessionId(), botInfo, isDroid, serverSecret);
            String msg = gson.toJson(botHandshake);

            socket.sendText(msg, true);
        }
    }
}