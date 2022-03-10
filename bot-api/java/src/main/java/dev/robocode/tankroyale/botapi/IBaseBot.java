package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.Collection;

/**
 * Interface containing the core API for a bot.
 *
 * <script src="../../../../prism.js"></script>
 */
@SuppressWarnings({"UnusedDeclaration", "EmptyMethod"})
public interface IBaseBot {

    /**
     * The method used to start running the bot. You should call this method from the main method or
     * similar.
     *
     * <p>Example:
     * <pre><code class="language-java">
     * public void main(String[] args) {
     *     // create myBot
     *     ...
     *     myBot.start();
     * }
     * </code></pre>
     */
    void start();

    /**
     * Commits the current commands (actions), which finalizes the current turn for the bot.
     *
     * <p>This method must be called once per turn to send the bot actions to the server and must be
     * called before the turn timeout occurs. A turn timer is started when the {@link
     * GameStartedEvent} and {@link TickEvent} occurs. If the {@code go()} method is called too late,
     * a turn timeout will occur and the {@link SkippedTurnEvent} will occur, which means that the bot
     * has skipped all actions for the last turn. In this case, the server will continue executing the
     * last actions received. This could be fatal for the bot due to loss of control over the bot. So
     * make sure that {@code go()} is called before the turn ends.
     *
     * <p>The commands executed when {@code go()} is called are set by calling the various setter
     * methods prior to calling the {@code go()} method: {@link #setTurnRate(double)}, {@link
     * #setGunTurnRate(double)}, {@link #setRadarTurnRate(double)}, {@link #setTargetSpeed(double)},
     * and {@link #setFire(double)}.
     *
     * @see #getTurnTimeout()
     */
    void go();

    /**
     * Unique id of this bot, which is available when the game has started.
     *
     * @return The unique id of this bot.
     */
    int getMyId();

    /**
     * The game variant, which is "Tank Royale".
     *
     * @return The game variant of Robocode.
     */
    String getVariant();

    /**
     * Game version, e.g. "1.0.0".
     *
     * @return The game version.
     */
    String getVersion();

    /**
     * Game type, e.g. "melee" or "1v1".
     *
     * <p>First available when the game has started.
     *
     * @return The game type.
     */
    String getGameType();

    /**
     * Width of the arena measured in units.
     *
     * <p>First available when the game has started.
     *
     * @return The arena width measured in units
     */
    int getArenaWidth();

    /**
     * Height of the arena measured in units.
     *
     * <p>First available when the game has started.
     *
     * @return The arena height measured in units
     */
    int getArenaHeight();

    /**
     * The number of rounds in a battle.
     *
     * <p>First available when the game has started.
     *
     * @return The number of rounds in a battle.
     */
    int getNumberOfRounds();

    /**
     * Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun can fire. The
     * gun cooling rate determines how fast the gun cools down. That is, the gun cooling rate is
     * subtracted from the gun heat each turn until the gun heat reaches zero.
     *
     * <p>First available when the game has started.
     *
     * @return The gun cooling rate.
     * @see #getGunHeat()
     */
    double getGunCoolingRate();

    /**
     * The maximum number of inactive turns allowed the bot will become zapped by the game for being
     * inactive. Inactive means that the bot has taken no action in several turns in a row.
     *
     * <p>First available when the game has started.
     *
     * @return The maximum number of allowed inactive turns.
     */
    int getMaxInactivityTurns();

    /**
     * The turn timeout is important as the bot needs to take action by calling {@link #go()} before
     * the turn timeout occurs. As soon as the {@link TickEvent} is triggered, i.e. when {@link
     * #onTick(TickEvent)} is called, you need to call {@link #go()} to take action before the turn
     * timeout occurs. Otherwise, your bot will skip a turn and receive a {@link
     * #onSkippedTurn(SkippedTurnEvent)} for each turn where {@link #go()} is called too late.
     *
     * <p>First available when the game has started.
     *
     * @return The turn timeout in microseconds (1 / 1,000,000 second).
     * @see #getTimeLeft()
     * @see #go()
     */
    int getTurnTimeout();

    /**
     * The number of microseconds left of this turn before the bot will skip the turn. Make sure to
     * call {@link #go()} before the time runs out.
     *
     * @return The amount of time left in microseconds.
     * @see #getTurnTimeout()
     * @see #go()
     */
    int getTimeLeft();

    /**
     * Current round number.
     *
     * @return The current round number.
     */
    int getRoundNumber();

    /**
     * Current turn number.
     *
     * @return The current turn number.
     */
    int getTurnNumber();

