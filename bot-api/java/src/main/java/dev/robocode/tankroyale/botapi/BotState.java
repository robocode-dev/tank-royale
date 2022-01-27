package dev.robocode.tankroyale.botapi;

/**
 * Represents the current bot state.
 */
@SuppressWarnings("unused")
public final class BotState {

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
     * Gun heat.
     */
    private final double gunHeat;

    /**
     * Body color
     */
    private final Integer bodyColor;

    /**
     * Gun turret color code
     */
    private final Integer turretColor;

    /**
     * Radar color code
     */
    private final Integer radarColor;

    /**
     * Bullet color code
     */
    private final Integer bulletColor;

    /**
     * Scan arc color code
     */
    private final Integer scanColor;

    /**
     * Tracks color code
     */
    private final Integer tracksColor;

    /**
     * Gun color code
     */
    private final Integer gunColor;

    /**
     * Initializes a new instance of the BotState class.
     *
     * @param energy         is the energy level.
     * @param x              is the X coordinate.
     * @param y              is the Y coordinate.
     * @param direction      is the driving direction in degrees.
     * @param gunDirection   is the gun direction in degrees.
     * @param radarDirection is the radar direction in degrees.
     * @param radarSweep     is the radar sweep angle in degrees.
     * @param speed          is the speed measured in units per turn.
     * @param turnRate       is the turn rate of the body in degrees per turn.
     * @param gunTurnRate    is the turn rate of the gun in degrees per turn.
     * @param radarTurnRate  is the turn rate of the radar in degrees per turn.
     * @param gunHeat        is the gun heat.
     * @param bodyColor      is the body color code.
     * @param turretColor    is the gun turret color code.
     * @param radarColor     is the radar color code.
     * @param bulletColor    is the bullet color code.
     * @param scanColor      is the scan arc color code.
     * @param tracksColor    is the tracks color code.
     * @param gunColor       is the gun color code.
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
            double turnRate,
            double gunTurnRate,
            double radarTurnRate,
            double gunHeat,
            Integer bodyColor,
            Integer turretColor,
            Integer radarColor,
            Integer bulletColor,
            Integer scanColor,
            Integer tracksColor,
            Integer gunColor) {
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
        this.bodyColor = bodyColor;
        this.turretColor = turretColor;
        this.radarColor = radarColor;
        this.bulletColor = bulletColor;
        this.scanColor = scanColor;
        this.tracksColor = tracksColor;
        this.gunColor = gunColor;
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
     * Returns the RGB color code of the body. The color code is an integer in hexadecimal format
     * using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
     *
     * @return The color code of the body or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getBodyColor() {
        return bodyColor;
    }

    /**
     * Returns the RGB color code of the gun turret. The color code is an integer in hexadecimal
     * format using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
     *
     * @return The color code of the gun turret or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getTurretColor() {
        return turretColor;
    }

    /**
     * Returns the RGB color code of the radar. The color code is an integer in hexadecimal format
     * using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
     *
     * @return The color code of the radar or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getRadarColor() {
        return radarColor;
    }

    /**
     * Returns the RGB color code of the bullets when fired. The color code is an integer in
     * hexadecimal format using bits 0 - 23 using an 8-bit channel for each color component; red,
     * green, and blue.
     *
     * @return The color code of the bullets or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getBulletColor() {
        return bulletColor;
    }

    /**
     * Returns the RGB color code of the scan arc. The color code is an integer in hexadecimal format
     * using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
     *
     * @return The color code of the scan arc or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getScanColor() {
        return scanColor;
    }

    /**
     * Returns the RGB color code of the tracks. The color code is an integer in hexadecimal format
     * using bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
     *
     * @return The color code of the tracks or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getTracksColor() {
        return tracksColor;
    }

    /**
     * Returns the RGB color code of the gun. The color code is an integer in hexadecimal format using
     * bits 0 - 23 using an 8-bit channel for each color component; red, green, and blue.
     *
     * @return The color code of the gun or {@code null} if the bot uses the default code.
     * @see <a
     * href="https://www.rapidtables.com/web/color/RGB_Color.html">https://www.rapidtables.com/web/color/RGB_Color.html</a>
     */
    public Integer getGunColor() {
        return gunColor;
    }
}
