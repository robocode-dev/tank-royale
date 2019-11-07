package dev.robocode.tankroyale.botapi;

public interface IBot extends IBasicBot {

  /**
   * Sets the bot to move forward until it has traveled a specific distance from its current
   * position or is moving into an obstacle. The speed is limited by {@link #setMaxSpeed(double)}.
   *
   * <p>This method will cancel the effect of calling {@link #setTargetSpeed(double)} as the
   * setForward and setBack methods calls the setTargetSpeed for each turn until {@link
   * #getDistanceRemaining()} reaches 0.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setBack(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   * @param distance is the distance to move forward. If negative, the bot will move backwards.
   */
  void setForward(double distance);

  /**
   * Sets the bot to move backward until it has traveled a specific distance from its current
   * position or is moving into an obstacle. The speed is limited by {@link #setMaxSpeed(double)}.
   *
   * <p>This method will cancel the effect of calling {@link #setTargetSpeed(double)} as the
   * setForward and setBack methods calls the setTargetSpeed for each turn until {@link
   * #getDistanceRemaining()} reaches 0.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setForward(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   * @param distance is the distance to move backward. If negative, the bot will move forward.
   */
  void setBack(double distance);

  /**
   * Returns the distance remaining till the bot has finished moving after having called {@link
   * #setForward(double)} or {@link #setBack(double)}. When the distance remaining has reached 0,
   * the bot has finished its move.
   *
   * <p>When the distance remaining is positive, the bot is moving forward. When the distance
   * remaining is negative, the bot is moving backwards.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setForward(double)
   * @see #setBack(double)
   * @return the remaining distance to move.
   */
  double getDistanceRemaining();

  /**
   * Sets the maximum speed which applies when moving forwards and backwards. The maximum speed must
   * be an absolute value from 0 to {@link #MAX_SPEED}, both values are included. If the input speed
   * is negative, the max speed will be cut to zero. If the input speed is above {@link #MAX_SPEED},
   * the max speed will be set to {@link #MAX_SPEED}.
   *
   * <p>If for example the maximum speed is set to 5, then the bot will be able to move backwards
   * with a speed down to -5 pixels/turn and up to 5 pixels/turn when moving forward.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setForward(double)
   * @see #setBack(double)
   * @param maxSpeed is the new maximum speed
   */
  void setMaxSpeed(double maxSpeed);

  /**
   * Sets the bot to turn left (following the increasing degrees of the unity circle) until it
   * turned the specified amount of degrees, i.e., when {@link #getTurnRemaining()} is 0. The amount
   * of degrees to turn each turn is limited by {@link #setTurnRate(double)}.
   *
   * <p>This method will cancel the effect of earlier called to setTurnLeft or setTurnRight.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnRight(double)
   * @see #getTurnRemaining()
   * @see #setTurnRate(double)
   * @param degrees is the amount of degrees to turn left. If negative, the bot will turn right.
   */
  void setTurnLeft(double degrees);

  /**
   * Sets the bot to turn right (following the decreasing degrees of the unity circle) until it
   * turned the specified amount of degrees, i.e., when {@link #getTurnRemaining()} is 0. The amount
   * of degrees to turn each turn is limited by {@link #setTurnRate(double)}.
   *
   * <p>This method will cancel the effect of earlier called to setTurnLeft or setTurnRight.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnLeft(double)
   * @see #getTurnRemaining()
   * @see #setTurnRate(double)
   * @param degrees is the amount of degrees to turn right. If negative, the bot will turn left.
   */
  void setTurnRight(double degrees);

  /**
   * Returns the remaining turn in degrees till the bot has finished turning after having called
   * {@link #setTurnLeft(double)} or {@link #setTurnRight(double)}. When the turn remaining has
   * reached 0, the bot has finished turning. If the
   *
   * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
   * When the turn remaining is negative, the bot is turning to the right.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setForward(double)
   * @see #setBack(double)
   * @return the remaining degrees to turn
   */
  double getTurnRemaining();

  /**
   * Sets the gun to turn left (following the increasing degrees of the unity circle) until it
   * turned the specified amount of degrees, i.e., when {@link #getGunTurnRemaining()} is 0. The
   * amount of degrees to turn each turn is limited by {@link #setGunTurnRate(double)}.
   *
   * <p>This method will cancel the effect of earlier called to setTurnGunLeft or setTurnGunRight.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnGunRight(double)
   * @see #getGunTurnRemaining()
   * @see #setGunTurnRate(double)
   * @param degrees is the amount of degrees to turn left. If negative, the gun will turn right.
   */
  void setTurnGunLeft(double degrees);

  /**
   * Sets the gun to turn right (following the decreasing degrees of the unity circle) until it
   * turned the specified amount of degrees, i.e., when {@link #getGunTurnRemaining()} is 0. The
   * amount of degrees to turn each turn is limited by {@link #setGunTurnRate(double)}.
   *
   * <p>This method will cancel the effect of earlier called to setTurnGunLeft or setTurnGunRight.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnGunLeft(double)
   * @see #getGunTurnRemaining()
   * @see #setGunTurnRate(double)
   * @param degrees is the amount of degrees to turn right. If negative, the gun will turn left.
   */
  void setTurnGunRight(double degrees);

  /**
   * Returns the remaining turn in degrees till the gun has finished turning after having called
   * {@link #setTurnGunLeft(double)} or {@link #setTurnGunRight(double)}. When the turn remaining
   * has reached 0, the gun has finished turning.
   *
   * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
   * When the turn remaining is negative, the bot is turning to the right.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnGunLeft(double)
   * @see #setTurnGunRight(double)
   * @return the remaining degrees to turn the gun
   */
  double getGunTurnRemaining();

  /**
   * Sets the radar to turn left (following the increasing degrees of the unity circle) until it
   * turned the specified amount of degrees, i.e., when {@link #getRadarTurnRemaining()} is 0. The
   * amount of degrees to turn each turn is limited by {@link #setRadarTurnRate(double)}.
   *
   * <p>This method will cancel the effect of earlier called to setTurnRadarLeft or
   * setTurnRadarRight.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnRadarRight(double)
   * @see #getRadarTurnRemaining()
   * @see #setRadarTurnRate(double)
   * @param degrees is the amount of degrees to turn left. If negative, the radar will turn right.
   */
  void setTurnRadarLeft(double degrees);

  /**
   * Sets the radar to turn right (following the decreasing degrees of the unity circle) until it
   * turned the specified amount of degrees, i.e., when {@link #getRadarTurnRemaining()} is 0. The
   * amount of degrees to turn each turn is limited by {@link #setRadarTurnRate(double)}.
   *
   * <p>This method will cancel the effect of earlier called to setTurnRadarLeft or
   * setTurnRadarRight.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnRadarLeft(double)
   * @see #getRadarTurnRemaining()
   * @see #setRadarTurnRate(double)
   * @param degrees is the amount of degrees to turn right. If negative, the radar will turn left.
   */
  void setTurnRadarRight(double degrees);

  /**
   * Returns the remaining turn in degrees till the radar has finished turning after having called
   * {@link #setTurnRadarLeft(double)} or {@link #setTurnRadarRight(double)}. When the turn
   * remaining has reached 0, the radar has finished turning.
   *
   * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
   * When the turn remaining is negative, the bot is turning to the right.
   *
   * <p>If this method is called multiple times, the last call before go() is executed counts.
   *
   * @see #setTurnGunLeft(double)
   * @see #setTurnGunRight(double)
   * @return the remaining degrees to turn the gun
   */
  double getRadarTurnRemaining();
}
