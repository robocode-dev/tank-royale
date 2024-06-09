package dev.robocode.tankroyale.botapi;

/**
 * Constants.
 */
@SuppressWarnings("unused")
public final class Constants {

    // Hide constructor to prevent instantiation
    private Constants() {
    }

    /**
     * The radius of the bounding circle of the bot, which is a constant of 18 units.
     *
     * <p>The bounding circle of a bot is a circle going from the center of the bot with a radius so
     * that the circle covers most of the bot. The bounding circle is used for determining when a bot
     * is hit by a bullet.
     *
     * <p>A bot gets hit by a bullet when the bullet gets inside the bounding circle, i.e. the
     * distance between the bullet and the center of the bounding circle is less than the radius of
     * the bounding circle.
     */
    public static final int BOUNDING_CIRCLE_RADIUS = 18;

    /**
     * The radius of the radar's scan beam, which is a constant of 1200 units.
     *
     * <p>The radar is used for scanning the battlefield for opponent bots. The shape of the scan beam
     * of the radar is a circle arc ("pizza slice") starting from the center of the bot. Opponent bots
     * that get inside the scan arc will be detected by the radar.
     *
     * <p>The radius of the arc is a constant of {@value} units. This means that that the radar will
     * not be able to detect bots that are more than {@value} units away from the bot.
     *
     * <p>The radar needs to be turned (left or right) to scan opponent bots. So make sure the radar
     * is always turned. The more the radar is turned, the larger the area of the scan arc becomes,
     * and the bigger the chance is that the radar detects an opponent. If the radar is not turning,
     * the scan arc becomes a thin line, unable to scan and detect anything.
     */
    public static final int SCAN_RADIUS = 1200;

    /**
     * The maximum possible driving turn rate, which is max. 10 degrees per turn.
     *
     * <p>This is the max. possible turn rate of the bot. Note that the speed of the bot has a direct
     * impact on the turn rate. The faster the speed the less turn rate.
     *
     * <p>The formula for the max. possible turn rate at a given speed is: MaxTurnRate - 0.75 x
     * abs(speed). Hence, the turn rate is at max. 10 degrees/turn when the speed is zero, and down to
     * only 4 degrees per turn when the bot is at max speed (which is 8 units per turn).
     */
    public static final int MAX_TURN_RATE = 10;

    /**
     * The maximum gun turn rate, which is a constant of 20 degrees per turn.
     */
    public static final int MAX_GUN_TURN_RATE = 20;

    /**
     * The maximum radar turn rate, which is a constant of 45 degrees per turn.
     */
    public static final int MAX_RADAR_TURN_RATE = 45;

    /**
     * The maximum absolute speed, which is 8 units per turn.
     */
    public static final int MAX_SPEED = 8;

    /**
     * The minimum firepower, which is 0.1. The gun will not fire with a power that is less than the
     * minimum firepower, which is 0.1.
     */
    public static final double MIN_FIREPOWER = 0.1;

    /**
     * The maximum firepower, which is 3. The gun will fire up to this power, even if the firepower is
     * set to a higher value.
     */
    public static final double MAX_FIREPOWER = 3;

    /**
     * The minimum bullet speed is 11 units per turn.
     *
     * <p>The minimum bullet speed is the slowest possible speed that a bullet can travel and is
     * defined by the maximum firepower: 20 - 3 x max. firepower, i.e. 20 - 3 x 3 = 11. The more
     * power, the slower the bullet speed will be.
     */
    public static final double MIN_BULLET_SPEED = 20 - 3 * MAX_FIREPOWER;

    /**
     * The maximum bullet speed is 19.7 units per turn.
     *
     * <p>The maximum bullet speed is the fastest possible speed that a bullet can travel and is
     * defined by the minimum firepower. Max. bullet speed = 20 - 3 x min. firepower, i.e. 20 - 3 x
     * 0.1 = 19.7. The lesser power, the faster the bullet speed will be.
     */
    public static final double MAX_BULLET_SPEED = 20 - 3 * MIN_FIREPOWER;

    /**
     * Acceleration is the increase in speed per turn, which adds 1 unit to the speed per turn when
     * the bot is increasing its speed moving forward.
     */
    public static final int ACCELERATION = 1;

    /**
     * Deceleration is the decrease in speed per turn, which subtracts 2 units to the speed per turn
     * when the bot is decreasing its speed moving backward. This means that a bot is faster at
     * braking than accelerating forward.
     */
    public static final int DECELERATION = -2;
}
