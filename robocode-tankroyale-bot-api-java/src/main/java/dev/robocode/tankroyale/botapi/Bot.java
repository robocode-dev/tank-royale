package dev.robocode.tankroyale.botapi;

import lombok.val;

import java.net.URI;
import static java.lang.Math.abs;

public abstract class Bot extends BasicBot implements IBot {

  private static final double ABS_DECELERATION = Math.abs(DECELERATION);

  private final __Internals __internals = new __Internals();

  public Bot() {
    super();
  }

  public Bot(final BotInfo botInfo) {
    super(botInfo);
  }

  public Bot(final BotInfo botInfo, URI serverUri) {
    super(botInfo, serverUri);
  }

  @Override
  public final void setForward(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    __internals.distanceRemaining = distance;
  }

  @Override
  public final void setBack(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    __internals.distanceRemaining = -distance;
  }

  @Override
  public final double getDistanceRemaining() {
    return __internals.distanceRemaining;
  }

  @Override
  public final void setMaxSpeed(double maxSpeed) {
    if (maxSpeed < 0) {
      maxSpeed = 0;
    } else if (maxSpeed > MAX_SPEED) {
      maxSpeed = MAX_SPEED;
    }
    __internals.maxSpeed = maxSpeed;
  }

  @Override
  public final void setTurnLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.turnRemaining = degrees;
  }

  @Override
  public final void setTurnRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.turnRemaining = -degrees;
  }

  @Override
  public final double getTurnRemaining() {
    return __internals.turnRemaining;
  }

  @Override
  public final void setMaxTurnRate(double maxTurnRate) {
    if (maxTurnRate < 0) {
      maxTurnRate = 0;
    } else if (maxTurnRate > MAX_TURN_RATE) {
      maxTurnRate = MAX_TURN_RATE;
    }
    __internals.maxTurnRate = maxTurnRate;
  }

  @Override
  public final void setTurnGunLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.gunTurnRemaining = degrees;
  }

  @Override
  public final void setTurnGunRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.gunTurnRemaining = -degrees;
  }

  @Override
  public final double getGunTurnRemaining() {
    return __internals.gunTurnRemaining;
  }

  @Override
  public final void setMaxGunTurnRate(double maxGunTurnRate) {
    if (maxGunTurnRate < 0) {
      maxGunTurnRate = 0;
    } else if (maxGunTurnRate > MAX_GUN_TURN_RATE) {
      maxGunTurnRate = MAX_GUN_TURN_RATE;
    }
    __internals.maxGunTurnRate = maxGunTurnRate;
  }

  @Override
  public final void setTurnRadarLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.radarTurnRemaining = degrees;
  }

  @Override
  public final void setTurnRadarRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.radarTurnRemaining = -degrees;
  }

  @Override
  public final double getRadarTurnRemaining() {
    return __internals.radarTurnRemaining;
  }

  @Override
  public final void setMaxRadarTurnRate(double maxRadarTurnRate) {
    if (maxRadarTurnRate < 0) {
      maxRadarTurnRate = 0;
    } else if (maxRadarTurnRate > MAX_RADAR_TURN_RATE) {
      maxRadarTurnRate = MAX_RADAR_TURN_RATE;
    }
    __internals.maxRadarTurnRate = maxRadarTurnRate;
  }

  private final class __Internals {
    private double maxSpeed = MAX_FORWARD_SPEED;
    private double maxTurnRate = MAX_TURN_RATE;
    private double maxGunTurnRate = MAX_GUN_TURN_RATE;
    private double maxRadarTurnRate = MAX_RADAR_TURN_RATE;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private boolean isCollidingWithBot;
    private boolean isOverDriving;

    private __Internals() {
      val superInt = Bot.super.__internals;

      superInt.onTick.subscribe(
          event -> {
            onTick();
            return null;
          },
          50);
      superInt.onSkippedTurn.subscribe(
          event -> {
            onSkippedTurn();
            return null;
          },
          50);
      superInt.onHitBot.subscribe(
          event -> {
            onHitBot(event.isRammed());
            return null;
          },
          50);
      superInt.onHitWall.subscribe(
          event -> {
            onHitWall();
            return null;
          },
          50);
    }

    private void onTick() {
      processTurn();
    }

    private void onSkippedTurn() {
      processTurn();
    }

    private void onHitBot(boolean isRamming) {
      if (isRamming) {
        __internals.distanceRemaining = 0;
      }
      isCollidingWithBot = true;
    }

    private void onHitWall() {
      __internals.distanceRemaining = 0;
    }

    private void processTurn() {
      // No movement is possible, when the bot has become disabled
      if (isDisabled()) {
        __internals.distanceRemaining = 0;
        __internals.turnRemaining = 0;
      }
      updateHeadings();
      updateMovement();
      __internals.isCollidingWithBot = false;
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
          __internals.gunTurnRemaining -= getTurnRemaining();
        }
        __internals.turnRemaining = 0;
      } else {
        if (isAdjustGunForBodyTurn()) {
          __internals.gunTurnRemaining -= turnRate;
        }
        __internals.turnRemaining -= turnRate;
      }
      if (__internals.turnRemaining > 0) {
        setTurnRate(Math.min(__internals.maxTurnRate, __internals.turnRemaining));
      } else {
        setTurnRate(Math.max(-__internals.maxTurnRate, __internals.turnRemaining));
      }
    }

    private void updateGunTurnRemaining() {
      System.out.println(
              "gunTurnRemaining: " + getGunTurnRemaining() + ", gunTurnRate: " + getGunTurnRate() + ", gunTurnRateLimit: " + __internals.maxGunTurnRate);
      final double absGunTurnRate = abs(getGunTurnRate());

      if (abs(getGunTurnRemaining()) < absGunTurnRate) {
        if (isAdjustRadarForGunTurn()) {
          __internals.radarTurnRemaining -= getGunTurnRemaining();
        }
        __internals.gunTurnRemaining = 0;
      } else {
        if (isAdjustRadarForGunTurn()) {
          __internals.radarTurnRemaining -= getGunTurnRate();
        }
        __internals.gunTurnRemaining -= getGunTurnRate();
      }
      if (__internals.gunTurnRemaining > 0) {
        setGunTurnRate(Math.min(__internals.maxGunTurnRate, __internals.gunTurnRemaining));
      } else {
        setGunTurnRate(Math.max(-__internals.maxGunTurnRate, __internals.gunTurnRemaining));
      }
    }

    private void updateRadarTurnRemaining() {
      System.out.println(
              "radarTurnRemaining: " + getRadarTurnRemaining() + ", radarTurnRate: " + getRadarTurnRate() + ", radarTurnRateLimit: " + __internals.maxRadarTurnRate);
      final double absRadarTurnRate = abs(getRadarTurnRate());

      if (abs(getRadarTurnRemaining()) < absRadarTurnRate) {
        __internals.radarTurnRemaining = 0;
      } else {
        __internals.radarTurnRemaining -= getRadarTurnRate();
      }
      if (__internals.radarTurnRemaining > 0) {
        setRadarTurnRate(Math.min(__internals.maxRadarTurnRate, __internals.radarTurnRemaining));
      } else {
        setRadarTurnRate(Math.max(-__internals.maxRadarTurnRate, __internals.radarTurnRemaining));
      }
    }

    /**
     * Updates the movement.
     *
     * <p>This is Nat Pavasants method described here:
     * http://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
     */
    private void updateMovement() {
      double distance = distanceRemaining;
      if (Double.isNaN(distance)) {
        distance = 0;
      }

      val speed = getNewSpeed(getSpeed(), distance);
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
     * @return the new speed
     *     <p>Credits for this algorithm goes to Patrick Cupka (aka Voidious), Julian Kent (aka
     *     Skilgannon), and Positive:
     *     http://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
     */
    private double getNewSpeed(double speed, double distance) {
      if (distance < 0) {
        // If the distance is negative, then change it to be positive and change the sign of the
        // input
        // velocity and the result
        return -getNewSpeed(-speed, -distance);
      }

      final double targetSpeed;
      if (distance == Double.POSITIVE_INFINITY) {
        targetSpeed = __internals.maxSpeed;
      } else {
        targetSpeed = Math.min(getMaxSpeed(distance), __internals.maxSpeed);
      }

      if (speed >= 0) {
        return Math.max(speed - ABS_DECELERATION, Math.min(targetSpeed, speed + ACCELERATION));
      } // else
      return Math.max(speed - ACCELERATION, Math.min(targetSpeed, speed + maxDeceleration(-speed)));
    }

    private double getMaxSpeed(double distance) {
      val decelTime =
          Math.max(
              1,
              Math.ceil( // sum of 0... decelTime, solving for decelTime using quadratic formula
                  (Math.sqrt((4 * 2 / ABS_DECELERATION) * distance + 1) - 1) / 2));

      if (decelTime == Double.POSITIVE_INFINITY) {
        return MAX_SPEED;
      }

      val decelDist =
          (decelTime / 2)
              * (decelTime - 1) // sum of 0..(decelTime-1)
              * ABS_DECELERATION;

      return ((decelTime - 1) * ABS_DECELERATION) + ((distance - decelDist) / decelTime);
    }

    private double maxDeceleration(double speed) {
      val decelTime = speed / ABS_DECELERATION;
      val accelTime = (1 - decelTime);

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
  }
}