    /**
     * Number of enemies left in the round.
     *
     * @return The number of enemies left in the round.
     */
    int getEnemyCount();

    /**
     * Current energy level. When the energy level is positive, the bot is alive and active. When the
     * energy level is 0, the bot is still alive but disabled. If the bot becomes disabled it will not
     * be able to move or take any action. If negative, the bot has been defeated.
     *
     * @return The current energy level.
     */
    double getEnergy();

    /**
     * Specifies if the bot is disabled, i.e., when the energy is zero. When the bot is disabled, it
     * is not able to take any action like movement, turning, and firing.
     *
     * @return {@code true} if the bot is disabled; {@code false} otherwise.
     */
    boolean isDisabled();

    /**
     * Current X coordinate of the center of the bot.
     *
     * @return The current X coordinate of the bot.
     */
    double getX();

    /**
     * Current Y coordinate of the center of the bot.
     *
     * @return The current Y coordinate of the bot.
     */
    double getY();

    /**
     * Current driving direction of the bot in degrees.
     *
     * @return The current driving direction of the bot.
     */
    double getDirection();

    /**
     * Current direction of the gun in degrees.
     *
     * @return The current gun direction of the bot.
     */
    double getGunDirection();

    /**
     * Current direction of the radar in degrees.
     *
     * @return The current radar direction of the bot.
     */
    double getRadarDirection();

    /**
     * The current speed measured in units per turn. If the speed is positive, the bot moves forward.
     * If negative, the bot moves backward. Zero speed means that the bot is not moving from its
     * current position.
     *
     * @return The current speed.
     */
    double getSpeed();

    /**
     * Current gun heat. When the is fired it gets heated and will not be able to fire before it has
     * been cooled down. The gun is cooled down when the gun heat is zero.
     *
     * <p>When the gun has fired the gun heat is set to 1 + (firepower / 5) and will be cooled down by
     * the gun cooling rate.
     *
     * @return The current gun heat.
     * @see #getGunCoolingRate()
     */
    double getGunHeat();

    /**
     * Current bullet states. Keeps track of all the bullets fired by the bot, which are still active
     * on the arena.
     *
     * @return The current bullet states.
     */
    Collection<BulletState> getBulletStates();

    /**
     * Game events received for the current turn. Note that all event handlers are automatically being
     * called when each of these events occurs.
     *
     * @return The game events received for the current turn.
     */
    Collection<? extends BotEvent> getEvents();

    /**
     * Returns the turn rate of the bot in degrees per turn.
     *
     * @return The turn rate of the bot.
     * @see #setTurnRate(double)
     */
    double getTurnRate();

    /**
     * Sets the turn rate of the bot, which can be positive and negative. The turn rate is measured in
     * degrees per turn. The turn rate is added to the current direction of the bot. But it is also
     * added to the current direction of the gun and radar. This is because the gun is mounted on the
     * body, and hence turns with the body. The radar is mounted on the gun and hence moves with the
     * gun. You can compensate for the turn rate of the bot by subtracting the turn rate of the bot
     * from the turn rate of the gun and radar. But be aware that the turn limits defined for the gun
     * and radar cannot be exceeded.
     *
     * <p>The turn rate is truncated to {@link Constants#MAX_TURN_RATE} if the turn rate exceeds this value.
     *
     * <p>If this property is set multiple times, the last value set before {@link #go()} counts.
     *
     * @param turnRate is the new turn rate of the bot in degrees per turn.
     */
    void setTurnRate(double turnRate);

    /**
     * Returns the maximum turn rate of the bot in degrees per turn.
     *
     * @return The maximum turn rate of the bot.
     * @see #setMaxTurnRate(double)
     */
    double getMaxTurnRate();

    /**
     * Sets the maximum turn rate which applies to turn the bot to the left or right. The maximum turn
     * rate must be an absolute value from 0 to {@link Constants#MAX_TURN_RATE}, both values are included. If
     * the input turn rate is negative, the max turn rate will be cut to zero. If the input turn rate
     * is above {@link Constants#MAX_TURN_RATE}, the max turn rate will be set to {@link Constants#MAX_TURN_RATE}.
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
     */
    void setMaxTurnRate(double maxTurnRate);

    /**
     * Returns the gun turn rate in degrees per turn.
     *
     * @return The turn rate of the gun.
     * @see #setGunTurnRate(double)
     */
    double getGunTurnRate();

