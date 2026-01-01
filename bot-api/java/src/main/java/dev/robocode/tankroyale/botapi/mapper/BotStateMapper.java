package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BotState;

import static dev.robocode.tankroyale.botapi.util.ColorUtil.fromHexColor;

/**
 * Utility class for mapping a bot state.
 */
public final class BotStateMapper {

    // Hide constructor to prevent instantiation
    private BotStateMapper() {
    }

    public static BotState map(final dev.robocode.tankroyale.schema.BotState source) {
        if (source == null) {
            // Return a safe default to avoid NPEs in tests when botState is missing
            return new BotState(
                    false, // isDroid
                    0.0,   // energy
                    0.0,   // x
                    0.0,   // y
                    0.0,   // direction
                    0.0,   // gunDirection
                    0.0,   // radarDirection
                    0.0,   // radarSweep
                    0.0,   // speed
                    0.0,   // turnRate
                    0.0,   // gunTurnRate
                    0.0,   // radarTurnRate
                    0.0,   // gunHeat
                    0,     // enemyCount
                    null,  // bodyColor
                    null,  // turretColor
                    null,  // radarColor
                    null,  // bulletColor
                    null,  // scanColor
                    null,  // tracksColor
                    null,  // gunColor
                    false  // isDebuggingEnabled
            );
        }

        boolean isDroid = source.getIsDroid() != null && source.getIsDroid();
        double energy = source.getEnergy() == null ? 0.0 : source.getEnergy();
        double x = source.getX() == null ? 0.0 : source.getX();
        double y = source.getY() == null ? 0.0 : source.getY();
        double direction = source.getDirection() == null ? 0.0 : source.getDirection();
        double gunDirection = source.getGunDirection() == null ? 0.0 : source.getGunDirection();
        double radarDirection = source.getRadarDirection() == null ? 0.0 : source.getRadarDirection();
        double radarSweep = source.getRadarSweep() == null ? 0.0 : source.getRadarSweep();
        double speed = source.getSpeed() == null ? 0.0 : source.getSpeed();
        double turnRate = source.getTurnRate() == null ? 0.0 : source.getTurnRate();
        double gunTurnRate = source.getGunTurnRate() == null ? 0.0 : source.getGunTurnRate();
        double radarTurnRate = source.getRadarTurnRate() == null ? 0.0 : source.getRadarTurnRate();
        double gunHeat = source.getGunHeat() == null ? 0.0 : source.getGunHeat();
        int enemyCount = source.getEnemyCount() == null ? 0 : source.getEnemyCount();
        boolean isDebuggingEnabled = source.getIsDebuggingEnabled() != null && source.getIsDebuggingEnabled();

        return new BotState(
                isDroid,
                energy,
                x,
                y,
                direction,
                gunDirection,
                radarDirection,
                radarSweep,
                speed,
                turnRate,
                gunTurnRate,
                radarTurnRate,
                gunHeat,
                enemyCount,
                fromHexColor(source.getBodyColor()),
                fromHexColor(source.getTurretColor()),
                fromHexColor(source.getRadarColor()),
                fromHexColor(source.getBulletColor()),
                fromHexColor(source.getScanColor()),
                fromHexColor(source.getTracksColor()),
                fromHexColor(source.getGunColor()),
                isDebuggingEnabled
        );
    }
}
