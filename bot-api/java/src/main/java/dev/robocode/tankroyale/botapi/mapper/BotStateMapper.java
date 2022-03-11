package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotState;
import dev.robocode.tankroyale.botapi.Color;

/**
 * Utility class for mapping a bot state.
 */
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
                source.getTurnRate(),
                source.getGunTurnRate(),
                source.getRadarTurnRate(),
                source.getGunHeat(),
                Color.fromRgb(source.getBodyColor()),
                Color.fromRgb(source.getTurretColor()),
                Color.fromRgb(source.getRadarColor()),
                Color.fromRgb(source.getBulletColor()),
                Color.fromRgb(source.getScanColor()),
                Color.fromRgb(source.getTracksColor()),
                Color.fromRgb(source.getGunColor())
        );
    }
}