    /**
     * Sets the turn rate of the gun, which can be positive and negative. The gun turn rate is
     * measured in degrees per turn. The turn rate is added to the current turn direction of the gun.
     * But it is also added to the current direction of the radar. This is because the radar is
     * mounted on the gun, and hence moves with the gun. You can compensate for the turn rate of the
     * gun by subtracting the turn rate of the gun from the turn rate of the radar. But be aware that
     * the turn limits defined for the radar cannot be exceeded.
     *
     * <p>The gun turn rate is truncated to {@link Constants#MAX_GUN_TURN_RATE} if the gun turn rate exceeds
     * this value.
     *
     * <p>If this property is set multiple times, the last value set before {@link #go()} counts.
     *
     * @param gunTurnRate is the new turn rate of the gun in degrees per turn.
     */
    void setGunTurnRate(double gunTurnRate);

    /**
     * Returns the maximum gun turn rate in degrees per turn.
     *
     * @return The maximum turn rate of the gun.
     * @see #setMaxGunTurnRate(double)
     */
    double getMaxGunTurnRate();

    /**
     * Sets the maximum turn rate which applies to turn the gun to the left or right. The maximum turn
     * rate must be an absolute value from 0 to {@link Constants#MAX_GUN_TURN_RATE}, both values are included.
     * If the input turn rate is negative, the max turn rate will be cut to zero. If the input turn
     * rate is above {@link Constants#MAX_GUN_TURN_RATE}, the max turn rate will be set to {@link
     * Constants#MAX_GUN_TURN_RATE}.
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
     */
    void setMaxGunTurnRate(double maxGunTurnRate);

    /**
     * Returns the radar turn rate in degrees per turn.
     *
     * @return The turn rate of the radar.
     * @see #setRadarTurnRate(double)
     */
    double getRadarTurnRate();

    /**
     * Sets the turn rate of the radar, which can be positive and negative. The radar turn rate is
     * measured in degrees per turn. The turn rate is added to the current direction of the radar.
     * Note that besides the turn rate of the radar, the turn rates of the bot and gun are also added
     * to the radar direction, as the radar moves with the gun, which is mounted on the gun that moves
     * with the body. You can compensate for the turn rate of the gun by subtracting the turn rate of
     * the bot and gun from the turn rate of the radar. But be aware that the turn limits defined for
     * the radar cannot be exceeded.
     *
     * <p>The radar turn rate is truncated to {@link Constants#MAX_RADAR_TURN_RATE} if the radar turn rate
     * exceeds this value.
     *
     * <p>If this property is set multiple times, the last value set before {@link #go()} counts.
     *
     * @param gunRadarTurnRate is the new turn rate of the radar in degrees per turn.
     */
    void setRadarTurnRate(double gunRadarTurnRate);

    /**
     * Returns the maximum radar turn rate in degrees per turn.
     *
     * @return The maimim turn rate of the radar.
     * @see #setMaxRadarTurnRate(double)
     */
    double getMaxRadarTurnRate();

    /**
     * Sets the maximum turn rate which applies to turn the radar to the left or right. The maximum
     * turn rate must be an absolute value from 0 to {@link Constants#MAX_RADAR_TURN_RATE}, both values are
     * included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
     * input turn rate is above {@link Constants#MAX_RADAR_TURN_RATE}, the max turn rate will be set to {@link
     * Constants#MAX_RADAR_TURN_RATE}.
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
     */
    void setMaxRadarTurnRate(double maxRadarTurnRate);

    /**
     * Returns the target speed in units per turn.
     *
     * @return The target speed.
     * @see #setTargetSpeed(double)
     */
    double getTargetSpeed();

    /**
     * Sets the new target speed for the bot in units per turn. The target speed is the speed you want
     * to achieve eventually, which could take one to several turns depending on the current speed.
     * For example, if the bot is moving forward with max speed, and then must change to move backward
     * at full speed, the bot will have to first decelerate/brake its positive speed (moving forward).
     * When passing speed of zero, it will then have to accelerate back to achieve max negative speed.
     *
     * <p>Note that acceleration is 1 unit per turn and deceleration/braking is faster than
     * acceleration as it is -2 unit per turn. Deceleration is negative as it is added to the speed
     * and hence needs to be negative when slowing down.
     *
     * <p>The target speed is truncated to {@link Constants#MAX_SPEED} if the target speed exceeds this value.
     *
     * <p>If this property is set multiple times, the last value set before {@link #go()} counts.
     *
     * @param targetSpeed is the new target speed in units per turn.
     */
    void setTargetSpeed(double targetSpeed);

    /**
     * Returns the maximum speed in units per turn.
     *
     * @return The maximum speed.
     * @see #setMaxSpeed(double)
     */
    double getMaxSpeed();

