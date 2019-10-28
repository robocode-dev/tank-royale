package dev.robocode.tankroyale.botapi;

public interface IBot extends IBasicBot {

  /**
   * Sets the bot to move forward until it has traveled a specific distance from its current position
   * or is moving into an obstacle.
   *
   * @param distance is the distance to move forward.
   */
  void setForward(double distance);

  /**
   * Sets the bot to move backward until it has traveled a specific distance from its current
   * position or is moving into an obstacle.
   *
   * @param distance is the distance to move backward.
   */
  void setBack(double distance);

  /**
   * Returns the distance remaining till the bot has finished moving after having called {@link
   * #setForward(double)} or {@link #setBack(double)}. If the distance remaining is 0, the bot has
   * finished its move.
   *
   * @return the remaining distance to move.
   */
  double getDistanceRemaining();

  /**
   * Sets the maximum speed which applies when moving forwards and backwards.
   * The maximum speed must be a value from 0 to {@link #MAX_SPEED}, both values are included.
   * If the input speed is negative, the max speed will be set to zero.
   * If the input speed is above {@link #MAX_SPEED}, the max speed will be set to {@link #MAX_SPEED}
   *
   * @param maxSpeed is the new maximum speed
   */
  void setMaxSpeed(double maxSpeed);
}
