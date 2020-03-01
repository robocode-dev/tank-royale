package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.*;

import java.util.Collection;

/** Interface containing the core API for a bot. */
@SuppressWarnings({"UnusedDeclaration", "EmptyMethod"})
public interface IBaseBot {

  /**
   * The radius of the bounding circle of the bot, which is a constant of {@value} units.
   *
   * <p>The bounding circle of a bot is a circle going from the center of the bot with a radius so
   * that the circle covers most of the bot. The bounding circle is used for determining when a bot
   * is hit by a bullet.
   *
   * <p>A bot gets hit by a bullet when the bullet gets inside the bounding circle, i.e. the
   * distance between the bullet and the center of the bounding circle is less than the radius of
   * the bounding circle.
   */
  short BOUNDING_CIRCLE_RADIUS = 18;

  /**
   * The radius of the radar's scan beam, which is a constant of {@value} units.
   *
   * <p>The radar is used for scanning the battlefield for opponent bots. The shape of the scan beam
   * of the radar is a circle arc ("pizza slice") starting from the center of the bot. Opponent bots
   * that get inside the scan arc will be detected by the radar.
   *
   * <p>The radius of the arc is a constant of {@value} units. This means that that the radar will
   * not be able to detect bots that are more than {@value} units away from the bot.
   *
   * <p>The radar needs to be turned (left or right) to scan opponent bots. So make sure the radar
   * is always turned. The more the radar is turned, the larger the area of the scan arc becomes,
   * and the bigger the chance is that the radar detects an opponent. If the radar is not turning,
   * the scan arc becomes a thin line, unable to scan and detect anything.
   */
  short SCAN_RADIUS = 1200;

  /**
   * The maximum possible driving turn rate, which is max. {@value} degrees per turn.
   *
   * <p>This is the max. possible turn rate of the bot. Note that the speed of the bot has a direct
   * impact on the turn rate. The faster the speed the less turn rate.
   *
   * <p>The formula for the max. possible turn rate at a given speed is: MaxTurnRate - 0.75 x
   * abs(speed). Hence, the turn rate is at max. 10 degrees/turn when the speed is zero, and down to
   * only 4 degrees per turn when the robot is at max speed (which is 8 units per turn).
   */
  short MAX_TURN_RATE = 10;

  /** The maximum gun turn rate, which is a constant of 20 degrees per turn. */
  short MAX_GUN_TURN_RATE = 20;

  /** The maximum radar turn rate, which is a constant of 45 degrees per turn. */
  short MAX_RADAR_TURN_RATE = 45;

  /** The maximum absolute speed, which is 8 units per turn. */
  short MAX_SPEED = 8;

  /** The maximum backward speed, which is -8 units per turn. */
  short MAX_FORWARD_SPEED = MAX_SPEED;

  /** The minimum forward speed, which is -8 units per turn. */
  short MAX_BACKWARD_SPEED = -MAX_SPEED;

  /**
   * The minimum firepower, which is 0.1. The gun will not fire with a power that is less than the
   * minimum firepower, which is 0.1.
   */
  double MIN_FIREPOWER = 0.1;

  /**
   * The maximum firepower, which is 3. The gun will fire up to this power, even if the firepower is
   * set to a higher value.
   */
  short MAX_FIREPOWER = 3;

  /**
   * The minimum bullet speed is 11 units per turn.
   *
   * <p>The minimum bullet speed is the slowest possible speed that a bullet can travel and is
   * defined by the maximum firepower: 20 - 3 x max. firepower, i.e. 20 - 3 x 3 = 11. The more
   * power, the slower the bullet speed will be.
   */
  short MIN_BULLET_SPEED = 20 - 3 * MAX_FIREPOWER;

  /**
   * The maximum bullet speed is 19.7 units per turn.
   *
   * <p>The maximum bullet speed is the fastest possible speed that a bullet can travel and is
   * defined by the minimum firepower. Max. bullet speed = 20 - 3 x min. firepower, i.e. 20 - 3 x
   * 0.1 = 19.7. The lesser power, the faster the bullet speed will be.
   */
  double MAX_BULLET_SPEED = 20 - 3 * MIN_FIREPOWER;

  /**
   * Acceleration is the increase in speed per turn, which adds 1 unit to the speed per turn when
   * the bot is increasing its speed moving forward.
   */
  short ACCELERATION = 1;

  /**
   * Deceleration is the decrease in speed per turn, which subtracts 2 units to the speed per turn
   * when the bot is decreasing its speed moving backward. This means that a bot is faster at
   * braking than accelerating forward.
   */
  short DECELERATION = -2;