    /**
     * Sets the maximum speed which applies when moving forward and backward. The maximum speed must
     * be an absolute value from 0 to {@link Constants#MAX_SPEED}, both values are included. If the input speed
     * is negative, the max speed will be cut to zero. If the input speed is above {@link Constants#MAX_SPEED},
     * the max speed will be set to {@link Constants#MAX_SPEED}.
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
     */
    void setMaxSpeed(double maxSpeed);

    /**
     * Sets the gun to fire in the direction that the gun is pointing with the specified firepower.
     *
     * <p>Firepower is the amount of energy your bot will spend on firing the gun. This means that the
     * bot will lose power on firing the gun where the energy loss is equal to the firepower. You
     * cannot spend more energy than available from your bot.
     *
     * <p>The bullet power must be greater than {@link Constants#MIN_FIREPOWER} and the gun heat zero before
     * the gun can fire.
     *
     * <p>If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
     * another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
     *
     * <p>The gun will only fire when the firepower is at {@link Constants#MIN_FIREPOWER} or higher. If the
     * firepower is more than {@link Constants#MAX_FIREPOWER} the power will be truncated to the max firepower.
     *
     * <p>Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
     * again. The gun heat must be zero before the gun is able to fire (see {@link #getGunHeat()}).
     * The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used
     * the longer it takes to cool down the gun. The gun cooling rate can be read by calling {@link
     * #getGunCoolingRate()}.
     *
     * <p>The amount of energy used for firing the gun is subtracted from the bots' total energy. The
     * amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
     * greater than 1 it will do an additional 2 x (firepower - 1) damage.
     *
     * <p>Note that the gun will automatically keep firing at any turn as soon as the gun heat reaches
     * zero. It is possible to disable the gun firing by setting the firepower to zero.
     *
     * <p>The firepower is truncated to 0 and {@link Constants#MAX_FIREPOWER} if the firepower exceeds this
     * value.
     *
     * <p>If this property is set multiple times, the last value set before go() counts.
     *
     * @param firepower is the new firepower
     * @return {@code true} if the cannon can fire, i.e. if there no gun heat; {@code false}
     * otherwise.
     * @see #onBulletFired(BulletFiredEvent)
     * @see #getFirepower()
     * @see #getGunHeat()
     * @see #getGunCoolingRate()
     */
    boolean setFire(double firepower);

    /**
     * Returns the firepower.
     *
     * @return The firepower.
     * @see #setFire(double)
     */
    double getFirepower();

    /**
     * Sets the bot to rescan with the radar. This method is useful if the radar has not turned, and
     * hence will not automatically scan bots. The last radar direction and sweep angle will be used
     * for scanning for bots.
     */
    void setScan();

    /**
     * Sets the gun to adjust for the bot's turn when setting the gun turn rate. So the gun behaves
     * like it is turning independent of the bot's turn.
     *
     * <p>Ok, so this needs some explanation: The gun is mounted on the bot's body. So, normally, if
     * the bot turns 90 degrees to the right, then the gun will turn with it as it is mounted on top
     * of the bot's body. To compensate for this, you can adjust the gun for the bot's turn. When this
     * is set, the gun will turn independent from the bot's turn.
     *
     * <p>Note: This property is additive until you reach the maximum the gun can turn {@link
     * Constants#MAX_GUN_TURN_RATE}. The "adjust" is added to the amount, you set for turning the bot by the
     * turn rate, then capped by the physics of the game.
     *
     * <p>Note: The gun compensating this way does count as "turning the gun".
     *
     * @param adjust {@code true} if the gun must adjust/compensate for the bot's turn; {@code false}
     *               if the gun must turn with the bot's turn.
     * @see #setAdjustRadarForGunTurn(boolean)
     * @see #isAdjustGunForBodyTurn()
     */
    void setAdjustGunForBodyTurn(boolean adjust);

    /**
     * Checks if the gun is set to adjust for the bot turning, i.e. to turn independent from the bot's
     * body turn.
     *
     * <p>This call returns {@code true} if the gun is set to turn independent of the turn of the
     * bot's body. Otherwise, {@code false} is returned, meaning that the gun is set to turn with the
     * bot's body turn.
     *
     * @return {@code true} if the gun is set to turn independent of the bot turning; {@code false} if
     * the gun is set to turn with the bot turning.
     * @see #setAdjustGunForBodyTurn(boolean)
     */
    boolean isAdjustGunForBodyTurn();

