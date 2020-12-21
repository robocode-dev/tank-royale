package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

import java.util.concurrent.atomic.AtomicBoolean;

public final class BotInternals {

  private final Bot bot;
  private final BaseBotInternals baseBotInternals;

  private double distanceRemaining;
  private double turnRemaining;
  private double gunTurnRemaining;
  private double radarTurnRemaining;

  private boolean isCollidingWithWall;
  private boolean isOverDriving;

  private TickEvent currentTick;

  private Thread thread;
  private final Object nextTurn = new Object();
  private volatile boolean isRunning;
  private volatile boolean isStopped;

  private double savedDistanceRemaining;
  private double savedTurnRemaining;
  private double savedGunTurnRemaining;
  private double savedRadarTurnRemaining;

  public BotInternals(Bot bot, BaseBotInternals baseBotInternals) {
    this.bot = bot;
    this.baseBotInternals = baseBotInternals;

    BotEventHandlers botEventHandlers = baseBotInternals.getBotEventHandlers();
    botEventHandlers.onProcessTurn.subscribe(this::onProcessTurn, 100);
    botEventHandlers.onDisconnected.subscribe(this::onDisconnected, 100);
    botEventHandlers.onGameEnded.subscribe(this::onGameEnded, 100);
    botEventHandlers.onHitBot.subscribe(this::onHitBot, 100);
    botEventHandlers.onHitWall.subscribe(e -> onHitWall(), 100);
    botEventHandlers.onBotDeath.subscribe(this::onDeath, 100);
  }

  private void onDisconnected(DisconnectedEvent e) {
    stopThread();
  }

  private void onGameEnded(GameEndedEvent e) {
    stopThread();
  }

  private void onProcessTurn(TickEvent e) {
    currentTick = e;
    processTurn();
  }

  private void onHitBot(HitBotEvent e) {
    if (e.isRammed()) {
      distanceRemaining = 0;
    }
  }

  private void onHitWall() {
    distanceRemaining = 0;
    isCollidingWithWall = true;
  }

  private void onDeath(DeathEvent e) {
    if (e.getVictimId() == bot.getMyId()) {
      stopThread();
    }
  }

  public boolean isRunning() {
    return isRunning;
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

  public void setForward(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    distanceRemaining = distance;
    double speed = baseBotInternals.getNewSpeed(bot.getSpeed(), distance);
    bot.setTargetSpeed(speed);
  }

  public void forward(double distance) {
    blockIfStopped();
    setForward(distance);
    awaitMovementComplete();
  }

  public void setTurnLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    turnRemaining = degrees;
    bot.setTurnRate(degrees);
  }

  public void turnLeft(double degrees) {
    blockIfStopped();
    setTurnLeft(degrees);
    awaitTurnComplete();
  }

