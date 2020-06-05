package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotState;

/** Utility class for mapping a bot state. */
public final class BotStateMapper {

  public static BotState map(final dev.robocode.tankroyale.schema.BotState source) {
    return new BotState(
        source.getEnergy(),
        source.getX(),
        source.getY(),
        source.getDirection(),
        source.getGunDirection(),
        source.getRadarDirection(),
        source.getRadarSweep(),
        source.getSpeed(),
        source.getGunHeat(),
        source.getBodyColor(),
        source.getTurretColor(),
        source.getRadarColor(),
        source.getBulletColor(),
        source.getScanColor(),
        source.getTracksColor(),
        source.getGunColor()
    );
  }
}
