package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

import static dev.robocode.tankroyale.botapi.Constants.MAX_SPEED;
import static java.lang.Math.abs;

public final class BotInternals implements IStopResumeListener {

    private final Bot bot;
    private final BaseBotInternals baseBotInternals;
    private final Object threadMonitor = new Object();
    private Thread thread;
    private double previousDirection;
    private double previousGunDirection;
    private double previousRadarDirection;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private boolean isOverDriving;

    private double savedPreviousDirection;
    private double savedPreviousGunDirection;
    private double savedPreviousRadarDirection;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    public BotInternals(Bot bot, BaseBotInternals baseBotInternals) {
        this.bot = bot;
        this.baseBotInternals = baseBotInternals;

        baseBotInternals.setStopResumeHandler(this);

        BotEventHandlers botEventHandlers = baseBotInternals.getBotEventHandlers();
        botEventHandlers.onGameAborted.subscribe(e -> onGameAborted(), 100);
        botEventHandlers.onNextTurn.subscribe(this::onNextTurn, 90);
        botEventHandlers.onRoundEnded.subscribe(e -> onRoundEnded(), 90);
        botEventHandlers.onGameEnded.subscribe(this::onGameEnded, 90);
        botEventHandlers.onDisconnected.subscribe(this::onDisconnected, 90);
        botEventHandlers.onHitWall.subscribe(e -> onHitWall(), 90);
        botEventHandlers.onHitBot.subscribe(this::onHitBot, 90);
        botEventHandlers.onBotDeath.subscribe(this::onDeath, 90);
    }

    private void onNextTurn(TickEvent e) {
        if (e.getTurnNumber() == 1) {
            onFirstTurn();
        }
        processTurn();
    }

    private void onFirstTurn() {
        stopThread(); // sanity before starting a new thread (later)
        clearRemaining();
        startThread();
    }

    private void clearRemaining() {
        distanceRemaining = 0d;
        turnRemaining = 0d;
        gunTurnRemaining = 0d;
        radarTurnRemaining = 0d;

        previousDirection = bot.getDirection();
        previousGunDirection = bot.getGunDirection();
        previousRadarDirection = bot.getRadarDirection();
    }

    private void onGameAborted() {
        stopThread();
    }

    private void onRoundEnded() {
        stopThread();
    }

    private void onGameEnded(GameEndedEvent e) {
        stopThread();
    }

    private void onDisconnected(DisconnectedEvent e) {
        stopThread();
    }

    private void processTurn() {
        // No movement is possible, when the bot has become disabled
        if (bot.isDisabled()) {
            clearRemaining();
        } else {
            updateTurnRemaining();
            updateGunTurnRemaining();
            updateRadarTurnRemaining();
            updateMovement();
        }
    }

    private void startThread() {
        synchronized (threadMonitor) {
            thread = new Thread(bot::run);
            thread.start();
        }
    }

    private void stopThread() {
        synchronized (threadMonitor) {
            if (thread != null) {
                thread.interrupt();
                thread = null;
            }
        }
    }

    private void onHitWall() {
        distanceRemaining = 0;
    }

    private void onHitBot(HitBotEvent e) {
        if (e.isRammed()) {
            distanceRemaining = 0;
        }
    }

    private void onDeath(DeathEvent e) {
        if (e.getVictimId() == bot.getMyId()) {
            stopThread();
        }
    }

    public boolean isRunning() {
        return thread != null && !thread.isInterrupted();
    }

    public double getDistanceRemaining() {
        return distanceRemaining;
    }

    public double getTurnRemaining() {
        return turnRemaining;
    }

    public double getGunTurnRemaining() {
        return gunTurnRemaining;
    }

    public double getRadarTurnRemaining() {
        return radarTurnRemaining;
    }

    public void setTargetSpeed(double targetSpeed) {
        if (Double.isNaN(targetSpeed)) {
            throw new IllegalArgumentException("targetSpeed cannot be NaN");
        }
        if (targetSpeed > 0) {
            distanceRemaining = Double.POSITIVE_INFINITY;
        } else if (targetSpeed < 0) {
            distanceRemaining = Double.NEGATIVE_INFINITY;
        } else {
            distanceRemaining = 0;
        }
        baseBotInternals.getBotIntent().setTargetSpeed(targetSpeed);
    }

    public void setForward(double distance) {
        if (Double.isNaN(distance)) {
            throw new IllegalArgumentException("distance cannot be NaN");
        }
        distanceRemaining = distance;
        double speed = baseBotInternals.getNewSpeed(bot.getSpeed(), distance);
        baseBotInternals.getBotIntent().setTargetSpeed(speed);
    }

    public void forward(double distance) {
        if (bot.isStopped()) {
            bot.go();
        } else {
            setForward(distance);
            do {
                bot.go();
            } while (isRunning() && distanceRemaining != 0);
        }
    }

    public void setTurnLeft(double degrees) {
        if (Double.isNaN(degrees)) {
            throw new IllegalArgumentException("degrees cannot be NaN");
        }
        turnRemaining = degrees;
        baseBotInternals.getBotIntent().setTurnRate(degrees);
    }