  /**
   * The method used to start running the bot. You should call this method from the main method or
   * similar.
   *
   * <p>Example:
   *
   * <pre>{@code
   * public void main(String[] args) {
   *     // create myBot
   *     ...
   *     myBot.start();
   * }
   * }</pre>
   */
  void start();

  /**
   * Commits the current actions for the current turn.
   *
   * <p>This method must be called once per turn to send the bot actions to the server and must be
   * called before the turn timeout occurs. The turn timeout is started when the {@link
   * GameStartedEvent} and {@link TickEvent} occurs. If the {@link #go} method is called too late, a
   * {@link SkippedTurnEvent} will occur.
   *
   * <p>Actions are that the bot needs to execute by calling {@link #go} are set by calling the
   * various setter methods prior to calling {@link #go}: {@link #setTurnRate}, {@link
   * #setGunTurnRate}, {@link #setRadarTurnRate}, {@link #setTargetSpeed}, and {@link
   * #setFirepower}.
   */
  void go();

  /** Unique id of this bot in the battle. Available when game has started. */
  int getMyId();

  /** Game variant, e.g. "Tank Royale" for Robocode Tank Royale. */
  String getVariant();

  /** Game version, e.g. "1.0.0" */
  String getVersion();

  /**
   * Game type, e.g. "melee".
   *
   * <p>Available when game has started.
   */
  String getGameType();

  /**
   * Width of the arena measured in pixels.
   *
   * <p>Available when game has started.
   */
  int getArenaWidth();

  /**
   * Height of the arena measured in pixels.
   *
   * <p>Available when game has started.
   */
  int getArenaHeight();

  /**
   * Number of rounds in a battle.
   *
   * <p>Available when game has started.
   */
  int getNumberOfRounds();

  /**
   * Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun is able to
   * fire. The gun cooling rate determines how fast the gun cools down. That is, the gun cooling
   * rate is subtracted from the gun heat each turn until the gun heat reaches zero.
   *
   * <p>Available when game has started.
   *
   * @see #getGunHeat()
   */
  double getGunCoolingRate();

  /**
   * Maximum number of inactive turns allowed, where a bot does not take any action before it is
   * zapped by the game.
   *
   * <p>Available when game has started.
   */
  int getMaxInactivityTurns();

  /**
   * Turn timeout in microseconds (1 / 1,000,000 second). The turn timeout is important as the bot
   * need to take action by calling go() before the turn timeout occurs. As soon as the TickEvent is
   * triggered, i.e. when onTick() is called, you need to call go() to take action before the turn
   * timeout occurs. Otherwise your bot will receive SkippedTurnEvent(s).
   *
   * <p>Available when game has started.
   *
   * @see #getTimeLeft()
   * @see #go()
   */
  int getTurnTimeout();

  /**
   * Number of microseconds left for this round before the bot will skip the turn. Make sure to call
   * go() before the time runs out.
   *
   * @see #getTurnTimeout()
   * @see #go()
   */
  int getTimeLeft();

  /** Current round number. */
  int getRoundNumber();

  /** Current turn number. */
  int getTurnNumber();

  /**
   * Current energy level. When positive, the bot is alive and active. When 0, the bot is alive, but
   * disabled, meaning that it will not be able to move. If negative, the bot has been defeated.
   */
  double getEnergy();

  /**
   * Flag specifying if the bot is disabled, i.e. when the energy is zero. When the bot is disabled,
   * it it is not able to take any action like movement, turning and firing.
   */
  boolean isDisabled();

  /** X coordinate of the center of the bot. */
  double getX();

  /** Y coordinate of the center of the bot. */
  double getY();

  /** Driving direction of the body in degrees. */
  double getDirection();

  /** Gun direction in degrees. */
  double getGunDirection();

  /** Radar direction in degrees. */
  double getRadarDirection();

  /**
   * Speed measured in pixels per turn. If the speed is positive, the bot moves forward. If
   * negative, the bot moves backwards. A zero speed means that the bot is not moving from its
   * current position.
   */
  double getSpeed();

  /**
   * Gun heat. The gun gets heated when it is fired, and will first be able to fire again, when the
   * gun has cooled down, meaning that the gun heat must be zero.
   *
   * <p>When the gun is fired the gun heat is set to 1 + (firepower / 5). The gun is cooled down by
   * the gun cooling rate.
   *
   * @see #getGunCoolingRate()
   */
  double getGunHeat();

