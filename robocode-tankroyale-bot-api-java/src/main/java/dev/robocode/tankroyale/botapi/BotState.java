package dev.robocode.tankroyale.botapi;

/** Current bot state */
@SuppressWarnings("unused")
public final class BotState {

  /** Energy level */
  private final double energy;

  /** X coordinate */
  private final double x;

  /** Y coordinate */
  private final double y;

  /** Driving direction in degrees */
  private final double direction;

  /** Gun direction in degrees */
  private final double gunDirection;

  /** Radar direction in degrees */
  private final double radarDirection;

  /** Radar sweep angle in degrees */
  private final double radarSweep;

  /** Speed measured in pixels per turn */
  private final double speed;

  /** Gun heat */
  private final double gunHeat;

  /**
   * Initializes a new instance of the BotState class.
   *
   * @param energy is the energy level.
   * @param x is the X coordinate.
   * @param y is the Y coordinate.
   * @param direction is the driving direction in degrees.
   * @param gunDirection is the gun direction in degrees.
   * @param radarDirection is the radar direction in degrees.
   * @param radarSweep is the radar sweep angle in degrees.
   * @param speed is the speed measured in pixels per turn.
   * @param gunHeat is the gun heat.
   */
  public BotState(
      double energy,
      double x,
      double y,
      double direction,
      double gunDirection,
      double radarDirection,
      double radarSweep,
      double speed,
      double gunHeat) {
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.gunDirection = gunDirection;
    this.radarDirection = radarDirection;
    this.radarSweep = radarSweep;
    this.speed = speed;
    this.gunHeat = gunHeat;
  }

  /**
   * Returns the energy level of the bot. The energy level is typically starting at 100. The bot
   * gains more energy when hitting other bots, and loses energy by getting hit and when spending
   * energy on firing bullets. When the energy reaches 0, the bot becomes disabled and will not be
   * able to take any new action. It might become active again, if one of its bullets hit another
   * bot, meaning that the bot gains new energy.
   *
   * @return the energy level.
   */
  public double getEnergy() {
    return energy;
  }

  /**
   * Returns the X coordinate of the bot, which is in the center of the bot.
   *
   * @return the X coordinate.
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the Y coordinate of the bot, which is in the center of the bot.
   *
   * @return the Y coordinate.
   */
  public double getY() {
    return y;
  }

  /**
   * Returns the driving direction of the bot in degrees.
   *
   * @return the driving direction.
   */
  public double getDirection() {
    return direction;
  }

  /**
   * Returns the gun direction of the bot in degrees.
   *
   * @return the gun direction.
   */
  public double getGunDirection() {
    return gunDirection;
  }

  /**
   * Returns the radar direction of the bot in degrees.
   *
   * @return the radar direction.
   */
  public double getRadarDirection() {
    return radarDirection;
  }

  /**
   * Returns the radar sweep angle in degrees, i.e. delta angle between previous and current radar
   * direction.
   *
   * @return the radar sweep angle.
   */
  public double getRadarSweep() {
    return radarSweep;
  }

  /**
   * Returns the speed measured in units per turn.
   *
   * @return the speed.
   */
  public double getSpeed() {
    return speed;
  }

  /**
   * Returns the gun heat. When firing the gun, it will be heated up. The gun will need to cool down
   * before it can fire another bullet. When the gun heat is zero, the gun will be able to fire
   * again.
   *
   * @return the gun heat.
   */
  public double getGunHeat() {
    return gunHeat;
  }
}
