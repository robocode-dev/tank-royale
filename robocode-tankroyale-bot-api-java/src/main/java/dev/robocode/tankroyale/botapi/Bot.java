package dev.robocode.tankroyale.botapi;

import java.net.URI;

/**
 * Abstract bot class provides convenient methods for movement, turning, and firing the gun. Most
 * bots should inherit from this class.
 */
@SuppressWarnings("unused")
public abstract class Bot extends BaseBot implements IBot {

  private final BotInternals __internals = new BotInternals(this, super.__internals.botEvents);

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * both BotInfo and server URL is provided through environment variables, i.e., when starting up
   * the bot using a bootstrap. These environment variables must be set to provide the server URL
   * and bot information, and are automatically set by the bootstrap tool for Robocode.
   *
   * <p><b>Example of how to set the predefined environment variables:</b>
   *
   * <p>ROBOCODE_SERVER_URL=ws://localhost<br>
   * BOT_NAME=MyBot<br>
   * BOT_VERSION=1.0<br>
   * BOT_AUTHOR=fnl<br>
   * BOT_DESCRIPTION=Sample bot<br>
   * BOT_URL=https://mybot.somewhere.net
   * BOT_COUNTRY_CODE=DK<br>
   * BOT_GAME_TYPES=melee,1v1<br>
   * BOT_PLATFORM=Java<br>
   * BOT_PROG_LANG=Java 8<br>
   */
  public Bot() {
    super();
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used when
   * server URL is provided through the environment variable ROBOCODE_SERVER_URL.
   *
   * @param botInfo is the bot info containing information about your bot.
   */
  public Bot(final BotInfo botInfo) {
    super(botInfo);
  }

  /**
   * Constructor for initializing a new instance of the BaseBot class, which should be used
   * providing both the bot information and server URL for your bot.
   *
   * @param botInfo is the bot info containing information about your bot.
   * @param serverUrl is the server URL
   */
  public Bot(final BotInfo botInfo, URI serverUrl) {
    super(botInfo, serverUrl);
  }

  /** {@inheritDoc} */
  @Override
  public final boolean isRunning() {
    return __internals.isRunning;
  }

  /** {@inheritDoc} */
  @Override
  public final void setForward(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    __internals.distanceRemaining = distance;
  }

  /** {@inheritDoc} */
  @Override
  public final void forward(double distance) {
    setForward(distance);
    go();
    __internals.awaitMovementComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setBack(double distance) {
    if (Double.isNaN(distance)) {
      throw new IllegalArgumentException("distance cannot be NaN");
    }
    __internals.distanceRemaining = -distance;
  }

  /** {@inheritDoc} */
  @Override
  public final void back(double distance) {
    setBack(distance);
    go();
    __internals.awaitMovementComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getDistanceRemaining() {
    return __internals.distanceRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxSpeed(double maxSpeed) {
    if (maxSpeed < 0) {
      maxSpeed = 0;
    } else if (maxSpeed > MAX_SPEED) {
      maxSpeed = MAX_SPEED;
    }
    __internals.maxSpeed = maxSpeed;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.turnRemaining = degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnLeft(double degrees) {
    setTurnLeft(degrees);
    go();
    __internals.awaitTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.turnRemaining = -degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnRight(double degrees) {
    setTurnRight(degrees);
    go();
    __internals.awaitTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getTurnRemaining() {
    return __internals.turnRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxTurnRate(double maxTurnRate) {
    if (maxTurnRate < 0) {
      maxTurnRate = 0;
    } else if (maxTurnRate > MAX_TURN_RATE) {
      maxTurnRate = MAX_TURN_RATE;
    }
    __internals.maxTurnRate = maxTurnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnGunLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.gunTurnRemaining = degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnGunLeft(double degrees) {
    setTurnGunLeft(degrees);
    go();
    __internals.awaitGunTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnGunRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.gunTurnRemaining = -degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnGunRight(double degrees) {
    setTurnGunRight(degrees);
    go();
    __internals.awaitGunTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getGunTurnRemaining() {
    return __internals.gunTurnRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxGunTurnRate(double maxGunTurnRate) {
    if (maxGunTurnRate < 0) {
      maxGunTurnRate = 0;
    } else if (maxGunTurnRate > MAX_GUN_TURN_RATE) {
      maxGunTurnRate = MAX_GUN_TURN_RATE;
    }
    __internals.maxGunTurnRate = maxGunTurnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRadarLeft(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.radarTurnRemaining = degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnRadarLeft(double degrees) {
    setTurnRadarLeft(degrees);
    go();
    __internals.awaitRadarTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final void setTurnRadarRight(double degrees) {
    if (Double.isNaN(degrees)) {
      throw new IllegalArgumentException("degrees cannot be NaN");
    }
    __internals.radarTurnRemaining = -degrees;
  }

  /** {@inheritDoc} */
  @Override
  public final void turnRadarRight(double degrees) {
    setTurnRadarRight(degrees);
    go();
    __internals.awaitRadarTurnComplete();
  }

  /** {@inheritDoc} */
  @Override
  public final double getRadarTurnRemaining() {
    return __internals.radarTurnRemaining;
  }

  /** {@inheritDoc} */
  @Override
  public final void setMaxRadarTurnRate(double maxRadarTurnRate) {
    if (maxRadarTurnRate < 0) {
      maxRadarTurnRate = 0;
    } else if (maxRadarTurnRate > MAX_RADAR_TURN_RATE) {
      maxRadarTurnRate = MAX_RADAR_TURN_RATE;
    }
    __internals.maxRadarTurnRate = maxRadarTurnRate;
  }

  /** {@inheritDoc} */
  @Override
  public final void fire(double firepower) {
    setFirepower(firepower);
    go();
    setFirepower(0); // No more firing!
  }

  /** {@inheritDoc} */
  @Override
  public void stop() {
    __internals.stop();
  }

  /** {@inheritDoc} */
  @Override
  public void resume() {
    __internals.resume();
  }
}