  /** Current bullet states. */
  Collection<BulletState> getBulletStates();

  /** Game events received for the current turn. */
  Collection<? extends Event> getEvents();

  /**
   * Sets the new turn rate of the body in degrees per turn (can be positive and negative). The turn
   * rate is added to the current turn direction of the body. But it is also added to the current
   * direction of the gun and radar. This is because the gun is mounted on the body, and hence turns
   * with the body. The radar is mounted on the gun, and hence moves with the gun. By subtracting
   * the turn rate of the body from the turn rate of the gun and radar, you can compensate for the
   * turn rate of the body. But be aware that the turn limits for the gun and radar cannot be
   * exceeded.
   *
   * <p>The turn rate is truncated to {@link #MAX_TURN_RATE} if the turn rate exceeds this value.
   *
   * <p>If this property is set multiple times, the last value set before go() counts.
   *
   * @param turnRate is the new turn rate of the body in degrees per turn.
   */
  void setTurnRate(double turnRate);

  /**
   * Returns the turn rate in degrees per turn.
   *
   * @see #setTurnRate(double)
   */
  double getTurnRate();

  /**
   * Sets the new turn rate of the gun in degrees per turn (can be positive and negative). The turn
   * rate is added to the current turn direction of the gun. But it is also added to the current
   * direction of the radar. This is because the radar is mounted on the gun, and hence moves with
   * the gun. You can compensate for this by subtracting the turn rate of the gun and body from the
   * turn rate of the radar. And you can compensate the turn rate of the body on the gun by
   * subtracting the turn rate of the body from the turn rate of the gun. But be aware that the turn
   * limits for the radar (and also body and gun) cannot be exceeded.
   *
   * <p>The gun turn rate is truncated to {@link #MAX_GUN_TURN_RATE} if the gun turn rate exceeds
   * this value.
   *
   * <p>If this property is set multiple times, the last value set before go() counts.
   *
   * @param gunTurnRate is the new turn rate of the gun in degrees per turn.
   */
  void setGunTurnRate(double gunTurnRate);

  /**
   * Returns the gun turn rate in degrees per turn.
   *
   * @see #setGunTurnRate(double)
   */
  double getGunTurnRate();

  /**
   * Sets the new turn rate of the radar in degrees per turn (can be positive and negative). The
   * turn rate is added to the current turn direction of the radar. Note that beside the turn rate
   * of the radar, the turn rates of the body and gun is also added to the radar direction, as the
   * radar moves with the gun, which is mounted on the gun that moves with the body. You can
   * compensate for this by subtracting the turn rate of the body and gun from the turn rate of the
   * radar. But be aware that the turn limits for the radar (and also body and gun) cannot be
   * exceeded.
   *
   * <p>The radar turn rate is truncated to {@link #MAX_RADAR_TURN_RATE} if the radar turn rate
   * exceeds this value.
   *
   * <p>If this property is set multiple times, the last value set before go() counts.
   *
   * @param gunRadarTurnRate is the new turn rate of the radar in degrees per turn.
   */
  void setRadarTurnRate(double gunRadarTurnRate);

  /**
   * Returns the radar turn rate in degrees per turn.
   *
   * @see #setRadarTurnRate(double)
   */
  double getRadarTurnRate();

  /**
   * Sets the new target speed for the bot in units per turn. The target speed is the speed you want
   * to achieve eventually, which could take one to several turns to achieve depending on the
   * current speed. For example, if the bot is moving forward with max speed, and then must change
   * to move backwards at full speed, the bot will need to first decelerate/brake its positive speed
   * (moving forward). When passing a speed of zero, it will then need to accelerate backwards to
   * achieve max negative speed.
   *
   * <p>Note that acceleration is 1 pixel/turn and deceleration/braking is faster than acceleration
   * as it is -2 pixel/turn. Deceleration is negative as it is added to the speed and hence needs to
   * be negative.
   *
   * <p>The target speed is truncated to {@link #MAX_SPEED} if the target speed exceeds this value.
   *
   * <p>If this property is set multiple times, the last value set before go() counts.
   *
   * @param targetSpeed is the new target speed in units per turn.
   */
  void setTargetSpeed(double targetSpeed);

  /**
   * Returns the target speed in units per turn.
   *
   * @see #setTargetSpeed(double)
   */
  double getTargetSpeed();

