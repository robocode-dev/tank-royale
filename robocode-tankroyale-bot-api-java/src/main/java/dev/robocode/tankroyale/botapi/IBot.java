package dev.robocode.tankroyale.botapi;

public interface IBot extends IBasicBot {

  /**
   * Sets the bot to move forward until it has traveled a specific distance from its current
   * position or is moving into an obstacle.
   *
   * <p>This method will cancel the effect of calling {@link #setTargetSpeed(double)}
   * as the setForward and setBack methods calls the setTargetSpeed for each turn until {@link
   * #getDistanceRemaining()} reaches 0.
   *
   * @see #setBack(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   * @param distance is the distance to move forward. If negative, the bot will move backwards.
   */
  void setForward(double distance);

  /**
   * Sets the bot to move backward until it has traveled a specific distance from its current
   * position or is moving into an obstacle.
   *
   * <p>This method will cancel the effect of calling {@link #setTargetSpeed(double)}
   * as the setForward and setBack methods calls the setTargetSpeed for each turn until {@link
   * #getDistanceRemaining()} reaches 0.
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
   * @see #setForward(double)
   * @see #setBack(double)
   * @return the remaining distance to move.
   */
  double getDistanceRemaining();

  /**
   * Sets the maximum speed which applies when moving forwards and backwards. The maximum speed must
   * be a value from 0 to {@link #MAX_SPEED}, both values are included. If the input speed is
   * negative, the max speed will be set to zero. If the input speed is above {@link #MAX_SPEED},
   * the max speed will be set to {@link #MAX_SPEED}
   *
   * @see #setForward(double)
   * @see #setBack(double) 
   * @param maxSpeed is the new maximum speed
   */
  void setMaxSpeed(double maxSpeed);
}