    /**
     * Sets the radar to adjust for the gun's turn when setting the radar turn rate. So the radar
     * behaves like it is turning independent of the gun's turn.
     *
     * <p>Ok, so this needs some explanation: The radar is mounted on the gun. So, normally, if the
     * gun turns 90 degrees to the right, then the radar will turn with it as it is mounted on top of
     * the gun. To compensate for this, you can adjust the radar for the gun turn. When this is set,
     * the radar will turn independent from the gun's turn.
     *
     * <p>Note: This property is additive until you reach the maximum the radar can turn ({@link
     * Constants#MAX_RADAR_TURN_RATE}). The "adjust" is added to the amount, you set for turning the gun by the
     * gun turn rate, then capped by the physics of the game.
     *
     * <p>Note: The radar compensating this way does count as "turning the radar".
     *
     * @param adjust {@code true} if the radar must adjust/compensate for the gun's turn; {@code
     *               false} if the radar must turn with the gun's turn.
     * @see #setAdjustGunForBodyTurn(boolean)
     * @see #isAdjustRadarForGunTurn()
     */
    void setAdjustRadarForGunTurn(boolean adjust);

    /**
     * Checks if the radar is set to adjust for the gun turning, i.e. to turn independent from the
     * gun's turn.
     *
     * <p>This call returns {@code true} if the radar is set to turn independent of the turn of the
     * gun. Otherwise, {@code false} is returned, meaning that the radar is set to turn with the gun's
     * turn.
     *
     * @return {@code true} if the radar is set to turn independent of the gun turning; {@code false}
     * if the radar is set to turn with the gun turning.
     * @see #setAdjustRadarForGunTurn(boolean)
     */
    boolean isAdjustRadarForGunTurn();

    /**
     * Adds a event handler that will be automatically triggered {@link #onCustomEvent(CustomEvent)}
     * when the {@link Condition#test()} returns true.
     *
     * @param condition is the condition that must be met to trigger the custom event.
     * @see #removeCustomEvent(Condition)
     */
    void addCustomEvent(Condition condition);

    /**
     * Removes triggering an custom event handler for a specific condition that was previously added
     * with {@link #addCustomEvent(Condition)}.
     *
     * @param condition is the condition that was previously added with {@link
     *                  #addCustomEvent(Condition)}
     * @see #addCustomEvent(Condition)
     */
    void removeCustomEvent(Condition condition);

    /**
     * Set the bot to stop all movement including turning the gun and radar. The remaining movement is
     * saved for a call to {@link #setResume()}. This method has no effect, if it has already been
     * called.
     *
     * <p>This method will first be executed when {@link #go()} is called making it possible to call
     * other set methods before execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
     * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go()}.
     *
     * @see #setResume()
     */
    void setStop();

    /**
     * Sets the bot to scan (again) with the radar. This method is useful if the radar has not been
     * turning and thereby will not be able to automatically scan bots. This method is useful when the
     * robot movement has stopped, e.g. when {@link #setStop()} has been called. The last radar
     * direction and sweep angle will be used for rescanning for bots.
     *
     * <p>This method will first be executed when {@link #go()} is called making it possible to call
     * other set methods before execution. This makes it possible to set the bot to move, turn the
     * body, radar, gun, and also fire the gun in parallel in a single turn when calling {@link
     * #go()}. But notice that this is only possible to execute multiple methods in parallel by using
     * <strong>setter</strong> methods only prior to calling {@link #go()}.
     *
     * @see #setStop()
     */
    void setResume();

    /**
     * Checks if the movement has been stopped.
     *
     * @return true if the movement has been stopped by {@link #setStop()}; false otherwise.
     * @see #setResume()
     * @see #setStop()
     */
    boolean isStopped();

    /**
     * Returns the color of the body.
     * @return The color of the body.
     */
    Color getBodyColor();

    /**
     * Sets the color of the body. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setBodyColor(Color.RED); // the red color
     *     setBodyColor(new Color(255, 0, 0)); // also the red color
     *     setBodyColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param bodyColor is the new body color of the bot.
     */
    void setBodyColor(Color bodyColor);

    /**
     * Returns the color of the gun turret.
     * @return The color of the gun turret.
     */
    Color getTurretColor();

    /**
     * Sets the color of the gun turret. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setTurretColor(Color.RED); // the red color
     *     setTurretColor(new Color(255, 0, 0)); // also the red color
     *     setTurretColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param turretColor is the new gun turret color of the bot.
     */
    void setTurretColor(Color turretColor);

    /**
     * Returns the color of the radar.
     * @return The color of the radar.
     */
    Color getRadarColor();

    /**
     * Sets the color of the radar. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setRadarColor(Color.RED); // the red color
     *     setRadarColor(new Color(255, 0, 0)); // also the red color
     *     setRadarColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param radarColor is the new radar color of the bot.
     */
    void setRadarColor(Color radarColor);

