package dev.robocode.tankroyale.botapi;

import java.awt.Color;

/**
 * Represents the current bot state.
 */
@SuppressWarnings("unused")
public final class BotState {

    /**
     * Flag specifying if the bot is a droid.
     */
    private final boolean isDroid;

    /**
     * Energy level.
     */
    private final double energy;

    /**
     * X coordinate.
     */
    private final double x;

    /**
     * Y coordinate.
     */
    private final double y;

    /**
     * Driving direction in degrees.
     */
    private final double direction;

    /**
     * Gun direction in degrees.
     */
    private final double gunDirection;

    /**
     * Radar direction in degrees.
     */
    private final double radarDirection;

    /**
     * Radar sweep angle in degrees.
     */
    private final double radarSweep;

    /**
     * Speed measured in units per turn.
     */
    private final double speed;

    /**
     * Turn rate of the body in degrees per turn (can be positive and negative)
     */
    private final double turnRate;

    /**
     * Turn rate of the gun in degrees per turn (can be positive and negative)
     */
    private final double gunTurnRate;

    /**
     * Turn rate of the radar in degrees per turn (can be positive and negative)
     */
    private final double radarTurnRate;

    /**
     * Gun heat
     */
    private final double gunHeat;

    /**
     *
     */
    private final int enemyCount;

    /**
     * Body color
     */
    private final Color bodyColor;

    /**
     * Gun turret color
     */
    private final Color turretColor;

    /**
     * Radar color
     */
    private final Color radarColor;

    /**
     * Bullet color
     */
    private final Color bulletColor;

    /**
     * Scan arc color
     */
    private final Color scanColor;

    /**
     * Tracks color
     */
    private final Color tracksColor;

    /**
     * Gun color
     */
    private final Color gunColor;

    /**
     * Flag indicating if graphical debugging is enabled.
     */
    private final boolean isDebuggingEnabled;

    /**
     * Initializes a new instance of the BotState class.
     *
     * @param isDroid            is the flag specifying if the bot is a droid.
     * @param energy             is the energy level.
     * @param x                  is the X coordinate.
     * @param y                  is the Y coordinate.
     * @param direction          is the driving direction in degrees.
     * @param gunDirection       is the gun direction in degrees.
     * @param radarDirection     is the radar direction in degrees.
     * @param radarSweep         is the radar sweep angle in degrees.
     * @param speed              is the speed measured in units per turn.
     * @param turnRate           is the turn rate of the body in degrees per turn.
     * @param gunTurnRate        is the turn rate of the gun in degrees per turn.
     * @param radarTurnRate      is the turn rate of the radar in degrees per turn.
     * @param gunHeat            is the gun heat.
     * @param enemyCount         is tbe number of enemies left
     * @param bodyColor          is the body color.
     * @param turretColor        is the gun turret color.
     * @param radarColor         is the radar color.
     * @param bulletColor        is the bullet color.
     * @param scanColor          is the scan arc color.
     * @param tracksColor        is the tracks color.
     * @param gunColor           is the gun color.
     * @param isDebuggingEnabled is a flag indicating if graphical debugging is enabled.
     */
    public BotState(
            boolean isDroid,
            double energy,
            double x,
            double y,
            double direction,
            double gunDirection,
            double radarDirection,
            double radarSweep,
            double speed,
            double turnRate,
            double gunTurnRate,
            double radarTurnRate,
            double gunHeat,
            int enemyCount,
            Color bodyColor,
            Color turretColor,
            Color radarColor,
            Color bulletColor,
            Color scanColor,
            Color tracksColor,
            Color gunColor,
            boolean isDebuggingEnabled) {
        this.isDroid = isDroid;
        this.energy = energy;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.gunDirection = gunDirection;
        this.radarDirection = radarDirection;
        this.radarSweep = radarSweep;
        this.speed = speed;
        this.turnRate = turnRate;
        this.gunTurnRate = gunTurnRate;
        this.radarTurnRate = radarTurnRate;
        this.gunHeat = gunHeat;
        this.enemyCount = enemyCount;
        this.bodyColor = bodyColor;
        this.turretColor = turretColor;
        this.radarColor = radarColor;
        this.bulletColor = bulletColor;
        this.scanColor = scanColor;
        this.tracksColor = tracksColor;
        this.gunColor = gunColor;
        this.isDebuggingEnabled = isDebuggingEnabled;
    }

