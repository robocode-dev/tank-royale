package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.BulletFiredEvent;

/**
 * Interface for a bot that extends the core API with convenient methods for movement, turning, and
 * firing the gun.
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface IBot extends IBaseBot {

  /**
   * The run() method is used for running a program for the bot like:
   *
   * <pre>
   * public void run() {
   *   while (isRunning()) {
   *     forward(100);
   *     turnGunLeft(360);
   *     back(100);
   *     turnGunRight(360);
   *   }
   * }
   * </pre>
   *
   * <p>Note that the program runs in a loop in this example, meaning that it will start moving
   * forward as soon as {@link #turnGunRight(double)} has executed.
   *
   * <p>When running a loop that could potentially run forever. The best practice is to check if the
   * bot is still running to stop and exit the loop. This gives the game a chance of stopping the
   * thread running the loop in the code behind. If the thread is not stopped correctly, the bot may
   * behave strangely in new rounds.
   *
   * @see #isRunning()
   */
  default void run() {}

  /**
   * Checks if this bot is running.
   *
   * @return {@code true} when the bot is running, {@code false} otherwise.
   */
  boolean isRunning();

  /**
   * Set the bot to move forward until it has traveled a specific distance from its current
   * position, or it is moving into an obstacle. The speed is limited by {@link
   * #setMaxSpeed(double)}.
   *
   * <p>When the bot is moving forward, the {@link #ACCELERATION} determines the acceleration of the
   * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
   * faster at braking. The {@link #DECELERATION} determines the deceleration of the bot that
   * subtracts 2 units from the speed per turn.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods before execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed(double)} as the
   * setForward(double) and {@link #setBack(double)} methods calls the {@link
   * #setTargetSpeed(double)} for each turn until {@link #getDistanceRemaining()} reaches 0.
   *
   * @param distance is the distance to move forward. If negative, the bot will move backward.
   * @see #forward(double)
   * @see #setBack(double)
   * @see #back(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   */
  void setForward(double distance);

  /**
   * Moves the bot forward until it has traveled a specific distance from its current position, or
   * it is moving into an obstacle. The speed is limited by {@link #setMaxSpeed(double)}.
   *
   * <p>When the bot is moving forward, the {@link #ACCELERATION} determine the acceleration of the
   * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
   * faster at braking. The {@link #DECELERATION} determines the deceleration of the bot that
   * subtracts 2 units from the speed per turn.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed(double)}, {@link
   * #setForward(double)}, and {@link #setBack(double)} methods.
   *
   * @param distance is the distance to move forward. If negative, the bot will move backward.
   * @see #setForward(double)
   * @see #setBack(double)
   * @see #back(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   */
  void forward(double distance);

  /**
   * Set the bot to move backward until it has traveled a specific distance from its current
   * position, or it is moving into an obstacle. The speed is limited by {@link
   * #setMaxSpeed(double)}.
   *
   * <p>When the bot is moving forward, the {@link #ACCELERATION} determines the acceleration of the
   * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
   * faster at braking. The {@link #DECELERATION} determines the deceleration of the bot that
   * subtracts 2 units from the speed per turn.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed(double)} as the
   * {@link #setForward(double)} and setBack(double) methods calls the {@link
   * #setTargetSpeed(double)} for each turn until {@link #getDistanceRemaining()} reaches 0.
   *
   * @param distance is the distance to move backward. If negative, the bot will move forward.
   * @see #back(double)
   * @see #setForward(double)
   * @see #forward(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   */
  void setBack(double distance);

  /**
   * Moves the bot backward until it has traveled a specific distance from its current position, or
   * it is moving into an obstacle. The speed is limited by {@link #setMaxSpeed(double)}.
   *
   * <p>When the bot is moving forward, the {@link #ACCELERATION} determine the acceleration of the
   * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
   * faster at braking. The {@link #DECELERATION} determine the deceleration of the bot that
   * subtracts 2 units from the speed per turn.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed(double)}, {@link
   * #setForward(double)}, and {@link #setBack(double)} methods.
   *
   * @param distance is the distance to move backward. If negative, the bot will move forward.
   * @see #setForward(double)
   * @see #setBack(double)
   * @see #forward(double)
   * @see #getDistanceRemaining()
   * @see #setTargetSpeed(double)
   */
  void back(double distance);

  /**
   * Returns the distance remaining till the bot has finished moving after having called {@link
   * #setForward(double)}, {@link #setBack(double)}, {@link #forward(double)}, or {@link
   * #back(double)}. When the distance remaining has reached 0, the bot has finished its current
   * move.
   *
   * <p>When the distance remaining is positive, the bot is moving forward. When the distance
   * remaining is negative, the bot is moving backward.
   *
   * @return The remaining distance to move before its current movement is completed.
   * @see #setForward(double)
   * @see #setBack(double)
   * @see #forward(double)
   * @see #back(double)
   */
  double getDistanceRemaining();

  /**
   * Sets the maximum speed which applies when moving forward and backward. The maximum speed must
   * be an absolute value from 0 to {@link #MAX_SPEED}, both values are included. If the input speed
   * is negative, the max speed will be cut to zero. If the input speed is above {@link #MAX_SPEED},
   * the max speed will be set to {@link #MAX_SPEED}.
   *
   * <p>If for example the maximum speed is set to 5, then the bot will be able to move backwards
   * with a speed down to -5 units per turn and up to 5 units per turn when moving forward.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * @param maxSpeed is the new maximum speed
   * @see #setForward(double)
   * @see #setBack(double)
   */
  void setMaxSpeed(double maxSpeed);

  /**
   * Set the bot to turn to the left (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxTurnRate(double)}.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnRight(double)}.
   *
   * @param degrees is the amount of degrees to turn left. If negative, the bot will turn right.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnRight(double)
   * @see #turnRight(double)
   * @see #turnLeft(double)
   * @see #getTurnRemaining()
   * @see #setTurnRate(double)
   */
  void setTurnLeft(double degrees);

  /**
   * Turn the bot to the left (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxTurnRate(double)}.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnLeft(double)} and {@link
   * #setTurnRight(double)}.
   *
   * @param degrees is the amount of degrees to turn left. If negative, the bot will turn right.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnLeft(double)
   * @see #setTurnRight(double)
   * @see #turnRight(double)
   * @see #getTurnRemaining()
   * @see #setTurnRate(double)
   */
  void turnLeft(double degrees);

  /**
   * Set the bot to turn to the right (following the decreasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxTurnRate(double)}.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnLeft(double)}.
   *
   * @param degrees is the amount of degrees to turn right. If negative, the bot will turn left.
   * @see #setTurnLeft(double)
   * @see #turnRight(double)
   * @see #turnLeft(double)
   * @see #getTurnRemaining()
   * @see #setTurnRate(double)
   */
  void setTurnRight(double degrees);

  /**
   * Turn the bot to the right (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxTurnRate(double)}.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnLeft(double)} and {@link
   * #setTurnRight(double)}.
   *
   * @param degrees is the amount of degrees to turn right. If negative, the bot will turn left.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnLeft(double)
   * @see #setTurnRight(double)
   * @see #turnLeft(double)
   * @see #getTurnRemaining()
   * @see #setTurnRate(double)
   */
  void turnRight(double degrees);

  /**
   * Returns the remaining turn in degrees till the bot has finished turning after having called
   * {@link #setTurnLeft(double)}, {@link #setTurnRight(double)}, {@link #turnLeft(double)}, or
   * {@link #turnRight(double)}. When the turn remaining has reached 0, the bot has finished
   * turning.
   *
   * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
   * When the turn remaining is negative, the bot is turning to the right.
   *
   * @return The remaining degrees to turn before its current turning is completed.
   * @see #setTurnLeft(double)
   * @see #setTurnRight(double)
   * @see #turnLeft(double)
   * @see #turnRight(double)
   */
  double getTurnRemaining();

  /**
   * Sets the maximum turn rate which applies to turn the bot to the left or right. The maximum turn
   * rate must be an absolute value from 0 to {@link #MAX_TURN_RATE}, both values are included. If
   * the input turn rate is negative, the max turn rate will be cut to zero. If the input turn rate
   * is above {@link #MAX_TURN_RATE}, the max turn rate will be set to {@link #MAX_TURN_RATE}.
   *
   * <p>If for example the max turn rate is set to 5, then the bot will be able to turn left or
   * right with a turn rate down to -5 degrees per turn when turning right, and up to 5 degrees per
   * turn when turning left.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * @param maxTurnRate is the new maximum turn rate
   * @see #setTurnRate(double)
   * @see #setTurnLeft(double)
   * @see #setTurnRight(double)
   * @see #turnLeft(double)
   * @see #turnRight(double)
   */
  void setMaxTurnRate(double maxTurnRate);

  /**
   * Set the gun to turn to the left (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getGunTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxGunTurnRate(double)}.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnGunRight(double)}.
   *
   * @param degrees is the amount of degrees to turn left. If negative, the gun will turn right.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnGunRight(double)
   * @see #turnGunRight(double)
   * @see #turnGunLeft(double)
   * @see #getGunTurnRemaining()
   * @see #setGunTurnRate(double)
   */
  void setTurnGunLeft(double degrees);

  /**
   * Turn the gun to the left (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getGunTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxGunTurnRate(double)}.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnGunLeft(double)} and
   * {@link #setTurnGunRight(double)}.
   *
   * @param degrees is the amount of degrees to turn left. If negative, the gun will turn right.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnGunLeft(double)
   * @see #setTurnGunRight(double)
   * @see #turnGunRight(double)
   * @see #getGunTurnRemaining()
   * @see #setGunTurnRate(double)
   */
  void turnGunLeft(double degrees);

  /**
   * Set the gun to turn to the right (following the decreasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getGunTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxGunTurnRate(double)}.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnGunLeft(double)}.
   *
   * @param degrees is the amount of degrees to turn right. If negative, the gun will turn left.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnGunLeft(double)
   * @see #turnGunRight(double)
   * @see #turnGunLeft(double)
   * @see #getGunTurnRemaining()
   * @see #setGunTurnRate(double)
   */
  void setTurnGunRight(double degrees);

  /**
   * Turn the gun to the right (following the decreasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getGunTurnRemaining()} is 0. The amount of degrees to
   * turn each turn is limited by {@link #setMaxGunTurnRate(double)}.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnGunLeft(double)} and
   * {@link #setTurnGunRight(double)}.
   *
   * @param degrees is the amount of degrees to turn right. If negative, the gun will turn left.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnGunLeft(double)
   * @see #setTurnGunRight(double)
   * @see #turnGunLeft(double)
   * @see #getGunTurnRemaining()
   * @see #setGunTurnRate(double)
   */
  void turnGunRight(double degrees);

  /**
   * Returns the remaining turn in degrees till the gun has finished turning after having called
   * {@link #setTurnGunLeft(double)}, {@link #setTurnGunRight(double)}, {@link
   * #turnGunLeft(double)}, or {@link #turnGunRight(double)}. When the turn remaining has reached 0,
   * the gun has finished turning.
   *
   * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
   * When the turn remaining is negative, the bot is turning to the right.
   *
   * @return The remaining degrees to turn the gun before its current turning is completed.
   * @see #setTurnGunLeft(double)
   * @see #setTurnGunRight(double)
   * @see #turnGunLeft(double)
   * @see #turnGunRight(double)
   */
  double getGunTurnRemaining();

  /**
   * Sets the maximum turn rate which applies to turn the gun to the left or right. The maximum turn
   * rate must be an absolute value from 0 to {@link #MAX_GUN_TURN_RATE}, both values are included.
   * If the input turn rate is negative, the max turn rate will be cut to zero. If the input turn
   * rate is above {@link #MAX_GUN_TURN_RATE}, the max turn rate will be set to {@link
   * #MAX_GUN_TURN_RATE}.
   *
   * <p>If for example the max gun turn rate is set to 5, then the gun will be able to turn left or
   * right with a turn rate down to -5 degrees per turn when turning right and up to 5 degrees per
   * turn when turning left.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * @param maxGunTurnRate is the new maximum gun turn rate
   * @see #setGunTurnRate(double)
   * @see #setTurnGunLeft(double)
   * @see #setTurnGunRight(double)
   * @see #turnGunLeft(double)
   * @see #turnGunRight(double)
   */
  void setMaxGunTurnRate(double maxGunTurnRate);

  /**
   * Set the radar to turn to the left (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getRadarTurnRemaining()} is 0. The amount of degrees
   * to turn each turn is limited by {@link #setMaxRadarTurnRate(double)}.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarRight(double)}.
   *
   * @param degrees is the amount of degrees to turn left. If negative, the radar will turn right.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnRadarRight(double)
   * @see #turnRadarRight(double)
   * @see #turnRadarLeft(double)
   * @see #getRadarTurnRemaining()
   * @see #setRadarTurnRate(double)
   */
  void setTurnRadarLeft(double degrees);

  /**
   * Turn the radar to the left (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getRadarTurnRemaining()} is 0. The amount of degrees
   * to turn each turn is limited by {@link #setMaxRadarTurnRate(double)}.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarLeft(double)} and
   * {@link #setTurnRadarRight(double)}.
   *
   * @param degrees is the amount of degrees to turn left. If negative, the radar will turn right.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnRadarLeft(double)
   * @see #setTurnRadarRight(double)
   * @see #turnRadarRight(double)
   * @see #getRadarTurnRemaining()
   * @see #setRadarTurnRate(double)
   */
  void turnRadarLeft(double degrees);

  /**
   * Set the radar to turn to the right (following the decreasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getRadarTurnRemaining()} is 0. The amount of degrees
   * to turn each turn is limited by {@link #setMaxRadarTurnRate(double)}.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarLeft(double)} and
   * setTurnRadarRight(double).
   *
   * @param degrees is the amount of degrees to turn right. If negative, the radar will turn left.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnRadarLeft(double)
   * @see #turnRadarLeft(double)
   * @see #turnRadarRight(double)
   * @see #getRadarTurnRemaining()
   * @see #setRadarTurnRate(double)
   */
  void setTurnRadarRight(double degrees);

  /**
   * Turn the radar to the right (following the increasing degrees of the <a
   * href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
   * amount of degrees. That is, when {@link #getRadarTurnRemaining()} is 0. The amount of degrees
   * to turn each turn is limited by {@link #setMaxRadarTurnRate(double)}.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarLeft(double)} and
   * {@link #setTurnRadarRight(double)}.
   *
   * @param degrees is the amount of degrees to turn right. If negative, the radar will turn left.
   * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
   * @see #setTurnRadarLeft(double)
   * @see #setTurnRadarRight(double)
   * @see #turnRadarRight(double)
   * @see #getRadarTurnRemaining()
   * @see #setRadarTurnRate(double)
   */
  void turnRadarRight(double degrees);

  /**
   * Returns the remaining turn in degrees till the radar has finished turning after having called
   * {@link #setTurnRadarLeft(double)}, {@link #setTurnRadarRight(double)}, {@link
   * #turnRadarLeft(double)}, or {@link #turnRadarRight(double)}. When the turn remaining has
   * reached 0, the radar has finished turning.
   *
   * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
   * When the turn remaining is negative, the bot is turning to the right.
   *
   * @return The remaining degrees to turn the radar before its current turning is completed.
   * @see #setTurnRadarLeft(double)
   * @see #setTurnRadarRight(double)
   * @see #turnRadarLeft(double)
   * @see #turnRadarRight(double)
   */
  double getRadarTurnRemaining();

  /**
   * Sets the maximum turn rate which applies to turn the radar to the left or right. The maximum
   * turn rate must be an absolute value from 0 to {@link #MAX_RADAR_TURN_RATE}, both values are
   * included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
   * input turn rate is above {@link #MAX_RADAR_TURN_RATE}, the max turn rate will be set to {@link
   * #MAX_RADAR_TURN_RATE}.
   *
   * <p>If for example the max radar turn rate is set to 5, then the radar will be able to turn left
   * or right with a turn rate down to -5 degrees per turn when turning right and up to 5 degrees
   * per turn when turning left.
   *
   * <p>This method will first be executed when {@link #go()} is called making it possible to call
   * other set methods after execution. This makes it possible to set the bot to move, turn the
   * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
   * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
   * <strong>setter</strong> methods only prior to calling {@link #go()}.
   *
   * <p>If this method is called multiple times, the last call before {@link #go()} is executed,
   * counts.
   *
   * @param maxRadarTurnRate is the new maximum radar turn rate
   * @see #setRadarTurnRate(double)
   * @see #setTurnRadarLeft(double)
   * @see #setTurnRadarRight(double)
   * @see #turnRadarLeft(double)
   * @see #turnRadarRight(double)
   */
  void setMaxRadarTurnRate(double maxRadarTurnRate);

  /**
   * Fire the gun in the direction as the gun is pointing.
   *
   * <p>Note that your bot is spending energy when firing a bullet, the amount of energy used for
   * firing the bullet is taken from the bot. The amount of energy loss is equal to firepower.
   *
   * <p>If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
   * another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
   *
   * <p>The gun will only fire when the firepower is at {@link #MIN_FIREPOWER} or higher. If the
   * firepower is more than {@link #MAX_FIREPOWER}, the power will be truncated to the max
   * firepower.
   *
   * <p>Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
   * again. The gun heat must be zero before the gun can fire (see {@link #getGunHeat()}. The gun
   * heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used the
   * longer it takes to cool down the gun. The gun cooling rate can be read by calling {@link
   * #getGunCoolingRate()}.
   *
   * <p>The amount of energy used for firing the gun is subtracted from the bots total energy. The
   * amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
   * greater than 1 it will do an additional 2 x (firepower - 1) damage.
   *
   * <p>The firepower is truncated to {@link #MIN_FIREPOWER} and {@link #MAX_FIREPOWER} if the
   * firepower exceeds these values.
   *
   * <p>This call is executed immediately by calling {@link #go()} in the code behind. This method
   * will block until it has been completed, which can take one to several turns. New commands will
   * first take place after this method is completed. If you need to execute multiple commands in
   * parallel, use <strong>setter</strong> methods instead of this blocking method.
   *
   * <p>This method will cancel the effect of prior calls to {@link #setFire(double)}.
   *
   * @param firepower is the amount of energy spent on firing the gun. You cannot spend more energy
   *     than available from the bot. The bullet power must be greater than {@link #MIN_FIREPOWER}.
   * @see #onBulletFired(BulletFiredEvent)
   * @see #setFire(double)
   * @see #getGunHeat()
   * @see #getGunCoolingRate()
   */
  void fire(double firepower);

  /**
   * Stop all movement including turning the gun and radar. The remaining movement is saved for a
   * call to {@link #resume()}. This method has no effect, if it has already been called.
   *
   * @see #resume()
   * @see #getDistanceRemaining()
   * @see #getTurnRemaining()
   * @see #getGunTurnRemaining()
   * @see #getRadarTurnRemaining()
   */
  void stop();

  /**
   * Resume the movement prior to calling the {@link #stop()} method. This method has no effect, if
   * it has already been called.
   *
   * @see #stop()
   * @see #getDistanceRemaining()
   * @see #getTurnRemaining()
   * @see #getGunTurnRemaining()
   * @see #getRadarTurnRemaining()
   */
  void resume();

  /**
   * Scan (again) with the radar. This method is useful if the radar has not moved, and hence will
   * not automatically scan bots. This method is useful when the robot movement has stopped, e.g.
   * when {@link #stop()} has been called. The last radar direction and sweep angle will be used for
   * scanning for bots.
   *
   * @see #stop()
   */
  void scan();

  /**
   * Blocks until a condition is met, i.e. when a {@link Condition#test()} returns true.
   *
   * @param condition is the condition that must be met before this method will stop waiting.
   * @see Condition
   * @see #onCondition(Condition)
   */
  void waitFor(Condition condition);

  /**
   * The event handler triggered when a condition has been met with the {@link #waitFor(Condition)}
   * method. Use the {@link Condition#getName()} of the condition if you need to differ between
   * multiple conditions being met.
   *
   * @param condition is the condition that has been met.
   * @see #waitFor(Condition)
   */
  default void onCondition(Condition condition) {}
}
