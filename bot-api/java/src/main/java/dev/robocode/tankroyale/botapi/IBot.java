package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.Condition;

/**
 * Interface for a bot that extends the core API with convenient methods for movement, turning, and
 * firing the gun.
 * <script src="../../../../prism.js"></script>
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface IBot extends IBaseBot {
    /**
     * The run() method is used for running a program for the bot like:
     *
     * <pre><code class="language-java">
     * public void run() {
     *   while (isRunning()) {
     *     forward(100);
     *     turnGunLeft(360);
     *     back(100);
     *     turnGunRight(360);
     *   }
     * }
     * </code></pre>
     *
     * <p>Note that the program runs in a loop in this example (as long as the bot is running),
     * meaning that it will start moving forward as soon as {@link #turnGunRight} has executed.
     *
     * <p>When running a loop that could potentially run forever. The best practice is to check if the
     * bot is still running to stop and exit the loop. This gives the game a chance of stopping the
     * thread running the loop in the code behind. If the thread is not stopped correctly, the bot may
     * behave strangely in new rounds.
     *
     * @see #isRunning
     */
    default void run() {
    }

    /**
     * Checks if this bot is running.
     *
     * @return {@code true} when the bot is running, {@code false} otherwise.
     */
    boolean isRunning();

    /**
     * Set the bot to move forward until it has traveled a specific distance from its current
     * position, or it is moving into an obstacle. The speed is limited by {@link #setMaxSpeed}.
     *
     * <p>When the bot is moving forward, the {@link Constants#ACCELERATION} determines the acceleration of the
     * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
     * faster at braking. The {@link Constants#DECELERATION} determines the deceleration of the bot that
     * subtracts 2 units from the speed per turn.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods before execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed,
     * counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed} as the
     * setForward and {@link #setBack} methods calls the {@link #setTargetSpeed} for each turn until
     * {@link #getDistanceRemaining} reaches 0.
     *
     * @param distance is the distance to move forward. If negative, the bot will move backward.
     *                 If {@link Double#POSITIVE_INFINITY} the bot will move forward infinitely.
     *                 If {@link Double#NEGATIVE_INFINITY} the bot will move backward infinitely.
     * @see #forward
     * @see #setBack
     * @see #back
     * @see #getDistanceRemaining
     * @see #setTargetSpeed
     */
    void setForward(double distance);

    /**
     * Moves the bot forward until it has traveled a specific distance from its current position, or
     * it is moving into an obstacle. The speed is limited by {@link #setMaxSpeed}.
     *
     * <p>When the bot is moving forward, the {@link Constants#ACCELERATION} determine the acceleration of the
     * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
     * faster at braking. The {@link Constants#DECELERATION} determines the deceleration of the bot that
     * subtracts 2 units from the speed per turn.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed}, {@link #setForward},
     * and {@link #setBack} methods.
     *
     * @param distance is the distance to move forward. If negative, the bot will move backward.
     *                 If {@link Double#POSITIVE_INFINITY} the bot will move forward infinitely.
     *                 If {@link Double#NEGATIVE_INFINITY} the bot will move backward infinitely.
     * @see #setForward
     * @see #setBack
     * @see #back
     * @see #getDistanceRemaining
     * @see #setTargetSpeed
     */
    void forward(double distance);

    /**
     * Set the bot to move backward until it has traveled a specific distance from its current
     * position, or it is moving into an obstacle. The speed is limited by {@link #setMaxSpeed}.
     *
     * <p>When the bot is moving forward, the {@link Constants#ACCELERATION} determines the acceleration of the
     * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
     * faster at braking. The {@link Constants#DECELERATION} determines the deceleration of the bot that
     * subtracts 2 units from the speed per turn.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed,
     * counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed} as the
     * {@link #setForward} and setBack methods calls the {@link #setTargetSpeed} for each turn until
     * {@link #getDistanceRemaining} reaches 0.
     *
     * @param distance is the distance to move backward. If negative, the bot will move forward.
     *                 If {@link Double#POSITIVE_INFINITY} the bot will move backward infinitely.
     *                 If {@link Double#NEGATIVE_INFINITY} the bot will move forward infinitely.
     * @see #back
     * @see #setForward
     * @see #forward
     * @see #getDistanceRemaining
     * @see #setTargetSpeed
     */
    void setBack(double distance);

    /**
     * Moves the bot backward until it has traveled a specific distance from its current position, or
     * it is moving into an obstacle. The speed is limited by {@link #setMaxSpeed}.
     *
     * <p>When the bot is moving forward, the {@link Constants#ACCELERATION} determine the acceleration of the
     * bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
     * faster at braking. The {@link Constants#DECELERATION} determine the deceleration of the bot that
     * subtracts 2 units from the speed per turn.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTargetSpeed}, {@link #setForward},
     * and {@link #setBack} methods.
     *
     * @param distance is the distance to move backward. If negative, the bot will move forward.
     *                 If {@link Double#POSITIVE_INFINITY} the bot will move backward infinitely.
     *                 If {@link Double#NEGATIVE_INFINITY} the bot will move forward infinitely.
     * @see #setForward
     * @see #setBack
     * @see #forward
     * @see #getDistanceRemaining
     * @see #setTargetSpeed
     */
    void back(double distance);

    /**
     * Returns the distance remaining till the bot has finished moving after having called {@link
     * #setForward}, {@link #setBack}, {@link #forward}, or {@link #back}.
     * When the distance remaining has reached 0, the bot has finished its current move.
     *
     * <p>When the distance remaining is positive, the bot is moving forward. When the distance
     * remaining is negative, the bot is moving backward.
     *
     * @return The remaining distance to move before its current movement is completed.
     * If {@link Double#POSITIVE_INFINITY} the bot will move forward infinitely.
     * If {@link Double#NEGATIVE_INFINITY} the bot will move backward infinitely.
     * @see #setForward
     * @see #setBack
     * @see #forward
     * @see #back
     */
    double getDistanceRemaining();

    /**
     * Set the bot to turn to the left (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxTurnRate}.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed,
     * counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnRight}.
     *
     * @param degrees is the amount of degrees to turn left. If negative, the bot will turn right.
     *                If {@link Double#POSITIVE_INFINITY} the bot will turn left infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the bot will turn right infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnRight
     * @see #turnRight
     * @see #turnLeft
     * @see #getTurnRemaining
     * @see #setTurnRate
     */
    void setTurnLeft(double degrees);

    /**
     * Turn the bot to the left (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxTurnRate}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnLeft} and {@link
     * #setTurnRight}.
     *
     * @param degrees is the amount of degrees to turn left. If negative, the bot will turn right.
     *                If {@link Double#POSITIVE_INFINITY} the bot will turn left infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the bot will turn right infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnLeft
     * @see #setTurnRight
     * @see #turnRight
     * @see #getTurnRemaining
     * @see #setTurnRate
     */
    void turnLeft(double degrees);

    /**
     * Set the bot to turn to the right (following the decreasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxTurnRate}.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed, counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnLeft}.
     *
     * @param degrees is the amount of degrees to turn right. If negative, the bot will turn left.
     *                If {@link Double#POSITIVE_INFINITY} the bot will turn right infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the bot will turn left infinitely.
     * @see #setTurnLeft
     * @see #turnRight
     * @see #turnLeft
     * @see #getTurnRemaining
     * @see #setTurnRate
     */
    void setTurnRight(double degrees);

    /**
     * Turn the bot to the right (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxTurnRate}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnLeft} and {@link
     * #setTurnRight}.
     *
     * @param degrees is the amount of degrees to turn right. If negative, the bot will turn left.
     *                If {@link Double#POSITIVE_INFINITY} the bot will turn right infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the bot will turn left infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnLeft
     * @see #setTurnRight
     * @see #turnLeft
     * @see #getTurnRemaining
     * @see #setTurnRate
     */
    void turnRight(double degrees);

    /**
     * Returns the remaining turn in degrees till the bot has finished turning after having called
     * {@link #setTurnLeft}, {@link #setTurnRight}, {@link #turnLeft}, or {@link #turnRight}.
     * When the turn remaining has reached 0, the bot has finished turning.
     *
     * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
     * When the turn remaining is negative, the bot is turning to the right.
     *
     * @return The remaining degrees to turn before its current turning is completed.
     * If {@link Double#POSITIVE_INFINITY} the bot will turn left infinitely.
     * If {@link Double#NEGATIVE_INFINITY} the bot will turn right infinitely.
     * @see #setTurnLeft
     * @see #setTurnRight
     * @see #turnLeft
     * @see #turnRight
     */
    double getTurnRemaining();

    /**
     * Set the gun to turn to the left (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getGunTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxGunTurnRate}.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed,
     * counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnGunRight}.
     *
     * @param degrees is the amount of degrees to turn left. If negative, the gun will turn right.
     *                If {@link Double#POSITIVE_INFINITY} the gun will turn left infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the gun will turn right infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnGunRight
     * @see #turnGunRight
     * @see #turnGunLeft
     * @see #getGunTurnRemaining
     * @see #setGunTurnRate
     */
    void setTurnGunLeft(double degrees);

    /**
     * Turn the gun to the left (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getGunTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxGunTurnRate}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnGunLeft} and
     * {@link #setTurnGunRight}.
     *
     * @param degrees is the amount of degrees to turn left. If negative, the gun will turn right.
     *                If {@link Double#POSITIVE_INFINITY} the gun will turn left infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the gun will turn right infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnGunLeft
     * @see #setTurnGunRight
     * @see #turnGunRight
     * @see #getGunTurnRemaining
     * @see #setGunTurnRate
     */
    void turnGunLeft(double degrees);

    /**
     * Set the gun to turn to the right (following the decreasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getGunTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxGunTurnRate}.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed, counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnGunLeft}.
     *
     * @param degrees is the amount of degrees to turn right. If negative, the gun will turn left.
     *                If {@link Double#POSITIVE_INFINITY} the gun will turn right infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the gun will turn left infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnGunLeft
     * @see #turnGunRight
     * @see #turnGunLeft
     * @see #getGunTurnRemaining
     * @see #setGunTurnRate
     */
    void setTurnGunRight(double degrees);

    /**
     * Turn the gun to the right (following the decreasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getGunTurnRemaining} is 0. The amount of degrees to
     * turn each turn is limited by {@link #setMaxGunTurnRate}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnGunLeft} and
     * {@link #setTurnGunRight}.
     *
     * @param degrees is the amount of degrees to turn right. If negative, the gun will turn left.
     *                If {@link Double#POSITIVE_INFINITY} the gun will turn right infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the gun will turn left infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnGunLeft
     * @see #setTurnGunRight
     * @see #turnGunLeft
     * @see #getGunTurnRemaining
     * @see #setGunTurnRate
     */
    void turnGunRight(double degrees);

    /**
     * Returns the remaining turn in degrees till the gun has finished turning after having called
     * {@link #setTurnGunLeft}, {@link #setTurnGunRight}, {@link #turnGunLeft}, or {@link #turnGunRight}.
     * When the turn remaining has reached 0, the gun has finished turning.
     *
     * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
     * When the turn remaining is negative, the bot is turning to the right.
     *
     * @return The remaining degrees to turn the gun before its current turning is completed.
     * If {@link Double#POSITIVE_INFINITY} the gun will turn left infinitely.
     * If {@link Double#NEGATIVE_INFINITY} the gun will turn right infinitely.
     * @see #setTurnGunLeft
     * @see #setTurnGunRight
     * @see #turnGunLeft
     * @see #turnGunRight
     */
    double getGunTurnRemaining();

    /**
     * Set the radar to turn to the left (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getRadarTurnRemaining} is 0. The amount of degrees
     * to turn each turn is limited by {@link #setMaxRadarTurnRate}.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed, counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarRight}.
     *
     * @param degrees is the amount of degrees to turn left. If negative, the radar will turn right.
     *                If {@link Double#POSITIVE_INFINITY} the radar will turn left infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the radar will turn right infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnRadarRight
     * @see #turnRadarRight
     * @see #turnRadarLeft
     * @see #getRadarTurnRemaining
     * @see #setRadarTurnRate
     */
    void setTurnRadarLeft(double degrees);

    /**
     * Turn the radar to the left (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getRadarTurnRemaining} is 0. The amount of degrees
     * to turn each turn is limited by {@link #setMaxRadarTurnRate}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarLeft} and
     * {@link #setTurnRadarRight}.
     *
     * @param degrees is the amount of degrees to turn left. If negative, the radar will turn right.
     *                If {@link Double#POSITIVE_INFINITY} the radar will turn left infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the radar will turn right infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnRadarLeft
     * @see #setTurnRadarRight
     * @see #turnRadarRight
     * @see #getRadarTurnRemaining
     * @see #setRadarTurnRate
     */
    void turnRadarLeft(double degrees);

    /**
     * Set the radar to turn to the right (following the decreasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getRadarTurnRemaining} is 0. The amount of degrees
     * to turn each turn is limited by {@link #setMaxRadarTurnRate}.
     *
     * <p>This method will first be executed when {@link #go} is called making it possible to call
     * other set methods after execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link #go}.
     * But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go}.
     *
     * <p>If this method is called multiple times, the last call before {@link #go} is executed, counts.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarLeft} and
     * setTurnRadarRight(double).
     *
     * @param degrees is the amount of degrees to turn right. If negative, the radar will turn left.
     *                If {@link Double#POSITIVE_INFINITY} the radar will turn right infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the radar will turn left infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnRadarLeft
     * @see #turnRadarLeft
     * @see #turnRadarRight
     * @see #getRadarTurnRemaining
     * @see #setRadarTurnRate
     */
    void setTurnRadarRight(double degrees);

    /**
     * Turn the radar to the right (following the increasing degrees of the
     * <a href="https://en.wikipedia.org/wiki/Unit_circle">unit circle</a>) until it turned the specified
     * amount of degrees. That is, when {@link #getRadarTurnRemaining} is 0. The amount of degrees
     * to turn each turn is limited by {@link #setMaxRadarTurnRate}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setTurnRadarLeft} and
     * {@link #setTurnRadarRight}.
     *
     * @param degrees is the amount of degrees to turn right. If negative, the radar will turn left.
     *                If {@link Double#POSITIVE_INFINITY} the radar will turn right infinitely.
     *                If {@link Double#NEGATIVE_INFINITY} the radar will turn left infinitely.
     * @see <a href="https://en.wikipedia.org/wiki/Unit_circle">Unit circle</a>
     * @see #setTurnRadarLeft
     * @see #setTurnRadarRight
     * @see #turnRadarRight
     * @see #getRadarTurnRemaining
     * @see #setRadarTurnRate
     */
    void turnRadarRight(double degrees);

    /**
     * Returns the remaining turn in degrees till the radar has finished turning after having called
     * {@link #setTurnRadarLeft}, {@link #setTurnRadarRight}, {@link #turnRadarLeft}, or {@link #turnRadarRight}.
     * When the turn remaining has reached 0, the radar has finished turning.
     *
     * <p>When the turn remaining is positive, the bot is turning to the left (along the unit circle).
     * When the turn remaining is negative, the bot is turning to the right.
     *
     * @return The remaining degrees to turn the radar before its current turning is completed.
     * If {@link Double#POSITIVE_INFINITY} the radar will turn left infinitely.
     * If {@link Double#NEGATIVE_INFINITY} the radar will turn right infinitely.
     * @see #setTurnRadarLeft
     * @see #setTurnRadarRight
     * @see #turnRadarLeft
     * @see #turnRadarRight
     */
    double getRadarTurnRemaining();

    /**
     * Fire the gun in the direction as the gun is pointing.
     *
     * <p>Note that your bot is spending energy when firing a bullet, the amount of energy used for
     * firing the bullet is taken from the bot. The amount of energy loss is equal to firepower.
     *
     * <p>If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
     * another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
     *
     * <p>The gun will only fire when the firepower is at {@link Constants#MIN_FIREPOWER} or higher. If the
     * firepower is more than {@link Constants#MAX_FIREPOWER}, the power will be truncated to the max
     * firepower.
     *
     * <p>Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
     * again. The gun heat must be zero before the gun can fire (see {@link #getGunHeat}. The gun
     * heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used the
     * longer it takes to cool down the gun. The gun cooling rate can be read by calling {@link
     * #getGunCoolingRate}.
     *
     * <p>The amount of energy used for firing the gun is subtracted from the bots total energy. The
     * amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
     * greater than 1 it will do an additional 2 x (firepower - 1) damage.
     *
     * <p>The firepower is truncated to {@link Constants#MIN_FIREPOWER} and {@link Constants#MAX_FIREPOWER} if the
     * firepower exceeds these values.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * <p>This method will cancel the effect of prior calls to {@link #setFire}.
     *
     * @param firepower is the amount of energy spent on firing the gun. You cannot spend more energy
     *                  than available from the bot. The bullet power must be greater than {@link Constants#MIN_FIREPOWER}.
     * @see #onBulletFired
     * @see #setFire
     * @see #getGunHeat
     * @see #getGunCoolingRate
     */
    void fire(double firepower);

    /**
     * Stop all movement including turning the gun and radar. The remaining movement is saved for a
     * call to {@link #setResume} or {@link #resume}. This method has no effect, if it has already
     * been called.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * @see #resume
     * @see #setResume
     * @see #setStop
     */
    void stop();

    /**
     * Stop all movement including turning the gun and radar. The remaining movement is saved for a
     * call to {@link #setResume} or {@link #resume}.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * @param overwrite is set to <code>true</code> if the movement saved by a previous call to this
     *                  method or {@link #setStop()} must be overridden with the current movement.
     *                  When set to <code>false</code> this method is identical to {@link #setStop()}.
     * @see #resume
     * @see #setResume
     * @see #setStop
     */
    void stop(boolean overwrite);

    /**
     * Resume the movement prior to calling the {@link #setStop} or {@link #stop} method. This
     * method has no effect, if it has already been called.
     *
     * <p>This call is executed immediately by calling {@link #go} in the code behind. This method
     * will block until it has been completed, which can take one to several turns. New commands will
     * first take place after this method is completed. If you need to execute multiple commands in
     * parallel, use <strong>setter</strong> methods instead of this blocking method.
     *
     * @see #stop
     * @see #setStop
     * @see #setResume
     */
    void resume();

    /**
     * Scan (again) with the radar. This method is useful if the radar has not been turning and
     * thereby will not be able to automatically scan bots. This method is useful when the bot
     * movement has stopped, e.g. when {@link #stop} has been called. The last radar direction and
     * sweep angle will be used for rescanning for bots.
     *
     * @see #stop
     */
    void rescan();

    /**
     * Blocks until a condition is met, i.e. when a {@link Condition#test} returns true.
     *
     * @param condition is the condition that must be met before this method will stop waiting.
     * @see Condition
     * @see #onCustomEvent
     */
    void waitFor(Condition condition);
}