    /**
     * Returns the color of the bullets when fired.
     * @return The color of the bullets when fired.
     */
    Color getBulletColor();

    /**
     * Sets the color of the bullets when fired. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setBulletColor(Color.RED); // the red color
     *     setBulletColor(new Color(255, 0, 0)); // also the red color
     *     setBulletColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param bulletColor is the new bullet color of fired bullets.
     */
    void setBulletColor(Color bulletColor);

    /**
     * Returns the color of the scan arc.
     * @return The color of the scan arc.
     */
    Color getScanColor();

    /**
     * Sets the color of the scan arc. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setScanColor(Color.RED); // the red color
     *     setScanColor(new Color(255, 0, 0)); // also the red color
     *     setScanColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param scanColor is the new scan arc color of the bot.
     */
    void setScanColor(Color scanColor);

    /**
     * Returns the color of the tracks.
     * @return The color of the tracks.
     */
    Color getTracksColor();

    /**
     * Sets the color of the tracks. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setTracksColor(Color.RED); // the red color
     *     setTracksColor(new Color(255, 0, 0)); // also the red color
     *     setTracksColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param tracksColor is the new tracks color of the bot.
     */
    void setTracksColor(Color tracksColor);

    /**
     * Returns the color of the gun.
     * @return The color of the gun.
     */
    Color getGunColor();

    /**
     * Sets the color of the gun. Colors can (only) be changed each turn.
     *
     * <p>Example:
     *
     * <pre><code class="language-java">
     *     setGunColor(Color.RED); // the red color
     *     setGunColor(new Color(255, 0, 0)); // also the red color
     *     setGunColor(Color.fromHexTriplet("F00"); // and also the red color
     * </code></pre>
     *
     * @param gunColor is the new gun color of the bot.
     */
    void setGunColor(Color gunColor);

    /**
     * The event handler triggered when connected to the server.
     *
     * @param connectedEvent is the event details from the game.
     */
    default void onConnected(ConnectedEvent connectedEvent) {
        System.out.println("Connected to: " + connectedEvent.getServerUri());
    }

    /**
     * The event handler triggered when disconnected from the server.
     *
     * @param disconnectedEvent is the event details from the game.
     */
    default void onDisconnected(DisconnectedEvent disconnectedEvent) {
        StringBuilder msg = new StringBuilder("Disconnected from: " + disconnectedEvent.getServerUri());
        if (disconnectedEvent.getStatusCode().isPresent()) {
            msg.append(", status code: ").append(disconnectedEvent.getStatusCode().get());
        }
        if (disconnectedEvent.getReason().isPresent()) {
            msg.append(", reason: ").append(disconnectedEvent.getReason().get());
        }
        System.out.println(msg);
    }

    /**
     * The event handler triggered when a connection error occurs.
     *
     * @param connectionErrorEvent is the event details from the game.
     */
    default void onConnectionError(ConnectionErrorEvent connectionErrorEvent) {
        String msg = "Connection error with " + connectionErrorEvent.getServerUri();
        String cause = null;
        if (connectionErrorEvent.getError() != null) {
            cause = connectionErrorEvent.getError().getMessage();
        }
        if (cause != null) {
            msg += ": " + cause;
        }
        System.err.println(msg);
    }

    /**
     * The event handler triggered when a game has started.
     *
     * @param gameStatedEvent is the event details from the game.
     */
    default void onGameStarted(GameStartedEvent gameStatedEvent) {
    }

    /**
     * The event handler triggered when a game has ended.
     *
     * @param gameEndedEvent is the event details from the game.
     */
    default void onGameEnded(GameEndedEvent gameEndedEvent) {
    }

    /**
     * The event handler triggered when a new round has started.
     *
     * @param roundStartedEvent is the event details from the game.
     */
    default void onRoundStarted(RoundStartedEvent roundStartedEvent) {
    }

    /**
     * The event handler triggered when a round has ended.
     *
     * @param roundEndedEvent is the event details from the game.
     */
    default void onRoundEnded(RoundEndedEvent roundEndedEvent) {
    }

    /**
     * The event handler triggered when a game tick event occurs, i.e., when a new turn in a round has
     * started.
     *
     * @param tickEvent is the event details from the game.
     */
    default void onTick(TickEvent tickEvent) {
    }

    /**
     * The event handler triggered when another bot has died.
     *
     * @param botDeathEvent is the event details from the game.
     */
    default void onBotDeath(DeathEvent botDeathEvent) {
    }

