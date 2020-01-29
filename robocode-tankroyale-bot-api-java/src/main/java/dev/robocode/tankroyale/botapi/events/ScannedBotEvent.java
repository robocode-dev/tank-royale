package dev.robocode.tankroyale.botapi.events;

/** Event occurring when a bot has scanned another bot */
@SuppressWarnings("unused")
public final class ScannedBotEvent extends Event {

  /** ID of the bot did the scanning */
  private final int scannedByBotId;

  /** ID of the bot that was scanned */
  private final int scannedBotId;

  /** Energy level of the scanned bot */
  private final double energy;

  /** X coordinate of the scanned bot */
  private final double x;

  /** Y coordinate of the scanned bot */
  private final double y;

  /** Direction in degrees of the scanned bot */
  private final double direction;

  /** Speed measured in pixels per turn of the scanned bot */
  private final double speed;

  public ScannedBotEvent(
      int turnNumber,
      int scannedByBotId,
      int scannedBotId,
      double energy,
      double x,
      double y,
      double direction,
      double speed) {
    super(turnNumber);
    this.scannedByBotId = scannedByBotId;
    this.scannedBotId = scannedBotId;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.speed = speed;
  }

  /** Returns the ID of the bot did the scanning */
  public int getScannedByBotId() {
    return scannedByBotId;
  }

  /** Returns the ID of the bot that was scanned */
  public int getScannedBotId() {
    return scannedBotId;
  }

  /** Returns the energy level of the scanned bot */
  public double getEnergy() {
    return energy;
  }

  /** Returns the X coordinate of the scanned bot */
  public double getX() {
    return x;
  }

  /** Returns the Y coordinate of the scanned bot */
  public double getY() {
    return y;
  }

  /** Returns the direction in degrees of the scanned bot */
  public double getDirection() {
    return direction;
  }

  /** Returns the Speed measured in pixels per turn of the scanned bot */
  public double getSpeed() {
    return speed;
  }
}
