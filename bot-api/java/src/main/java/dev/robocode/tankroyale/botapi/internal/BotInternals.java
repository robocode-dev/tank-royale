package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.concurrent.Callable;

import static dev.robocode.tankroyale.botapi.Constants.MAX_SPEED;
import static java.lang.Math.abs;

public final class BotInternals implements IStopResumeListener {

    private final Bot bot;
    private final BaseBotInternals baseBotInternals;

    private boolean overrideTurnRate;
    private boolean overrideGunTurnRate;
    private boolean overrideRadarTurnRate;
    private boolean overrideTargetSpeed;

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

        // Bot events have higher priorities than BaseBot events, meaning that these event will be executed first
        var instantEventHandlers = baseBotInternals.getInstantEventHandlers();
        instantEventHandlers.onGameAborted.subscribe(e -> onGameAborted(), 100);
        instantEventHandlers.onNextTurn.subscribe(this::onNextTurn, 90);
        instantEventHandlers.onRoundEnded.subscribe(e -> onRoundEnded(), 90);
        instantEventHandlers.onGameEnded.subscribe(this::onGameEnded, 90);
        instantEventHandlers.onDisconnected.subscribe(this::onDisconnected, 90);
        instantEventHandlers.onHitWall.subscribe(e -> onHitWall(), 90);
        instantEventHandlers.onHitBot.subscribe(this::onHitBot, 90);
        instantEventHandlers.onDeath.subscribe(this::onDeath, 90);
    }

    private void onNextTurn(TickEvent e) {
        if (e.getTurnNumber() == 1) {
            onFirstTurn();
        }
        processTurn();
    }

    private void onFirstTurn() {
        baseBotInternals.stopThread(); // sanity before starting a new thread (later)
        clearRemaining();
        baseBotInternals.startThread(bot);
    }

    private void clearRemaining() {
        distanceRemaining = 0;
        turnRemaining = 0;
        gunTurnRemaining = 0;
        radarTurnRemaining = 0;

        previousDirection = bot.getDirection();
        previousGunDirection = bot.getGunDirection();
        previousRadarDirection = bot.getRadarDirection();
    }

    private void onGameAborted() {
        baseBotInternals.stopThread();
    }

    private void onRoundEnded() {
        baseBotInternals.stopThread();
    }

    private void onGameEnded(GameEndedEvent e) {
        baseBotInternals.stopThread();
    }

    private void onDisconnected(DisconnectedEvent e) {
        baseBotInternals.stopThread();
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

    private void onHitWall() {
        distanceRemaining = 0;
    }

    private void onHitBot(HitBotEvent e) {
        if (e.isRammed()) {
            distanceRemaining = 0;
        }
    }

    private void onDeath(DeathEvent e) {
        baseBotInternals.stopThread();
    }

    public void setTurnRate(double turnRate) {
        overrideTurnRate = false;
        turnRemaining = toInfiniteValue(turnRate);
        baseBotInternals.setTurnRate(turnRate);
    }

    public void setGunTurnRate(double gunTurnRate) {
        overrideGunTurnRate = false;
        gunTurnRemaining = toInfiniteValue(gunTurnRate);
        baseBotInternals.setGunTurnRate(gunTurnRate);
    }

    public void setRadarTurnRate(double radarTurnRate) {
        overrideRadarTurnRate = false;
        radarTurnRemaining = toInfiniteValue(radarTurnRate);
        baseBotInternals.setRadarTurnRate(radarTurnRate);
    }

    private static double toInfiniteValue(double turnRate) {
        if (turnRate > 0) {
            return Double.POSITIVE_INFINITY;
        }
        if (turnRate < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return 0;
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
        overrideTargetSpeed = false;
        if (targetSpeed > 0) {
            distanceRemaining = Double.POSITIVE_INFINITY;
        } else if (targetSpeed < 0) {
            distanceRemaining = Double.NEGATIVE_INFINITY;
        } else {
            distanceRemaining = 0;
        }
        baseBotInternals.setTargetSpeed(targetSpeed);
    }

    public void setForward(double distance) {
        overrideTargetSpeed = true;
        if (Double.isNaN(distance)) {
            throw new IllegalArgumentException("'distance' cannot be NaN");
        }
        getAndSetNewTargetSpeed(distance);
        distanceRemaining = distance;
    }

    public void forward(double distance) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setForward(distance);
            waitFor(() -> distanceRemaining == 0 && bot.getSpeed() == 0);
        }
    }

    public void setTurnLeft(double degrees) {
        overrideTurnRate = true;
        turnRemaining = degrees;
        baseBotInternals.setTurnRate(degrees);
    }

    public void turnLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setTurnLeft(degrees);
            waitFor(() -> turnRemaining == 0);
        }
    }

    public void setTurnGunLeft(double degrees) {
        overrideGunTurnRate = true;
        gunTurnRemaining = degrees;
        baseBotInternals.setGunTurnRate(degrees);
    }

    public void turnGunLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setTurnGunLeft(degrees);
            waitFor(() -> gunTurnRemaining == 0);
        }
    }

    public void setTurnRadarLeft(double degrees) {
        overrideRadarTurnRate = true;
        radarTurnRemaining = degrees;
        baseBotInternals.setRadarTurnRate(degrees);
    }

    public void turnRadarLeft(double degrees) {
        if (bot.isStopped()) {
            bot.go(); // skip turn by doing nothing in the turn
        } else {
            setTurnRadarLeft(degrees);
            waitFor(() -> radarTurnRemaining == 0);
        }
    }

    public void fire(double firepower) {
        if (bot.setFire(firepower)) {
            bot.go();
        }
    }

    public void rescan() {
        baseBotInternals.setScannedBotEventInterruptible();
        bot.setRescan();
        bot.go();
    }

    public void waitFor(Callable<Boolean> condition) {
        do {
            bot.go();
        } while (baseBotInternals.isRunning() && !call(condition));
    }

    private boolean call(Callable<Boolean> condition) {
        try {
            return condition.call();
        } catch (Exception ex) {
            throw new BotException("Condition could not be computed", ex);
        }
    }

    public void stop(boolean overwrite) {
        baseBotInternals.setStop(overwrite);
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

        if (!overrideTurnRate) {
            return; // called after previous direction has been calculated and stored!
        }

        if (abs(turnRemaining) <= abs(delta)) {
            turnRemaining = 0;
        } else {
            turnRemaining -= delta;
            if (isNearZero(turnRemaining)) {
                turnRemaining = 0;
            }
        }
        baseBotInternals.setTurnRate(turnRemaining);
    }

    private void updateGunTurnRemaining() {
        double delta = bot.calcDeltaAngle(bot.getGunDirection(), previousGunDirection);
        previousGunDirection = bot.getGunDirection();

        if (!overrideGunTurnRate) {
            return; // called after previous direction has been calculated and stored!
        }

        if (abs(gunTurnRemaining) <= abs(delta)) {
            gunTurnRemaining = 0;
        } else {
            gunTurnRemaining -= delta;
            if (isNearZero(gunTurnRemaining)) {
                gunTurnRemaining = 0;
            }
        }
        baseBotInternals.setGunTurnRate(gunTurnRemaining);
    }

    private void updateRadarTurnRemaining() {
        double delta = bot.calcDeltaAngle(bot.getRadarDirection(), previousRadarDirection);
        previousRadarDirection = bot.getRadarDirection();

        if (!overrideRadarTurnRate) {
            return; // called after previous direction has been calculated and stored!
        }

        if (abs(radarTurnRemaining) <= abs(delta)) {
            radarTurnRemaining = 0;
        } else {
            radarTurnRemaining -= delta;
            if (isNearZero(radarTurnRemaining)) {
                radarTurnRemaining = 0;
            }
        }
        baseBotInternals.setRadarTurnRate(radarTurnRemaining);
    }

    private void updateMovement() {
        if (!overrideTargetSpeed) {
            if (abs(distanceRemaining) < abs(bot.getSpeed())) {
                distanceRemaining = 0;
            } else {
                distanceRemaining -= bot.getSpeed();
            }
        } else if (Double.isInfinite(distanceRemaining)) {
            baseBotInternals.setTargetSpeed(distanceRemaining == Double.POSITIVE_INFINITY ? MAX_SPEED : -MAX_SPEED);
        } else {
            double distance = distanceRemaining;

            // This is Nat Pavasant's method described here:
            // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            double newSpeed = getAndSetNewTargetSpeed(distance);

            // If we are over-driving our distance, and we are now at speed=0 then we stopped
            if (isNearZero(newSpeed) && isOverDriving) {
                distance = 0;
                isOverDriving = false;
            }

            // the overdrive flag
            if (Math.signum(distance * newSpeed) != -1) {
                isOverDriving = baseBotInternals.getDistanceTraveledUntilStop(newSpeed) > abs(distance);
            }

            distanceRemaining = distance - newSpeed;
        }
    }

    private double getAndSetNewTargetSpeed(double distance) {
        double speed = baseBotInternals.getNewTargetSpeed(bot.getSpeed(), distance);
        baseBotInternals.setTargetSpeed(speed);
        return speed;
    }

    private boolean isNearZero(double value) {
        return abs(value) < .00001;
    }
}
