package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.BotHitBotEvent;
import dev.robocode.tankroyale.botapi.events.BotHitWallEvent;
import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import dev.robocode.tankroyale.botapi.events.TickEvent;
import lombok.val;

import java.net.URI;

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

  private final class __Internals {
    private double maxSpeed = MAX_FORWARD_SPEED;

    private double distanceRemaining;
    private double turnRemaining;

    private boolean isOverDriving;

    private __Internals() {
      val superInt = Bot.super.__internals;

      superInt.onTick.subscribe(
          event -> {
            onTick(event);
            return null;
          });
      superInt.onSkippedTurn.subscribe(
          event -> {
            onSkippedTurn(event);
            return null;
          });
      superInt.onHitBot.subscribe(
          event -> {
            onHitBot(event);
            return null;
          });
      superInt.onHitWall.subscribe(
          event -> {
            onHitWall(event);
            return null;
          });
    }

    private void onTick(TickEvent tick) {
      processTurn();
    }

    private void onSkippedTurn(SkippedTurnEvent skippedTurn) {
      processTurn();
    }

    private void onHitBot(BotHitBotEvent event) {
      if (event.isRammed()) {
        resetRemainingDistanceAndTurn();
      }
    }

    private void onHitWall(BotHitWallEvent event) {
      resetRemainingDistanceAndTurn();
    }

    private void processTurn() {
      // No movement is possible, when the bot has become disabled
      if (isDisabled()) {
        resetRemainingDistanceAndTurn();
      } else {
        updateMovement();
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
      if (isNear(speed, 0) && isOverDriving) {
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

    private void resetRemainingDistanceAndTurn() {
      distanceRemaining = 0;
      turnRemaining = 0;
    }

    private boolean isNear(double value1, double value2) {
      return (Math.abs(value1 - value2) < .00001);
    }
  }
}
