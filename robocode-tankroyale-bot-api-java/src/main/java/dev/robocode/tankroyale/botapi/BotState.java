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

  /** Radar sweep angle in degrees, i.e. angle between previous and current radar direction */
  private final double radarSweep;

  /** Speed measured in pixels per turn */
  private final double speed;

  /** Gun heat */
  private final double gunHeat;

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

  /** Returns the energy level */
  public double getEnergy() {
    return energy;
  }

  /** Returns the X coordinate */
  public double getX() {
    return x;
  }

  /** Returns the Y coordinate */
  public double getY() {
    return y;
  }

  /** Returns the driving direction in degrees */
  public double getDirection() {
    return direction;
  }

  /** Returns the gun direction in degrees */
  public double getGunDirection() {
    return gunDirection;
  }

  /** Returns the radar direction in degrees */
  public double getRadarDirection() {
    return radarDirection;
  }

  /**
   * Returns the radar sweep angle in degrees, i.e. angle between previous and current radar
   * direction
   */
  public double getRadarSweep() {
    return radarSweep;
  }

  /** Returns the speed measured in pixels per turn */
  public double getSpeed() {
    return speed;
  }

  /** Returns the gun heat */
  public double getGunHeat() {
    return gunHeat;
  }
}