    public void turnLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go();
        } else {
            setTurnLeft(degrees);
            do {
                bot.go();
            } while (isRunning() && turnRemaining != 0);
        }
    }

    public void setTurnGunLeft(double degrees) {
        if (Double.isNaN(degrees)) {
            throw new IllegalArgumentException("degrees cannot be NaN");
        }
        gunTurnRemaining = degrees;
        baseBotInternals.getBotIntent().setGunTurnRate(degrees);
    }

    public void turnGunLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go();
        } else {
            setTurnGunLeft(degrees);
            do {
                bot.go();
            } while (isRunning() && gunTurnRemaining != 0);
        }
    }

    public void setTurnRadarLeft(double degrees) {
        if (Double.isNaN(degrees)) {
            throw new IllegalArgumentException("degrees cannot be NaN");
        }
        radarTurnRemaining = degrees;
        baseBotInternals.getBotIntent().setRadarTurnRate(degrees);
    }

    public void turnRadarLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go();
        } else {
            setTurnRadarLeft(degrees);
            do {
                bot.go();
            } while (isRunning() && radarTurnRemaining != 0);
        }
    }

    public void fire(double firepower) {
        if (bot.setFire(firepower)) {
            bot.go();
        }
    }

    public void scan() {
        bot.setScan();
        boolean scan = baseBotInternals.getBotIntent().getScan();
        bot.go();
        if (scan && bot.getEvents().stream().anyMatch(e -> e instanceof ScannedBotEvent)) {
            // Interrupt event handler by throwing exception
            throw new RescanException();
        }
    }

    public void waitFor(Condition condition) {
        while (isRunning() && !condition.test()) {
            bot.go();
        }
    }

    public void stop() {
        baseBotInternals.setStop();
        bot.go();
    }

    public void resume() {
        baseBotInternals.setResume();
        bot.go();
    }

    public void onStop() {
        savedPreviousDirection = previousDirection;
        savedPreviousGunDirection = previousGunDirection;
        savedPreviousRadarDirection = previousRadarDirection;

        savedDistanceRemaining = distanceRemaining;
        savedTurnRemaining = turnRemaining;
        savedGunTurnRemaining = gunTurnRemaining;
        savedRadarTurnRemaining = radarTurnRemaining;
    }

    public void onResume() {
        previousDirection = savedPreviousDirection;
        previousGunDirection = savedPreviousGunDirection;
        previousRadarDirection = savedPreviousRadarDirection;

        distanceRemaining = savedDistanceRemaining;
        turnRemaining = savedTurnRemaining;
        gunTurnRemaining = savedGunTurnRemaining;
        radarTurnRemaining = savedRadarTurnRemaining;
    }

    private void updateTurnRemaining() {
        double delta = bot.calcDeltaAngle(bot.getDirection(), previousDirection);
        previousDirection = bot.getDirection();

        if (abs(turnRemaining) <= abs(delta)) {
            turnRemaining = 0;
        } else {
            turnRemaining -= delta;
            if (isNearZero(turnRemaining)) {
                turnRemaining = 0;
            }
        }
        bot.setTurnRate(turnRemaining);
    }

    private void updateGunTurnRemaining() {
        double delta = bot.calcDeltaAngle(bot.getGunDirection(), previousGunDirection);
        previousGunDirection = bot.getGunDirection();

        if (abs(gunTurnRemaining) <= abs(delta)) {
            gunTurnRemaining = 0;
        } else {
            gunTurnRemaining -= delta;
            if (isNearZero(gunTurnRemaining)) {
                gunTurnRemaining = 0;
            }
        }
        bot.setGunTurnRate(gunTurnRemaining);
    }

    private void updateRadarTurnRemaining() {
        double delta = bot.calcDeltaAngle(bot.getRadarDirection(), previousRadarDirection);
        previousRadarDirection = bot.getRadarDirection();

        if (abs(radarTurnRemaining) <= abs(delta)) {
            radarTurnRemaining = 0;
        } else {
            radarTurnRemaining -= delta;
            if (isNearZero(radarTurnRemaining)) {
                radarTurnRemaining = 0;
            }
        }
        bot.setRadarTurnRate(radarTurnRemaining);
    }

    private void updateMovement() {
        if (Double.isInfinite(distanceRemaining)) {
            baseBotInternals
                    .getBotIntent()
                    .setTargetSpeed(
                            (double) (distanceRemaining == Double.POSITIVE_INFINITY ? MAX_SPEED : -MAX_SPEED));

        } else {
            double distance = distanceRemaining;

            // This is Nat Pavasant's method described here:
            // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            double speed = baseBotInternals.getNewSpeed(bot.getSpeed(), distance);
            baseBotInternals.getBotIntent().setTargetSpeed(speed);

            // If we are over-driving our distance and we are now at velocity=0 then we stopped
            if (isNearZero(speed) && isOverDriving) {
                distanceRemaining = 0;
                distance = 0;
                isOverDriving = false;
            }

            // the overdrive flag
            if (Math.signum(distance * speed) != -1) {
                isOverDriving = baseBotInternals.getDistanceTraveledUntilStop(speed) > abs(distance);
            }

            distanceRemaining = distance - speed;
        }
    }

    private boolean isNearZero(double value) {
        return (abs(value) < .00001);
    }
}
