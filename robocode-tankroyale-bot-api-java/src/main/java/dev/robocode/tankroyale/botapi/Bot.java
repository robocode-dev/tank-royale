package dev.robocode.tankroyale.botapi;

import java.net.URI;

import static java.lang.Math.abs;

/**
 * Abstract bot class provides convenient methods for movement, turning, and firing the gun. Most
 * bots should inherit from this class.
 */
@SuppressWarnings("unused")
public abstract class Bot extends BaseBot implements IBot {

  private final __Internals __internals = new __Internals();

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * both BotInfo and server URL is provided through environment variables, i.e., when starting up
   * the bot using a bootstrap. These environment variables must be set to provide the server URL
   * and bot information, and are automatically set by the bootstrap tool for Robocode.
   *
   * <p><b>Example of how to set the predefined environment variables:</b>
   *
   * <p>ROBOCODE_SERVER_URL=ws://localhost<br>
   * BOT_NAME=MyBot<br>
   * BOT_VERSION=1.0<br>
   * BOT_AUTHOR=fnl<br>
   * BOT_DESCRIPTION=Sample bot<br>
   * BOT_URL=https://mybot.somewhere.net
   * BOT_COUNTRY_CODE=DK<br>
   * BOT_GAME_TYPES=melee,1v1<br>
   * BOT_PLATFORM=Java<br>
   * BOT_PROG_LANG=Java 8<br>
   */
  public Bot() {
    super();
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * server URL is provided through the environment variable ROBOCODE_SERVER_URL.
   *
   * @param botInfo is the bot info containing information about your bot.
   */
  public Bot(final BotInfo botInfo) {
    super(botInfo);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used
   * providing both the bot information and server URL for your bot.
   *
   * @param botInfo is the bot info containing information about your bot.
   * @param serverUrl is the server URL
   */
  public Bot(final BotInfo botInfo, URI serverUrl) {
    super(botInfo, serverUrl);
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isRunning() {
    return __internals.isRunning;
  }

  /** {@inheritDoc} */
  @Override
  public final void setForward(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    __internals.distanceRemaining = distance;
  }

  /** {@inheritDoc} */
  @Override
  public final void forward(double distance) {
    setForward(distance);
    go();
    __internals.awaitMovementComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBack(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    __internals.distanceRemaining = -distance;
  }

  /** {@inheritDoc} */
  @Override
  public final void back(double distance) {
    setBack(distance);
    go();
    __internals.awaitMovementComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getDistanceRemaining() {
    return __internals.distanceRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxSpeed(double maxSpeed) {
    if (maxSpeed < 0) {
      maxSpeed = 0;
    } else if (maxSpeed > MAX_SPEED) {
      maxSpeed = MAX_SPEED;
    }
    __internals.maxSpeed = maxSpeed;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.turnRemaining = degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnLeft(double degrees) {
    setTurnLeft(degrees);
    go();
    __internals.awaitTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.turnRemaining = -degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnRight(double degrees) {
    setTurnRight(degrees);
    go();
    __internals.awaitTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getTurnRemaining() {
    return __internals.turnRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxTurnRate(double maxTurnRate) {
    if (maxTurnRate < 0) {
      maxTurnRate = 0;
    } else if (maxTurnRate > MAX_TURN_RATE) {
      maxTurnRate = MAX_TURN_RATE;
    }
    __internals.maxTurnRate = maxTurnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnGunLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.gunTurnRemaining = degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnGunLeft(double degrees) {
    setTurnGunLeft(degrees);
    go();
    __internals.awaitGunTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnGunRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.gunTurnRemaining = -degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnGunRight(double degrees) {
    setTurnGunRight(degrees);
    go();
    __internals.awaitGunTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunTurnRemaining() {
    return __internals.gunTurnRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxGunTurnRate(double maxGunTurnRate) {
    if (maxGunTurnRate < 0) {
      maxGunTurnRate = 0;
    } else if (maxGunTurnRate > MAX_GUN_TURN_RATE) {
      maxGunTurnRate = MAX_GUN_TURN_RATE;
    }
    __internals.maxGunTurnRate = maxGunTurnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRadarLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.radarTurnRemaining = degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnRadarLeft(double degrees) {
    setTurnRadarLeft(degrees);
    go();
    __internals.awaitRadarTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRadarRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.radarTurnRemaining = -degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnRadarRight(double degrees) {
    setTurnRadarRight(degrees);
    go();
    __internals.awaitRadarTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarTurnRemaining() {
    return __internals.radarTurnRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxRadarTurnRate(double maxRadarTurnRate) {
    if (maxRadarTurnRate < 0) {
      maxRadarTurnRate = 0;
    } else if (maxRadarTurnRate > MAX_RADAR_TURN_RATE) {
      maxRadarTurnRate = MAX_RADAR_TURN_RATE;
    }
    __internals.maxRadarTurnRate = maxRadarTurnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void fire(double firepower) {
    setFirepower(firepower);
    go();
  }

  private final class __Internals {
    private final double ABS_DECELERATION = Math.abs(DECELERATION);

    private double maxSpeed = MAX_SPEED;
    private double maxTurnRate = MAX_TURN_RATE;
    private double maxGunTurnRate = MAX_GUN_TURN_RATE;
    private double maxRadarTurnRate = MAX_RADAR_TURN_RATE;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private boolean isCollidingWithBot;
    private boolean isOverDriving;

    private int turnNumber;

    private Thread thread;
    private final Object nextTurn = new Object();
    private volatile boolean isRunning;

    private __Internals() {
      BaseBot.__Internals internals = Bot.super.__internals;

      internals.onDisconnected.subscribe(event -> stopThread());
      internals.onGameEnded.subscribe(event -> stopThread());
      internals.onSkippedTurn.subscribe(event -> onSkippedTurn());
      internals.onHitBot.subscribe(event -> onHitBot(event.isRammed()));
      internals.onHitWall.subscribe(event -> onHitWall());
      internals.onTick.subscribe(
          event -> {
            turnNumber = event.getTurnNumber();
            onTick();
          });
      internals.onBotDeath.subscribe(
          event -> {
            if (event.getVictimId() == getMyId()) {
              stopThread();
            }
          });
    }

    private void onTick() {
      processTurn();
    }

    private void onSkippedTurn() {
      processTurn();
    }

    private void onHitBot(boolean isRammed) {
      if (isRammed) {
        distanceRemaining = 0;
      }
      isCollidingWithBot = true;
    }

    private void onHitWall() {
      distanceRemaining = 0;
    }

    private void processTurn() {
      // No movement is possible, when the bot has become disabled
      if (isDisabled()) {
        distanceRemaining = 0;
        turnRemaining = 0;
      }
      updateHeadings();
      updateMovement();
      isCollidingWithBot = false;

      // If this is the first turn -> Call the run method on the Bot class
      if (turnNumber == 1) {
        stopThread();
        startThread();
      }

      // Unblock waiting methods
      synchronized (nextTurn) {
        // Let's go ;-)
        go();

        // Unblock waiting methods waiting for the next turn
        nextTurn.notifyAll();
      }
    }

    private void startThread() {
      thread = new Thread(Bot.this::run);
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
      if (!isCollidingWithBot) {
        updateTurnRemaining();
      }
      updateGunTurnRemaining();
      updateRadarTurnRemaining();
    }

    private void updateTurnRemaining() {
      final double absTurnRate = abs(getTurnRate());

      double turnRate = Math.min(absTurnRate, calcMaxTurnRate(getSpeed()));
      if (getTurnRemaining() < 0) {
        turnRate *= -1;
      }
      if (abs(getTurnRemaining()) < absTurnRate) {
        if (isAdjustGunForBodyTurn()) {
          gunTurnRemaining -= getTurnRemaining();
        }
        turnRemaining = 0;
      } else {
        if (isAdjustGunForBodyTurn()) {
          gunTurnRemaining -= turnRate;
        }
        turnRemaining -= turnRate;
      }
      if (turnRemaining > 0) {
        setTurnRate(Math.min(maxTurnRate, turnRemaining));
      } else {
        setTurnRate(Math.max(-maxTurnRate, turnRemaining));
      }
    }

    private void updateGunTurnRemaining() {
      final double absGunTurnRate = abs(getGunTurnRate());

      if (abs(getGunTurnRemaining()) < absGunTurnRate) {
        if (isAdjustRadarForGunTurn()) {
          radarTurnRemaining -= getGunTurnRemaining();
        }
        gunTurnRemaining = 0;
      } else {
        if (isAdjustRadarForGunTurn()) {
          radarTurnRemaining -= getGunTurnRate();
        }
        gunTurnRemaining -= getGunTurnRate();
      }
      if (gunTurnRemaining > 0) {
        setGunTurnRate(Math.min(maxGunTurnRate, gunTurnRemaining));
      } else {
        setGunTurnRate(Math.max(-maxGunTurnRate, gunTurnRemaining));
      }
    }

    private void updateRadarTurnRemaining() {
      final double absRadarTurnRate = abs(getRadarTurnRate());

      if (abs(getRadarTurnRemaining()) < absRadarTurnRate) {
        radarTurnRemaining = 0;
      } else {
        radarTurnRemaining -= getRadarTurnRate();
      }
      if (radarTurnRemaining > 0) {
        setRadarTurnRate(Math.min(maxRadarTurnRate, radarTurnRemaining));
      } else {
        setRadarTurnRate(Math.max(-maxRadarTurnRate, radarTurnRemaining));
      }
    }

    /** Updates the movement. */
    // This is Nat Pavasants method described here:
    // http://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
    private void updateMovement() {
      double distance = distanceRemaining;
      if (Double.isNaN(distance)) {
        distance = 0;
      }

      double speed = getNewSpeed(getSpeed(), distance);
      setTargetSpeed(speed);

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
    private double getNewSpeed(double speed, double distance) {

      if (distance < 0) {
        // If the distance is negative, then change it to be positive and change the sign of the
        // input velocity and the result
        return -getNewSpeed(-speed, -distance);
      }

      final double targetSpeed;
      if (distance == Double.POSITIVE_INFINITY) {
        targetSpeed = maxSpeed;
      } else {
        targetSpeed = Math.min(getMaxSpeed(distance), maxSpeed);
      }

      if (speed >= 0) {
        return Math.max(speed - ABS_DECELERATION, Math.min(targetSpeed, speed + ACCELERATION));
      } // else
      return Math.max(
          speed - ACCELERATION, Math.min(targetSpeed, speed + getMaxDeceleration(-speed)));
    }

    private double getMaxSpeed(double distance) {
      double decelTime =
          Math.max(
              1,
              Math.ceil( // sum of 0... decelTime, solving for decelTime using quadratic formula
                  (Math.sqrt((4 * 2 / ABS_DECELERATION) * distance + 1) - 1) / 2));

      if (decelTime == Double.POSITIVE_INFINITY) {
        return Bot.this.MAX_SPEED;
      }

      double decelDist =
          (decelTime / 2)
              * (decelTime - 1) // sum of 0..(decelTime-1)
              * ABS_DECELERATION;

      return ((decelTime - 1) * ABS_DECELERATION) + ((distance - decelDist) / decelTime);
    }

    private double getMaxDeceleration(double speed) {
      double decelTime = speed / ABS_DECELERATION;
      double accelTime = (1 - decelTime);

      return Math.min(1, decelTime) * ABS_DECELERATION + Math.max(0, accelTime) * ACCELERATION;
    }

    private double getDistanceTraveledUntilStop(double speed) {
      speed = Math.abs(speed);
      double distance = 0;
      while (speed > 0) {
        distance += (speed = getNewSpeed(speed, 0));
      }
      return distance;
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

    private void await(ICondition condition) {
      synchronized (nextTurn) {
        // Loop while bot is running and condition has not been met
        while (isRunning && !condition.test()) {
          try {
            // Wait for next turn
            nextTurn.wait();
          } catch (InterruptedException e) {
            isRunning = false;
          }
        }
      }
    }
  }

  private interface ICondition {
    boolean test();
  }
}