    /**
     * Checks if the bot is a droid or not.
     *
     * @return {@code true} if the bot is a droid; {@code false} otherwise.
     */
    public boolean isDroid() {
        return isDroid;
    }

    /**
     * Returns the energy level of the bot. The energy level is typically starting at 100. The bot
     * gains more energy when hitting other bots, and loses energy by getting hit and when spending
     * energy on firing bullets. When the energy reaches 0, the bot becomes disabled and will not be
     * able to take any new action. It might become active again, if one of its bullets hit another
     * bot, meaning that the bot gains new energy.
     *
     * @return The energy level.
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * Returns the X coordinate of the bot, which is in the center of the bot.
     *
     * @return The X coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of the bot, which is in the center of the bot.
     *
     * @return The Y coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the driving direction of the bot in degrees.
     *
     * @return The driving direction.
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Returns the gun direction of the bot in degrees.
     *
     * @return The gun direction.
     */
    public double getGunDirection() {
        return gunDirection;
    }

    /**
     * Returns the radar direction of the bot in degrees.
     *
     * @return The radar direction.
     */
    public double getRadarDirection() {
        return radarDirection;
    }

    /**
     * Returns the radar sweep angle in degrees, i.e. delta angle between previous and current radar
     * direction.
     *
     * @return The radar sweep angle.
     */
    public double getRadarSweep() {
        return radarSweep;
    }

    /**
     * Returns the speed measured in units per turn.
     *
     * @return The speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns the turn rate of the body in degrees per turn (can be positive and negative).
     *
     * @return the turn rate.
     */
    public double getTurnRate() {
        return turnRate;
    }

    /**
     * Returns the turn rate of the gun in degrees per turn (can be positive and negative).
     *
     * @return the gun turn rate.
     */
    public double getGunTurnRate() {
        return gunTurnRate;
    }

    /**
     * Returns the turn rate of the radar in degrees per turn (can be positive and negative).
     *
     * @return the radar turn rate.
     */
    public double getRadarTurnRate() {
        return radarTurnRate;
    }

    /**
     * Returns the gun heat. When firing the gun, it will be heated up. The gun will need to cool down
     * before it can fire another bullet. When the gun heat is zero, the gun will be able to fire
     * again.
     *
     * @return The gun heat.
     */
    public double getGunHeat() {
        return gunHeat;
    }

    /**
     * Returns the number of enemy bots left on the battlefield.
     *
     * @return the number of enemy bots left on the battlefield.
     */
    public int getEnemyCount() {
        return enemyCount;
    }

    /**
     * Returns the color the body.
     *
     * @return The color the body.
     */
    public Color getBodyColor() {
        return bodyColor;
    }

    /**
     * Returns the color of the gun turret.
     *
     * @return The color of the gun turret.
     */
    public Color getTurretColor() {
        return turretColor;
    }

    /**
     * Returns the color of the radar.
     *
     * @return The color of the radar.
     */
    public Color getRadarColor() {
        return radarColor;
    }

    /**
     * Returns the color of the bullets when fired.
     *
     * @return The color of the bullets when fired.
     */
    public Color getBulletColor() {
        return bulletColor;
    }

    /**
     * Returns the color of the scan arc.
     *
     * @return The color of the scan arc.
     */
    public Color getScanColor() {
        return scanColor;
    }

    /**
     * Returns the color of the tracks.
     *
     * @return The color of the tracks.
     */
    public Color getTracksColor() {
        return tracksColor;
    }

    /**
     * Returns the color of the gun.
     *
     * @return The color of the gun.
     */
    public Color getGunColor() {
        return gunColor;
    }

    /**
     * Checks if graphical debugging is enabled.
     *
     * @return {@code true} if graphical debugging is enabled; {@code false} otherwise.
     */
    public boolean isDebuggingEnabled() {
        return isDebuggingEnabled;
    }
}
