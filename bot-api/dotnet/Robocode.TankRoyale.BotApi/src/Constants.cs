namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Constants.
/// </summary>
public static class Constants
{
    /// <summary>
    /// The radius of the bounding circle of the bot, which is a constant of 18 units.
    /// 
    /// The bounding circle of a bot is a circle going from the center of the bot with a radius so
    /// that the circle covers most of the bot. The bounding circle is used for determining when a
    /// bot is hit by a bullet.
    ///
    /// A bot gets hit by a bullet when the bullet gets inside the bounding circle, i.e. the
    /// distance between the bullet and the center of the bounding circle is less than the radius
    /// of the bounding circle.
    /// </summary>
    /// <value>The radius of the bounding circle of the bot, which is a constant of 18 units.</value>
    public const int BoundingCircleRadius = 18;

    /// <summary>
    /// The radius of the radar's scan beam, which is a constant of 1200 units.
    /// 
    /// The radar is used for scanning the battlefield for opponent bots. The shape of the scan
    /// beam of the radar is a circle arc ("pizza slice") starting from the center of the bot.
    /// Opponent bots that get inside the scan arc will be detected by the radar.
    ///
    /// The radius of the arc is a constant of 1200 units. This means that that the radar will not
    /// be able to detect bots that are more than 1200 units away from the bot.
    ///
    /// The radar needs to be turned (left or right) to scan opponent bots. So make sure the radar
    /// is always turned. The more the radar is turned, the larger the area of the scan arc
    /// becomes, and the bigger the chance is that the radar detects an opponent. If the radar is
    /// not turning, the scan arc becomes a thin line, unable to scan and detect anything.
    /// </summary>
    /// <value>The radius of the radar's scan beam, which is a constant of 1200 units.</value>
    public const int ScanRadius = 1200;

    /// <summary>
    /// The maximum possible driving turn rate, which is max. 10 degrees per turn.
    /// 
    /// This is the max. possible turn rate of the bot.
    /// 
    /// <note>
    /// The speed of the bot has a direct impact on the turn rate. The faster the speed the less
    /// turn rate.
    /// </note>
    ///
    /// The formula for the max. possible turn rate at a given speed is:
    /// MaxTurnRate - 0.75 x abs(speed).
    /// Hence, the turn rate is at max. 10 degrees/turn when the speed is zero, and down to only 4
    /// degrees per turn when the bot is at max speed (which is 8 units per turn).
    /// </summary>
    /// <value>The maximum possible driving turn rate, which is max. 10 degrees per turn.</value>
    public const int MaxTurnRate = 10;

    /// <summary>
    /// The maximum gun turn rate, which is a constant of 20 degrees per turn.
    /// </summary>
    public const int MaxGunTurnRate = 20;

    /// <summary>
    /// The maximum radar turn rate, which is a constant of 45 degrees per turn.
    /// </summary>
    public const int MaxRadarTurnRate = 45;

    /// <summary>
    /// The maximum absolute speed, which is 8 units per turn.
    /// </summary>
    public const int MaxSpeed = 8;

    /// <summary>
    /// The maximum forward speed, which is 8 units per turn.
    /// </summary>
    public const int MaxForwardSpeed = 8;

    /// <summary>
    /// The maximum backward speed, which is -8 units per turn.
    /// </summary>
    public const int MaxBackwardSpeed = -8;

    /// <summary>
    /// The gun will not fire with a power that is less than the minimum firepower, which is 0.1.
    /// </summary>
    /// <value>The minimum firepower, which is 0.1.</value>
    public const double MinFirepower = 0.1;

    /// <summary>
    /// The gun will fire up to this power only if the firepower is set to a higher value.
    /// </summary>
    /// <value>The maximum firepower, which is 3.</value>
    public const double MaxFirepower = 3;

    /// <summary>
    /// The minimum bullet speed is the slowest possible speed that a bullet can travel and is
    /// defined by the maximum firepower. Min. bullet speed = 20 - 3 x max. firepower, i.e.
    /// 20 - 3 x 3 = 11. The more power, the slower the bullet speed will be.
    /// </summary>
    /// <value>The minimum bullet speed is 11 units per turn.</value>
    public const double MinBulletSpeed = 20 - 3 * MaxFirepower;

    /// <summary>
    /// The maximum bullet speed is the fastest possible speed that a bullet can travel and is
    /// defined by the minimum firepower. Max. bullet speed = 20 - 3 x min. firepower, i.e.
    /// 20 - 3 x 0.1 = 19.7. The lesser power, the faster the bullet speed will be.
    /// </summary>
    /// <value>The maximum bullet speed is 19.7 units per turn.</value>
    public const double MaxBulletSpeed = 20 - 3 * MinFirepower;

    /// <summary>
    /// Acceleration is the increase in speed per turn, which adds 1 unit to the speed per turn
    /// when the bot is increasing its speed moving forward.
    /// </summary>
    /// <value>The acceleration is 1 additional unit per turn.</value>
    public const int Acceleration = 1;

    /// <summary>
    /// Deceleration is the decrease in speed per turn, which subtracts 2 units to the speed
    /// per turn when the bot is decreasing its speed moving backward. This means that a bot
    /// is faster at braking than accelerating forward.
    /// </summary>
    /// <value>The deceleration is 2 units less per turn.</value>
    public const int Deceleration = -2;
}