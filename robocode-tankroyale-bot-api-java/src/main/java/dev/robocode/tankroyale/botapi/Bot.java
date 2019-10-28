package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.BotHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BotHitWallEvent;
import lombok.val;

public abstract class Bot extends BasicBot implements IBot {

  private final __Internals __internals = new __Internals();

  @Override
  public final void setForward(double distance) {
    if (Double.isNaN(distance)) {
      distance = 0.0;
    }
    __internals.distanceRemaining = distance;
  }

  @Override
  public final void setBack(double distance) {
    if (Double.isNaN(distance)) {
      distance = 0.0;
    }
    setForward(-distance);
  }

  @Override
  public final double getDistanceRemaining() {
    return __internals.distanceRemaining;
  }

  @Override
  public final void setMaxSpeed(double maxSpeed) {
    if (Double.isNaN(maxSpeed)) {
      maxSpeed = MAX_SPEED;
    } else if (maxSpeed < 0) {
      maxSpeed = 0;
    } else if (maxSpeed > MAX_SPEED) {
      maxSpeed = MAX_SPEED;
    }
    __internals.maxSpeed = maxSpeed;
  }

  @Override
  public final void go() {
    updateDistanceRemaining();

    // updateMovement() // TODO

    super.go();
  }

  private void updateDistanceRemaining() {
    // Set the remaining distance to 0 if the bot is disabled, has rammed another another bot, or has hit a wall
    if (isDisabled()
        || getEvents().stream()
            .anyMatch(
                event ->
                    (event instanceof BotHitBotEvent && ((BotHitBotEvent) event).isRammed())
                        || (event instanceof BotHitWallEvent))) {
      __internals.distanceRemaining = 0.0;
    } else {
      // TODO
    }
  }

  /**
   * Returns the new speed based on the current speed and distance to move.
   *
   * @param speed is the current speed
   * @param distance is the distance to move
   * @return the new speed
   *     <p>Credits for this algorithm goes to Patrick Cupka (aka Voidious), Julian Kent (aka
   *     Skilgannon), and Positive: http://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
   */
  private double getNewSpeed(double speed, double distance) {
    if (distance < 0) {
      // If the distance is negative, then change it to be positive and change the sign of the input
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
      return Math.max(speed - DECELERATION, Math.min(targetSpeed, speed + ACCELERATION));
    } // else
    return Math.max(speed - ACCELERATION, Math.min(targetSpeed, speed + maxDeceleration(-speed)));
  }

  private static double getMaxSpeed(double distance) {
    val decelTime =
        Math.max(
            1,
            Math.ceil( // sum of 0... decelTime, solving for decelTime using quadratic formula
                (Math.sqrt((4 * 2 / DECELERATION) * distance + 1) - 1) / 2));

    if (decelTime == Double.POSITIVE_INFINITY) {
      return MAX_SPEED;
    }

    val decelDist =
        (decelTime / 2.0)
            * (decelTime - 1) // sum of 0..(decelTime-1)
            * DECELERATION;

    return ((decelTime - 1) * DECELERATION) + ((distance - decelDist) / decelTime);
  }

  private static double maxDeceleration(double speed) {
    val decelTime = speed / DECELERATION;
    val accelTime = (1 - decelTime);

    return Math.min(1, decelTime) * DECELERATION + Math.max(0, accelTime) * ACCELERATION;
  }

  private static final class __Internals {
    double distanceRemaining;
    double maxSpeed = MAX_FORWARD_SPEED;
  }
}