  public void setTurnGunLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    gunTurnRemaining = degrees;
    bot.setGunTurnRate(degrees);
  }

  public void turnGunLeft(double degrees) {
    blockIfStopped();
    setTurnGunLeft(degrees);
    awaitGunTurnComplete();
  }

  public void setTurnRadarLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    radarTurnRemaining = degrees;
    bot.setRadarTurnRate(degrees);
  }

  public void turnRadarLeft(double degrees) {
    blockIfStopped();
    setTurnRadarLeft(degrees);
    awaitRadarTurnComplete();
  }

  public void fire(double firepower) {
      while (!bot.setFire(firepower)) {
        awaitNextTurn();
      }
  }

  public boolean scan() {
    bot.setScan();
    awaitNextTurn();

    // If a ScannedBotEvent is put in the events, the bot scanned another bot
    return bot.getEvents().stream().anyMatch(e -> e instanceof ScannedBotEvent);
  }

  private void processTurn() {
    // No movement is possible, when the bot has become disabled
    if (bot.isDisabled()) {
      distanceRemaining = 0;
      turnRemaining = 0;
    }
    updateHeadings();
    updateMovement();

    // Reset collision flag after updating movement
    isCollidingWithWall = false;

    // If this is the first turn -> Call the run method on the Bot class
    if (currentTick.getTurnNumber() == 1) { // TODO: Use onNewRound event?
      if (isRunning) {
        stopThread();
      }
      startThread();
    }

    synchronized (nextTurn) {
      // Unblock methods waiting for the next turn
      nextTurn.notifyAll();
    }
  }

  private void startThread() {
    thread = new Thread(bot::run);
    thread.start();
    isRunning = true;
  }

  private void stopThread() {
    if (thread != null) {
      isRunning = false;
      thread.interrupt();
      try {
        thread.join();
      } catch (InterruptedException ignored) {
      }
      thread = null;
    }
  }

  /** Updates the bot heading, gun heading, and radar heading. */
  private void updateHeadings() {
    updateTurnRemaining();
    updateGunTurnRemaining();
    updateRadarTurnRemaining();
  }

  private void updateTurnRemaining() {
    if (bot.doAdjustGunForBodyTurn()) {
      gunTurnRemaining -= bot.getTurnRate();
    }
    turnRemaining -= bot.getTurnRate();

    bot.setTurnRate(turnRemaining);
  }

  private void updateGunTurnRemaining() {
    if (bot.doAdjustRadarForGunTurn()) {
      radarTurnRemaining -= bot.getGunTurnRate();
    }
    gunTurnRemaining -= bot.getGunTurnRate();

    bot.setGunTurnRate(gunTurnRemaining);
  }

  private void updateRadarTurnRemaining() {
    radarTurnRemaining -= bot.getRadarTurnRate();

    bot.setRadarTurnRate(radarTurnRemaining);
  }

  // This is Nat Pavasant's method described here:
  // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
  private void updateMovement() {
    if (isCollidingWithWall) { // TODO: add check for collision with bot?
      return;
    }

    double distance = distanceRemaining;
    if (Double.isNaN(distance)) {
      distance = 0;
    }

    double speed = baseBotInternals.getNewSpeed(bot.getSpeed(), distance);
    bot.setTargetSpeed(speed);

    // If we are over-driving our distance and we are now at velocity=0 then we stopped
    if (isNearZero(speed) && isOverDriving) {
      distanceRemaining = 0;
      distance = 0;
      isOverDriving = false;
    }

    // If we are moving normally and the breaking distance is more than remaining distance, enable
    // the overdrive flag
    if (Math.signum(distance * speed) != -1) {
      isOverDriving = baseBotInternals.getDistanceTraveledUntilStop(speed) > Math.abs(distance);
    }

    distanceRemaining = distance - speed;
  }

  public boolean isStopped() {
    return isStopped;
  }

  public void stop() {
    setStop();
    awaitNextTurn();
  }

  public void resume() {
    setResume();
    awaitNextTurn();
  }

  public void setStop() {
    if (!isStopped) {
      isStopped = true;

      savedDistanceRemaining = distanceRemaining;
      savedTurnRemaining = turnRemaining;
      savedGunTurnRemaining = gunTurnRemaining;
      savedRadarTurnRemaining = radarTurnRemaining;
    }
    distanceRemaining = 0d;
    turnRemaining = 0d;
    gunTurnRemaining = 0d;
    radarTurnRemaining = 0d;

    bot.setTargetSpeed(0);
    bot.setTurnRate(0);
    bot.setGunTurnRate(0);
    bot.setRadarTurnRate(0);
  }

  public void setResume() {
    if (isStopped) {
      isStopped = false;

      distanceRemaining = savedDistanceRemaining;
      turnRemaining = savedTurnRemaining;
      gunTurnRemaining = savedGunTurnRemaining;
      radarTurnRemaining = savedRadarTurnRemaining;
    }
  }

  private void blockIfStopped() {
    if (isStopped) {
      await(() -> !isStopped);
    }
  }

  private boolean isNearZero(double value) {
    return (Math.abs(value) < .00001);
  }

  private void awaitMovementComplete() {
    await(() -> distanceRemaining == 0);
  }

  private void awaitTurnComplete() {
    await(() -> turnRemaining == 0);
  }

  private void awaitGunTurnComplete() {
    await(() -> gunTurnRemaining == 0);
  }

  private void awaitRadarTurnComplete() {
    await(() -> radarTurnRemaining == 0);
  }

  private void awaitNextTurn() {
    int turnNumber = bot.getTurnNumber();
    await(() -> bot.getTurnNumber() > turnNumber);
  }

  public void await(ICondition condition) {
    // Loop while bot is running and condition has not been met
    synchronized (nextTurn) {
      try {
        while (isRunning && !condition.test()) {
          bot.go();
          nextTurn.wait(); // Wait for next turn
        }
      } catch (InterruptedException e) {
        isRunning = false;
        isStopped = false;
      }
    }
  }

  @FunctionalInterface
  public interface ICondition {
    boolean test();
  }
}
