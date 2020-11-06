package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;
import static java.lang.Math.min;

final class BotInternals {

  private final double absDeceleration = Math.abs(IBot.DECELERATION);

  private final Bot bot;
  private final BotEvents botEvents;

  double maxSpeed = IBot.MAX_SPEED;
  double maxTurnRate = IBot.MAX_TURN_RATE;
  double maxGunTurnRate = IBot.MAX_GUN_TURN_RATE;
  double maxRadarTurnRate = IBot.MAX_RADAR_TURN_RATE;

  double distanceRemaining;
  double turnRemaining;
  double gunTurnRemaining;
  double radarTurnRemaining;

  private boolean isCollidingWithWall;
  private boolean isCollidingWithBot;
  private boolean isOverDriving;

  private TickEvent currentTick;

  private Thread thread;
  private final Object nextTurn = new Object();
  volatile boolean isRunning;
  volatile AtomicBoolean isStopped = new AtomicBoolean();

  private double savedDistanceRemaining;
  private double savedTurnRemaining;
  private double savedGunTurnRemaining;
  private double savedRadarTurnRemaining;

  private final LinkedHashMap<Class<?>, Command> pendingCommands = new LinkedHashMap<>();

  BotInternals(Bot bot, BotEvents botEvents) {
    this.bot = bot;
    this.botEvents = botEvents;

    botEvents.onDisconnected.subscribe(this::onDisconnected, 100);
    botEvents.onGameEnded.subscribe(this::onGameEnded, 100);
    botEvents.onHitBot.subscribe(this::onHitBot, 100);
    botEvents.onHitWall.subscribe(e -> onHitWall(), 100);
    botEvents.onTick.subscribe(this::onTick, 100);
    botEvents.onBotDeath.subscribe(this::onDeath, 100);
  }

  private void onDisconnected(DisconnectedEvent e) {
    stopThread();
  }

  private void onGameEnded(GameEndedEvent e) {
    stopThread();
  }

  private void onTick(TickEvent e) {
    currentTick = e;
    processTurn();
  }

  private void onHitBot(BotHitBotEvent e) {
    if (e.isRammed()) {
      distanceRemaining = 0;
    }
    isCollidingWithBot = true;
  }

  private void onHitWall() {
    distanceRemaining = 0;
    isCollidingWithWall = true;
  }

