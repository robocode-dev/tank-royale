package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

/** Event occurring when a bot has scanned another bot */
@Value
@EqualsAndHashCode(callSuper = true)
public class ScannedBotEvent extends GameEvent {
  /** ID of the bot did the scanning */
  int scannedByBotId;
  /** ID of the bot that was scanned */
  int scannedBotId;
  /** Energy level of the scanned bot */
  double energy;
  /** X coordinate of the scanned bot */
  double x;
  /** Y coordinate of the scanned bot */
  double y;
  /** Direction in degrees of the scanned bot */
  double direction;
  /** Speed measured in pixels per turn of the scanned bot */
  double speed;

  @Builder
  private ScannedBotEvent(
      int turnNumber,
      int scannedByBotId,
      int scannedBotId,
      double energy,
      double x,
      double y,
      double direction,
      double speed) {
    this.turnNumber = turnNumber;
    this.scannedByBotId = scannedByBotId;
    this.scannedBotId = scannedBotId;
    this.energy = energy;
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.speed = speed;
  }
}
