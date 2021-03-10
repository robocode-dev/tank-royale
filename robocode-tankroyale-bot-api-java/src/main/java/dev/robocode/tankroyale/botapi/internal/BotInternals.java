package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.Bot;
import dev.robocode.tankroyale.botapi.events.*;

import static dev.robocode.tankroyale.botapi.IBaseBot.MAX_SPEED;

public final class BotInternals implements StopResumeListener {

  private final Bot bot;
  private final BaseBotInternals baseBotInternals;

  private double distanceRemaining;
  private double turnRemaining;
  private double gunTurnRemaining;
  private double radarTurnRemaining;

  private boolean isOverDriving;

  private Thread thread;
  private final Object nextTurn = new Object();
  private volatile boolean isRunning;

  private double savedDistanceRemaining;
  private double savedTurnRemaining;
  private double savedGunTurnRemaining;
  private double savedRadarTurnRemaining;

  public BotInternals(Bot bot, BaseBotInternals baseBotInternals) {
    this.bot = bot;
    this.baseBotInternals = baseBotInternals;

    baseBotInternals.setStopResumeHandler(this);

    BotEventHandlers botEventHandlers = baseBotInternals.getBotEventHandlers();
    botEventHandlers.onProcessTurn.subscribe(this::onProcessTurn, 90);
    botEventHandlers.onDisconnected.subscribe(this::onDisconnected, 90);
    botEventHandlers.onGameEnded.subscribe(this::onGameEnded, 90);
    botEventHandlers.onHitBot.subscribe(this::onHitBot, 90);
    botEventHandlers.onHitWall.subscribe(e -> onHitWall(), 90);
    botEventHandlers.onBotDeath.subscribe(this::onDeath, 90);
    botEventHandlers.onNewRound.subscribe(e -> onNewRound(), 90);
  }

  private void onDisconnected(DisconnectedEvent e) {
    stopThread();
  }

  private void onGameEnded(GameEndedEvent e) {
    stopThread();
  }

  private void onNewRound() {
    distanceRemaining = 0d;
    turnRemaining = 0d;
    gunTurnRemaining = 0d;
    radarTurnRemaining = 0d;

    stopThread();
    startThread();
  }

  private void onProcessTurn(TickEvent e) {
    processTurn();
  }

  private void onHitBot(HitBotEvent e) {
    if (e.isRammed()) {
      distanceRemaining = 0;
    }
  }

  private void onHitWall() {
    distanceRemaining = 0;
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
    blockIfStopped();
    setForward(distance);
    awaitMovementComplete();
  }

  public void setTurnLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    turnRemaining = degrees;
    baseBotInternals.getBotIntent().setTurnRate(degrees);
  }

  public void turnLeft(double degrees) {
    blockIfStopped();
    setTurnLeft(degrees);
    awaitTurnComplete();
    baseBotInternals.getBotIntent().setTurnRate(0d);
  }

  public void setTurnGunLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    gunTurnRemaining = degrees;
    baseBotInternals.getBotIntent().setGunTurnRate(degrees);
  }

  public void turnGunLeft(double degrees) {
    blockIfStopped();
    setTurnGunLeft(degrees);
    awaitGunTurnComplete();
    baseBotInternals.getBotIntent().setGunTurnRate(0d);
  }

  public void setTurnRadarLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    radarTurnRemaining = degrees;
    baseBotInternals.getBotIntent().setRadarTurnRate(degrees);
    baseBotInternals.getBotIntent().setRadarTurnRate(0d);
  }

  public void turnRadarLeft(double degrees) {
    blockIfStopped();
    setTurnRadarLeft(degrees);
    awaitRadarTurnComplete();
  }

  public void fire(double firepower) { // TODO: Return boolean
    bot.setFire(firepower);
    awaitNextTurn();
  }

  public void scan() {
    bot.setScan();
    awaitNextTurn();
  }

  private void processTurn() {
    // No movement is possible, when the bot has become disabled
    if (bot.isDisabled()) {
      distanceRemaining = 0;
      turnRemaining = 0;
      gunTurnRemaining = 0;
      radarTurnRemaining = 0;
    }

    updateTurnRemaining();
    updateGunTurnRemaining();
    updateRadarTurnRemaining();
    updateMovement();

    synchronized (nextTurn) {
      // Unblock methods waiting for the next turn
      nextTurn.notifyAll();
    }
  }

  private void startThread() {
    isRunning = true; // Set this before the thread is starting as run() needs it to be set
    thread = new Thread(bot::run);
    thread.start();
  }

  private void stopThread() {
    isRunning = false;
    if (thread != null) {
      thread.interrupt();
      try {
        thread.join(100, 0);
      } catch (InterruptedException ignored) {
      }
      thread = null;
    }
  }

  private void updateTurnRemaining() {
    if (bot.doAdjustGunForBodyTurn()) {
      double oldSign = Math.signum(gunTurnRemaining);
      gunTurnRemaining -= bot.getTurnRate();
      double newSign = Math.signum(gunTurnRemaining);

      if (oldSign != newSign) {
        gunTurnRemaining = 0;
      }
    }

    double oldSign = Math.signum(turnRemaining);
    turnRemaining -= bot.getTurnRate();
    double newSign = Math.signum(turnRemaining);

    if (oldSign != newSign) {
      turnRemaining = 0;
    }
  }

  private void updateGunTurnRemaining() {
    if (bot.doAdjustRadarForGunTurn()) {
      double oldSign = Math.signum(radarTurnRemaining);
      radarTurnRemaining -= bot.getGunTurnRate();
      double newSign = Math.signum(radarTurnRemaining);

      if (oldSign != newSign) {
        radarTurnRemaining = 0;
      }
    }

    double oldSign = Math.signum(gunTurnRemaining);
    gunTurnRemaining -= bot.getGunTurnRate();
    double newSign = Math.signum(gunTurnRemaining);

    if (oldSign != newSign) {
      gunTurnRemaining = 0;
    }
  }

  private void updateRadarTurnRemaining() {
    double oldSign = Math.signum(radarTurnRemaining);
    radarTurnRemaining -= bot.getRadarTurnRate();
    double newSign = Math.signum(radarTurnRemaining);

    if (oldSign != newSign) {
      radarTurnRemaining = 0;
    }
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
        isOverDriving = baseBotInternals.getDistanceTraveledUntilStop(speed) > Math.abs(distance);
      }

      distanceRemaining = distance - speed;
    }
  }

  public void stop() {
    baseBotInternals.setStop();
    awaitNextTurn();
  }

  public void resume() {
    baseBotInternals.setResume();
    awaitNextTurn();
  }

  public void onStop() {
    savedDistanceRemaining = distanceRemaining;
    savedTurnRemaining = turnRemaining;
    savedGunTurnRemaining = gunTurnRemaining;
    savedRadarTurnRemaining = radarTurnRemaining;

    distanceRemaining = 0d;
    turnRemaining = 0d;
    gunTurnRemaining = 0d;
    radarTurnRemaining = 0d;
  }

  public void onResume() {
    distanceRemaining = savedDistanceRemaining;
    turnRemaining = savedTurnRemaining;
    gunTurnRemaining = savedGunTurnRemaining;
    radarTurnRemaining = savedRadarTurnRemaining;
  }

  private void blockIfStopped() {
    if (baseBotInternals.isStopped()) {
      await(() -> !baseBotInternals.isStopped());
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
        baseBotInternals.setResume();
      }
    }
  }

  @FunctionalInterface
  public interface ICondition {
    boolean test();
  }
}