  /**
   * Sets the gun to fire in the direction as the gun is pointing with the specified firepower.
   *
   * <p>Firepower is the amount of energy spend on firing the gun. You cannot spend more energy that
   * available from the bot. The amount of energy loss is equal to the firepower.
   *
   * <p>The bullet power must be greater than {@link #MIN_FIREPOWER} and the gun heat zero before
   * the gun is able to fire.
   *
   * <p>If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
   * another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
   *
   * <p>The gun will only fire when the firepower is at {@link #MIN_FIREPOWER} or higher. If the
   * firepower is more than {@link #MAX_FIREPOWER}, the power will be truncated to the max
   * firepower.
   *
   * <p>Whenever the gun is fired, the gun is heated an needs to cool down before it is able to fire
   * again. The gun heat must be zero before the gun is able to fire (see {@link #getGunHeat()}).
   * The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used
   * the longer it takes to cool down the gun. The gun cooling rate can be read by calling {@link
   * #getGunCoolingRate()}.
   *
   * <p>The amount of energy used for firing the gun is subtracted from the bots total energy. The
   * amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
   * greater than 1 it will do an additional 2 x (firepower - 1) damage.
   *
   * <p>Note that the gun will automatically keep firing at any turn when the gun heat reaches zero.
   * It is possible disable the gun firing by setting the firepower on this property to zero.
   *
   * <p>The firepower is truncated to 0 and {@link #MAX_FIREPOWER} if the firepower exceeds these
   * values.
   *
   * <p>If this property is set multiple times, the last value set before go() counts.
   *
   * @param firepower is the amount of energy spend on firing the gun. You cannot spend more energy
   *     that available from the bot. The bullet power must be &gt; {@link #MIN_FIREPOWER} and the
   *     gun heat zero before the gun is able to fire.
   * @see #onBulletFired(BulletFiredEvent)
   * @see #getGunHeat()
   * @see #getGunCoolingRate()
   */
  void setFirepower(double firepower);

  /**
   * Returns the set firepower.
   *
   * @see #setFirepower(double)
   */
  double getFirepower();

  /**
   * Sets the gun to adjust for the bot's turn when setting the gun turn rate. So the gun behaves
   * like it is turning independent of the bot's turn.
   *
   * <p>Ok, so this needs some explanation: The gun is mounted on the bot's body. So, normally, if
   * the bot turns 90 degrees to the right, then the gun will turn with it as it is mounted on top
   * of the bot's body. To compensate for this, you can adjust the gun for the body turn. When this
   * is set, the gun will turn independent from the bot's turn.
   *
   * <p>Note: This property is additive until you reach the maximum the gun can turn
   * MAX_GUN_TURN_RATE. The "adjust" is added to the amount, you set for turning the bot by the turn
   * rate, then capped by the physics of the game.
   *
   * <p>Note: The gun compensating this way does count as "turning the gun".
   *
   * @param adjust {@code true} if the gun must adjust/compensate for the bot's turn; {@code false}
   *     if the gun must turn with the bot's turn.
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
   *     the gun is set to turn with the bot turning
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
   * #MAX_RADAR_TURN_RATE}). The "adjust" is added to the amount, you set for turning the gun by the
   * gun turn rate, then capped by the physics of the game.
   *
   * <p>Note: The radar compensating this way does count as "turning the radar".
   *
   * @param adjust {@code true} if the radar must adjust/compensate for the gun's turn; {@code
   *     false} if the radar must turn with the gun's turn.
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
   *     if the radar is set to turn with the gun turning
   * @see #setAdjustRadarForGunTurn(boolean)
   */
  boolean isAdjustRadarForGunTurn();

  /**
   * Event handler triggered when connected to server
   *
   * @param connectedEvent is the event details from the game
   */
  default void onConnected(ConnectedEvent connectedEvent) {}

  /**
   * Event handler triggered when disconnected from server
   *
   * @param disconnectedEvent is the event details from the game
   */
  default void onDisconnected(DisconnectedEvent disconnectedEvent) {}

  /**
   * Event handler triggered when a connection error occurs
   *
   * @param connectionErrorEvent is the event details from the game
   */
  default void onConnectionError(ConnectionErrorEvent connectionErrorEvent) {}

  /**
   * Event handler triggered when game has started
   *
   * @param gameStatedEvent is the event details from the game
   */
  default void onGameStarted(GameStartedEvent gameStatedEvent) {}

  /**
   * Event handler triggered when game has ended
   *
   * @param gameEndedEvent is the event details from the game
   */
  default void onGameEnded(GameEndedEvent gameEndedEvent) {}

