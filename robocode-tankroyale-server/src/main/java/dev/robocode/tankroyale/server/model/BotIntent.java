package dev.robocode.tankroyale.server.model;

import lombok.Builder;
import lombok.Value;

/**
 * Mutable bot intent. A bot intent is updated by a bot between turns. The bot intent reflects the
 * bot's wishes/orders for new target speed, turn rates, bullet power etc.
 *
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder = true)
public class BotIntent {

  /** Desired speed */
  Double targetSpeed;

  /** Desired driving turn rate */
  Double turnRate;

  /** Desired gun turn rate */
  Double gunTurnRate;

  /** Desired radar turn rate */
  Double radarTurnRate;

  /** Desired bullet power */
  Double bulletPower;

  /** Body color */
  String bodyColor;

  /** Gun turret color */
  String turretColor;

  /** Radar color */
  String radarColor;

  /** Bullet color */
  String bulletColor;

  /** Scan color */
  String scanColor;

  /** Tracks color */
  String tracksColor;

  /** Gun color */
  String gunColor;

  /**
   * Updates and returns this intent with new orders for target speed, turn rates, bullet power, and
   * bot colors.
   *
   * @param botIntent is the adjustments for this intent. Fields that are null are ignored, meaning
   *     that the corresponding fields on this intent are left unchanged.
   */
  public BotIntent update(BotIntent botIntent) {
    BotIntentBuilder builder = toBuilder();
    if (botIntent.targetSpeed != null) {
      builder.targetSpeed(botIntent.targetSpeed);
    }
    if (botIntent.turnRate != null) {
      builder.turnRate(botIntent.turnRate);
    }
    if (botIntent.gunTurnRate != null) {
      builder.gunTurnRate(botIntent.gunTurnRate);
    }
    if (botIntent.radarTurnRate != null) {
      builder.radarTurnRate(botIntent.radarTurnRate);
    }
    if (botIntent.bulletPower != null) {
      builder.bulletPower(botIntent.bulletPower);
    }
    if (botIntent.bodyColor != null) {
      builder.bodyColor(botIntent.bodyColor);
    }
    if (botIntent.turretColor != null) {
      builder.turretColor(botIntent.turretColor);
    }
    if (botIntent.radarColor != null) {
      builder.radarColor(botIntent.radarColor);
    }
    if (botIntent.bulletColor != null) {
      builder.bulletColor(botIntent.bulletColor);
    }
    if (botIntent.scanColor != null) {
      builder.scanColor(botIntent.scanColor);
    }
    if (botIntent.tracksColor != null) {
      builder.tracksColor(botIntent.tracksColor);
    }
    if (botIntent.gunColor != null) {
      builder.gunColor(botIntent.gunColor);
    }
    return builder.build();
  }

  /**
   * Returns a zeroed version of this bot intent where all null field for turn rates, target speed
   * and bullet power have been changed into zeros. Note that colors are not getting zeroed.
   */
  public BotIntent zeroed() {
    BotIntentBuilder builder = BotIntent.builder();
    builder.targetSpeed(targetSpeed == null ? 0 : targetSpeed);
    builder.turnRate(turnRate == null ? 0 : turnRate);
    builder.gunTurnRate(gunTurnRate == null ? 0 : gunTurnRate);
    builder.radarTurnRate(radarTurnRate == null ? 0 : radarTurnRate);
    builder.bulletPower(bulletPower == null ? 0 : bulletPower);
    builder.bodyColor(bodyColor);
    builder.turretColor(turretColor);
    builder.radarColor(radarColor);
    builder.bulletColor(bulletColor);
    builder.scanColor(scanColor);
    builder.tracksColor(tracksColor);
    builder.gunColor(gunColor);
    return builder.build();
  }
}