  private void onDeath(BotDeathEvent e) {
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
    if (currentTick.getTurnNumber() == 1) {
      if (isRunning) {
        stopThread();
      }
      startThread();
    }

    // Unblock waiting methods
    synchronized (nextTurn) {
      // Let's go ;-)
      bot.go();

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
    if (isCollidingWithBot) {
      return;
    }
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
    if (isCollidingWithWall) {
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
  double getNewSpeed(double speed, double distance) {

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

  void stop() {
    synchronized (isStopped) {
      if (!isStopped.get()) {
        savedDistanceRemaining = distanceRemaining;
        savedTurnRemaining = turnRemaining;
        savedGunTurnRemaining = gunTurnRemaining;
        savedRadarTurnRemaining = radarTurnRemaining;

        distanceRemaining = 0d;
        turnRemaining = 0d;
        gunTurnRemaining = 0d;
        radarTurnRemaining = 0d;

        bot.setTargetSpeed(0);
        bot.setTurnRate(0);
        bot.setGunTurnRate(0);
        bot.setRadarTurnRate(0);

        isStopped.set(true);
        isStopped.notifyAll();
      }
    }
  }

  void resume() {
    synchronized (isStopped) {
      if (isStopped.get()) {
        distanceRemaining = savedDistanceRemaining;
        turnRemaining = savedTurnRemaining;
        gunTurnRemaining = savedGunTurnRemaining;
        radarTurnRemaining = savedRadarTurnRemaining;

        isStopped.set(false);
        isStopped.notifyAll();
      }
    }
  }

  private boolean isNearZero(double value) {
    return (Math.abs(value) < .00001);
  }

  void waitIfStopped() {
    synchronized (isStopped) {
      while (isStopped.get()) {
        try {
          isStopped.wait();
        } catch (InterruptedException e) {
          return;
        }
      }
    }
  }

  void await() {
    synchronized (nextTurn) {
      while (isRunning && pendingCommands.entrySet().size() > 0) {
        // Fetch next pending command
        Iterator<Map.Entry<Class<?>, Command>> iterator = pendingCommands.entrySet().iterator();
        Map.Entry<Class<?>, Command> entry = iterator.next();
        Command cmd = entry.getValue();

        // Run the command, if it is not running already
        if (!cmd.isRunning()) {
          cmd.run();
          // Send the bot intend if the bot is now running
          if (cmd.isRunning()) {
            bot.go();
          }
        }
        // Loop while bot is running and command is not done yet
        while (isRunning && !cmd.isDone()) {
          try {
            // Wait for next turn and fire events
            nextTurn.wait();
            botEvents.fireEvents(currentTick);
          } catch (InterruptedException e) {
            isRunning = false;
          }
        }
        // Remove the command
        cmd.beforeDestroy();
        pendingCommands.remove(cmd);
      }
    }
  }

  void queueForward(double distance) {
    queueCommand(new MoveCommand(distance));
  }

  void queueTurn(double degrees) {
    queueCommand(new TurnCommand(degrees));
  }

  void queueGunTurn(double degrees) {
    queueCommand(new GunTurnCommand(degrees));
  }

  void queueRadarTurn(double degrees) {
    queueCommand(new RadarTurnCommand(degrees));
  }

  void queueFireGun(double firepower) {
    queueCommand(new FireGunCommand(firepower));
  }

  void queueCondition(Condition condition) {
    queueCommand(new ConditionCommand(condition));
  }

  void queueStop() {
    queueCommand(new StopCommand());
  }

  void queueResume() {
    queueCommand(new ResumeCommand());
  }

  void queueScan() {
    queueCommand(new ScanCommand());
  }

  void fireConditionMet(Condition condition) {
    botEvents.fireConditionMet(condition);
  }

  private void queueCommand(Command command) {
    pendingCommands.put(command.getClass(), command);
  }

  private abstract static class Command {
    boolean isRunning;

    boolean isRunning() {
      return isRunning;
    }

    abstract void run(); // must set isRunning

    abstract boolean isDone();

    void beforeDestroy() {}
  }

  private final class MoveCommand extends Command {
    final double distance;

    MoveCommand(double distance) {
      this.distance = distance;
    }

    @Override
    public void run() {
      bot.setForward(distance);
      isRunning = true;
    }

    @Override
    public boolean isDone() {
      return distanceRemaining == 0;
    }
  }

  private final class TurnCommand extends Command {
    final double degrees;

    TurnCommand(double degrees) {
      this.degrees = degrees;
    }

    @Override
    public void run() {
      bot.setTurnLeft(degrees);
      isRunning = true;
    }

    @Override
    public boolean isDone() {
      return turnRemaining == 0;
    }
  }

  private final class GunTurnCommand extends Command {
    final double degrees;

    GunTurnCommand(double degrees) {
      this.degrees = degrees;
    }

    @Override
    public void run() {
      bot.setTurnGunLeft(degrees);
      isRunning = true;
    }

    @Override
    public boolean isDone() {
      return gunTurnRemaining == 0;
    }
  }

  private final class RadarTurnCommand extends Command {
    final double degrees;

    RadarTurnCommand(double degrees) {
      this.degrees = degrees;
    }

    @Override
    public void run() {
      bot.setTurnRadarLeft(degrees);
      isRunning = true;
    }

    @Override
    public boolean isDone() {
      return radarTurnRemaining == 0;
    }
  }

  private final class FireGunCommand extends Command {
    final double firepower;

    FireGunCommand(double firepower) {
      this.firepower = firepower;
    }

    @Override
    public void run() {
      isRunning = bot.setFire(firepower);
    }

    @Override
    public boolean isDone() {
      return bot.getGunHeat() > 0;
    }
  }

  private static final class ConditionCommand extends Command {
    final Condition condition;

    ConditionCommand(Condition condition) {
      this.condition = condition;
    }

    @Override
    public void run() {
      isRunning = true;
    }

    @Override
    public boolean isDone() {
      return condition.test();
    }
  }

  private abstract class RunAndAwaitNextTurnCommand extends Command {

    private final int turnNumber;

    RunAndAwaitNextTurnCommand() {
      this.turnNumber = bot.getTurnNumber();
    }

    @Override
    public boolean isDone() {
      return bot.getTurnNumber() > turnNumber;
    }
  }

  private final class StopCommand extends RunAndAwaitNextTurnCommand {
    @Override
    public void run() {
      stop();
      isRunning = true;
    }
  }

  private final class ResumeCommand extends RunAndAwaitNextTurnCommand {
    @Override
    public void run() {
      resume();
      isRunning = true;
    }
  }

  private final class ScanCommand extends RunAndAwaitNextTurnCommand {
    @Override
    public void run() {
      bot.setScan(true);
      isRunning = true;
    }

    @Override
    public void beforeDestroy() {
      bot.setScan(false);
    }
  }
}