  /**
   * Event handler triggered when a game tick event occurs, i.e. when a new turn in a round has
   * started. When this handler is triggered, your bot must figure out the next action to take and
   * call go() when it needs to commit the action to the server.
   *
   * @param tickEvent is the event details from the game
   */
  default void onTick(TickEvent tickEvent) {}

  /**
   * Event handler triggered when another bot has died
   *
   * @param botDeathEvent is the event details from the game
   */
  default void onBotDeath(BotDeathEvent botDeathEvent) {}

  /**
   * Event handler triggered when this bot has died
   *
   * @param botDeathEvent is the event details from the game
   */
  default void onDeath(BotDeathEvent botDeathEvent) {}

  /**
   * Event handler triggered when the bot has collided with another bot
   *
   * @param botHitBotEvent is the event details from the game
   */
  default void onHitBot(BotHitBotEvent botHitBotEvent) {}

  /**
   * Event handler triggered when the bot has hit a wall
   *
   * @param botHitWallEvent is the event details from the game
   */
  default void onHitWall(BotHitWallEvent botHitWallEvent) {}

  /**
   * Event handler triggered when the bot has fired a bullet
   *
   * @param bulletFiredEvent is the event details from the game
   */
  default void onBulletFired(BulletFiredEvent bulletFiredEvent) {}

  /**
   * Event handler triggered when the bot has been hit by a bullet
   *
   * @param bulletHitBotEvent is the event details from the game
   */
  default void onHitByBullet(BulletHitBotEvent bulletHitBotEvent) {}

  /**
   * Event handler triggered when the bot has hit another bot with a bullet
   *
   * @param bulletHitBotEvent is the event details from the game
   */
  default void onBulletHit(BulletHitBotEvent bulletHitBotEvent) {}

  /**
   * Event handler triggered a bullet has collided with another bullet
   *
   * @param bulletHitBulletEvent is the event details from the game
   */
  default void onBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) {}

  /**
   * Event handler triggered a bullet has a wall
   *
   * @param bulletHitWallEvent is the event details from the game
   */
  default void onBulletHitWall(BulletHitWallEvent bulletHitWallEvent) {}

  /**
   * Event handler triggered when the bot has scanned another bot
   *
   * @param scannedBotEvent is the event details from the game
   */
  default void onScannedBot(ScannedBotEvent scannedBotEvent) {}

  /**
   * Event handler triggered when the bot has skipped a turn. This event occurs if the bot did not
   * take any action in a specific turn. That is, go() was not called before the turn timeout
   * occurred for the turn. If the bot does not take action for multiple turns in a row, it will
   * receive a SkippedTurnEvent for each turn where it did not take action. When the bot is skipping
   * a turn the server did not receive the message from the bot, and the server will use the newest
   * received instructions for target speed, turn rates, firing etc.
   *
   * @param skippedTurnEvent is the event details from the game
   */
  default void onSkippedTurn(SkippedTurnEvent skippedTurnEvent) {}

  /**
   * Event handler triggered when the bot has won a round
   *
   * @param wonRoundEvent is the event details from the game
   */
  default void onWonRound(WonRoundEvent wonRoundEvent) {}

  /**
   * Calculates the maximum turn rate for a specific speed.
   *
   * @param speed is the speed
   * @return maximum turn rate determined by the given speed
   */
  double calcMaxTurnRate(double speed);

  /**
   * Calculates the bullet speed given a fire power.
   *
   * @param firepower is the firepower
   * @return bullet speed determined by the given firepower
   */
  double calcBulletSpeed(double firepower);

  /**
   * Calculates gun heat after having fired the gun.
   *
   * @param firepower is the firepower used when firing the gun
   * @return gun heat produced when firing the gun with the given firepower
   */
  double calcGunHeat(double firepower);

  /**
   * Normalizes an angle to an absolute angle into the range [0,360[
   *
   * @param angle is the angle to normalize
   * @return the normalized absolute angle
   */
  default double normalizeAbsoluteDegrees(double angle) {
    return (angle %= 360) >= 0 ? angle : (angle + 360);
  }

  /**
   * Normalizes an angle to an relative angle into the range [-180,180[
   *
   * @param angle is the angle to normalize
   * @return the normalized relative angle.
   */
  default double normalizeRelativeDegrees(double angle) {
    return (angle %= 360) >= 0
        ? ((angle < 180) ? angle : (angle - 360))
        : ((angle >= -180) ? angle : (angle + 360));
  }
}