    /**
     * The event handler triggered when this bot has died.
     *
     * @param botDeathEvent is the event details from the game.
     */
    default void onDeath(DeathEvent botDeathEvent) {
    }

    /**
     * The event handler triggered when the bot has collided with another bot.
     *
     * @param botHitBotEvent is the event details from the game.
     */
    default void onHitBot(HitBotEvent botHitBotEvent) {
    }

    /**
     * The event handler triggered when the bot has hit a wall.
     *
     * @param botHitWallEvent is the event details from the game.
     */
    default void onHitWall(HitWallEvent botHitWallEvent) {
    }

    /**
     * The event handler triggered when the bot has fired a bullet.
     *
     * @param bulletFiredEvent is the event details from the game.
     */
    default void onBulletFired(BulletFiredEvent bulletFiredEvent) {
    }

    /**
     * The event handler triggered when the bot has been hit by a bullet.
     *
     * @param bulletHitBotEvent is the event details from the game.
     */
    default void onHitByBullet(BulletHitBotEvent bulletHitBotEvent) {
    }

    /**
     * The event handler triggered when the bot has hit another bot with a bullet.
     *
     * @param bulletHitBotEvent is the event details from the game.
     */
    default void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {
    }

    /**
     * The event handler triggered when a bullet fired from the bot has collided with another bullet.
     *
     * @param bulletHitBulletEvent is the event details from the game.
     */
    default void onBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) {
    }

    /**
     * The event handler triggered when a bullet has hit a wall.
     *
     * @param bulletHitWallEvent is the event details from the game.
     */
    default void onBulletHitWall(BulletHitWallEvent bulletHitWallEvent) {
    }

    /**
     * The event handler triggered when the bot has skipped a turn. This event occurs if the bot did
     * not take any action in a specific turn. That is, {@link #go()} was not called before the turn
     * timeout occurred for the turn. If the bot does not take action for multiple turns in a row, it
     * will receive a {@link SkippedTurnEvent} for each turn where it did not take action. When the
     * bot is skipping a turn, the server did not receive the message from the bot, and the server
     * will use the newest received instructions for target speed, turn rates, firing, etc.
     *
     * @param scannedBotEvent is the event details from the game.
     */
    default void onScannedBot(ScannedBotEvent scannedBotEvent) {
    }

    /**
     * The event handler triggered when the bot has skipped a turn. This event occurs if the bot did
     * not take any action in a specific turn. That is, Go() was not called before the turn timeout
     * occurred for the turn. If the bot does not take action for multiple turns in a row, it will
     * receive a SkippedTurnEvent for each turn where it did not take action. When the bot is skipping
     * a turn, the server did not receive the message from the bot, and the server will use the newest
     * received instructions for target speed, turn rates, firing, etc.
     *
     * @param skippedTurnEvent is the event details from the game.
     */
    default void onSkippedTurn(SkippedTurnEvent skippedTurnEvent) {
    }

    /**
     * The event handler triggered when the bot has won a round.
     *
     * @param wonRoundEvent is the event details from the game.
     */
    default void onWonRound(WonRoundEvent wonRoundEvent) {
    }

    /**
     * The event handler triggered when some condition has been met. Use the {@link
     * Condition#getName()} of the condition when you need to differentiate between different types of
     * conditions received with this event handler.
     *
     * @param customEvent is the event details from the game.
     */
    default void onCustomEvent(CustomEvent customEvent) {
    }

    /**
     * Calculates the maximum turn rate for a specific speed.
     *
     * @param speed is the speed.
     * @return The maximum turn rate determined by the given speed.
     */
    double calcMaxTurnRate(double speed);

    /**
     * Calculates the bullet speed given a fire power.
     *
     * @param firepower is the firepower.
     * @return The bullet speed determined by the given firepower.
     */
    double calcBulletSpeed(double firepower);

    /**
     * Calculates gun heat after having fired the gun.
     *
     * @param firepower is the firepower used when firing the gun.
     * @return The gun heat produced when firing the gun with the given firepower.
     */
    double calcGunHeat(double firepower);

    /**
     * Calculates the bearing (delta angle) between the input direction and the direction of this bot.
     *
     * <pre><code class="language-java">
     * bearing = calcBearing(direction) = normalizeRelativeDegrees(direction - getDirection())
     * </code></pre>
     *
     * @param direction is the input direction to calculate the bearing from.
     * @return A bearing (delta angle) between the input direction and the direction of this bot. The
     * bearing is a normalized angle in the range [-180,180[
     * @see #getDirection
     * @see #normalizeRelativeAngle
     */
    default double calcBearing(double direction) {
        return normalizeRelativeAngle(direction - getDirection());
    }

    /**
     * Calculates the bearing (delta angle) between the input direction and the direction of the gun.
     *
     * <pre><code class="language-java">
     * bearing = calcGunBearing(direction) = normalizeRelativeDegrees(direction - getGunDirection())
     * </code></pre>
     *
     * @param direction is the input direction to calculate the bearing from.
     * @return A bearing (delta angle) between the input direction and the direction of the gun. The
     * bearing is a normalized angle in the range [-180,180[
     * @see #getGunDirection
     * @see #normalizeRelativeAngle
     */
    default double calcGunBearing(double direction) {
        return normalizeRelativeAngle(direction - getGunDirection());
    }

    /**
     * Calculates the bearing (delta angle) between the input direction and the direction of the
     * radar.
     *
     * <pre><code class="language-java">
     * bearing = calcRadarBearing(direction) = normalizeRelativeDegrees(direction - getRadarDirection())
     * </code></pre>
     *
     * @param direction is the input direction to calculate the bearing from.
     * @return A bearing (delta angle) between the input direction and the direction of the radar. The
     * bearing is a normalized angle in the range [-180,180[
     * @see #getRadarDirection
     * @see #normalizeRelativeAngle
     */
    default double calcRadarBearing(double direction) {
        return normalizeRelativeAngle(direction - getRadarDirection());
    }

    /**
     * Calculates the direction (angle) from the bot's coordinates to a point x,y.
     *
     * @param x is the x coordinate of the point.
     * @param y is the y coordinate of the point.
     * @return The direction to the point x,y in the range [0,360[
     */
    default double directionTo(double x, double y) {
        return normalizeAbsoluteAngle(Math.toDegrees(Math.atan2(y - getY(), x - getX())));
    }

    /**
     * Calculates the bearing (delta angle) between the current direction of the bot's body and the
     * direction to the point x,y.
     *
     * @param x is the x coordinate of the point.
     * @param y is the y coordinate of the point.
     * @return The bearing to the point x,y in the range [-180,180[
     */
    default double bearingTo(double x, double y) {
        return normalizeRelativeAngle(directionTo(x, y) - getDirection());
    }

    /**
     * Calculates the bearing (delta angle) between the current direction of the bot's gun and the
     * direction to the point x,y.
     *
     * @param x is the x coordinate of the point.
     * @param y is the y coordinate of the point.
     * @return The bearing to the point x,y in the range [-180,180[
     */
    default double gunBearingTo(double x, double y) {
        return normalizeRelativeAngle(directionTo(x, y) - getGunDirection());
    }

    /**
     * Calculates the bearing (delta angle) between the current direction of the bot's radar and the
     * direction to the point x,y.
     *
     * @param x is the x coordinate of the point.
     * @param y is the y coordinate of the point.
     * @return The bearing to the point x,y in the range [-180,180[
     */
    default double radarBearingTo(double x, double y) {
        return normalizeRelativeAngle(directionTo(x, y) - getRadarDirection());
    }

    /**
     * Calculates the distance from the bots coordinates to a point x,y.
     *
     * @param x is the x coordinate of the point.
     * @param y is the y coordinate of the point.
     * @return The distance to the point x,y.
     */
    default double distanceTo(double x, double y) {
        return Math.hypot(x - getX(), y - getY());
    }

    /**
     * Normalizes an angle to an absolute angle into the range [0,360[
     *
     * @param angle is the angle to normalize.
     * @return The normalized absolute angle.
     */
    default double normalizeAbsoluteAngle(double angle) {
        return (angle %= 360) >= 0 ? angle : (angle + 360);
    }

    /**
     * Normalizes an angle to an relative angle into the range [-180,180[
     *
     * @param angle is the angle to normalize.
     * @return The normalized relative angle.
     */
    default double normalizeRelativeAngle(double angle) {
        return (angle %= 360) >= 0
                ? ((angle < 180) ? angle : (angle - 360))
                : ((angle >= -180) ? angle : (angle + 360));
    }

    /**
     * Calculates the difference between two angles, i.e. the number of degrees from a source angle to a target angle.
     * The delta angle will be in the range [-180,180]
     *
     * @param targetAngle is the target angle.
     * @param sourceAngle is the source angle.
     * @return The delta angle between a source angle and target angle.
     */
    default double calcDeltaAngle(double targetAngle, double sourceAngle) {
        double angle = targetAngle - sourceAngle;
        angle += (angle > 180) ? -360 : (angle < -180) ? 360 : 0;
        return angle;
    }
}
