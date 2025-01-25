package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotState;

import static dev.robocode.tankroyale.botapi.util.ColorUtil.fromString;

/**
 * Utility class for mapping a bot state.
 */
public final class BotStateMapper {

    // Hide constructor to prevent instantiation
    private BotStateMapper() {
    }

    public static BotState map(final dev.robocode.tankroyale.schema.game.BotState source) {
        return new BotState(
                source.getIsDroid(),
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
                source.getEnemyCount(),
                fromString(source.getBodyColor()),
                fromString(source.getTurretColor()),
                fromString(source.getRadarColor()),
                fromString(source.getBulletColor()),
                fromString(source.getScanColor()),
                fromString(source.getTracksColor()),
                fromString(source.getGunColor()),
                source.getIsDebuggingEnabled()
        );
    }
}
