package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.IBot;
import dev.robocode.tankroyale.botapi.events.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public final class BotInternals {

  private final double absDeceleration = Math.abs(IBot.DECELERATION);

  private final Bot bot;

  public double maxSpeed = IBot.MAX_SPEED;
  public double maxTurnRate = IBot.MAX_TURN_RATE;
  public double maxGunTurnRate = IBot.MAX_GUN_TURN_RATE;
  public double maxRadarTurnRate = IBot.MAX_RADAR_TURN_RATE;

  public double distanceRemaining;
  public double turnRemaining;
  public double gunTurnRemaining;
  public double radarTurnRemaining;

  private boolean isCollidingWithWall;
  private boolean isCollidingWithBot;
  private boolean isOverDriving;

  private TickEvent currentTick;

  private Thread thread;
  private final Object nextTurn = new Object();
  public volatile boolean isRunning;
  volatile boolean isStopped;

  private double savedDistanceRemaining;
  private double savedTurnRemaining;
  private double savedGunTurnRemaining;
  private double savedRadarTurnRemaining;

  public BotInternals(Bot bot, BotEvents botEvents) {
    this.bot = bot;

    botEvents.onProcessTurn.subscribe(this::onProcessTurn, 100);
    botEvents.onDisconnected.subscribe(this::onDisconnected, 100);
    botEvents.onGameEnded.subscribe(this::onGameEnded, 100);
    botEvents.onHitBot.subscribe(this::onHitBot, 100);
    botEvents.onHitWall.subscribe(e -> onHitWall(), 100);
    botEvents.onBotDeath.subscribe(this::onDeath, 100);
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
    isCollidingWithBot = true;
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

  private void processTurn() {
    // No movement is possible, when the bot has become disabled
    if (bot.isDisabled()) {
      distanceRemaining = 0;
      turnRemaining = 0;
    }
    updateHeadings();
    updateMovement();

    // Reset collision flags after updating movement
    isCollidingWithWall = false;
    isCollidingWithBot = false;

    // If this is the first turn -> Call the run method on the Bot class
    if (currentTick.getTurnNumber() == 1) { // TODO: Use onNewRound event?
      if (isRunning) {
        stopThread();
      }
      startThread();
    }

    // Unblock methods waiting for the next turn
    synchronized (nextTurn) {
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
    if (isCollidingWithWall/* || isCollidingWithBot*/) { // TODO: add check for collision with bot?
      return;
    }

    double distance = distanceRemaining;
    if (Double.isNaN(distance)) {
      distance = 0;
    }
    double speed = getNewSpeed(bot.getSpeed(), distance);
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
      isOverDriving = getDistanceTraveledUntilStop(speed) > Math.abs(distance);
    }

    distanceRemaining = distance - speed;
  }

  /**
   * Returns the new speed based on the current speed and distance to move.
   *
   * @param speed is the current speed
   * @param distance is the distance to move
   * @return The new speed
   */
  // Credits for this algorithm goes to Patrick Cupka (aka Voidious),
  // Julian Kent (aka Skilgannon), and Positive:
  // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
  public double getNewSpeed(double speed, double distance) {

    if (distance < 0) {
      // If the distance is negative, then change it to be positive and change the sign of the
      // input velocity and the result
      return -getNewSpeed(-speed, -distance);
    }

    final double targetSpeed;
    if (distance == Double.POSITIVE_INFINITY) {
      targetSpeed = maxSpeed;
    } else {
      targetSpeed = min(getMaxSpeed(distance), maxSpeed);
    }

    if (speed >= 0) {
      return max(speed - absDeceleration, min(targetSpeed, speed + IBot.ACCELERATION));
    } // else
    return max(speed - IBot.ACCELERATION, min(targetSpeed, speed + getMaxDeceleration(-speed)));
  }

  private double getMaxSpeed(double distance) {
    double decelTime =
        max(
            1,
            Math.ceil( // sum of 0... decelTime, solving for decelTime using quadratic formula
                (Math.sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));

    if (decelTime == Double.POSITIVE_INFINITY) {
      return IBot.MAX_SPEED;
    }

    double decelDist =
        (decelTime / 2)
            * (decelTime - 1) // sum of 0..(decelTime-1)
            * absDeceleration;

    return ((decelTime - 1) * absDeceleration) + ((distance - decelDist) / decelTime);
  }

  private double getMaxDeceleration(double speed) {
    double decelTime = speed / absDeceleration;
    double accelTime = (1 - decelTime);

    return min(1, decelTime) * absDeceleration + max(0, accelTime) * IBot.ACCELERATION;
  }

  private double getDistanceTraveledUntilStop(double speed) {
    speed = Math.abs(speed);
    double distance = 0;
    while (speed > 0) {
      distance += (speed = getNewSpeed(speed, 0));
    }
    return distance;
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

  private boolean isNearZero(double value) {
    return (Math.abs(value) < .00001);
  }

  public void awaitMovementComplete() {
    await(() -> distanceRemaining == 0);
  }

  public void awaitTurnComplete() {
    await(() -> turnRemaining == 0);
  }

  public void awaitGunTurnComplete() {
    await(() -> gunTurnRemaining == 0);
  }

  public void awaitRadarTurnComplete() {
    await(() -> radarTurnRemaining == 0);
  }

  public void awaitGunFired() {
    await(() -> bot.getGunHeat() > 0);
  }

  public void awaitNextTurn() {
    int turnNumber = bot.getTurnNumber();
    await(() -> bot.getTurnNumber() > turnNumber);
  }

  public void await(ICondition condition) {
    // Loop while bot is running and condition has not been met
    try {
      while (isRunning && !condition.test()) {
        bot.go();
        synchronized (nextTurn) {
          nextTurn.wait();
        }
      }
    } catch (InterruptedException e) {
      isRunning = false;
    }
  }

  public interface ICondition {
    boolean test();
  }
}
