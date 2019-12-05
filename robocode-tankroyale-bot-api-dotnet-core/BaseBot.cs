namespace Robocode.TankRoyale
{
  public class BaseBot : IBaseBot
  {
    /// <summary>
    /// Bounding circle radius. A bot gets hit by a bullet when the distance between the center of the
    /// bullet and the position of the bot (center) is less than the bounding circle radius.
    /// </summary>
    const int BOUNDING_CIRCLE_RADIUS = 18;

    /// <summary>
    /// Radar radius. This is how far a bot is able to scan other bots with the radar. Bots outside the
    /// radar radius will not be scanned.
    /// </summary>
    const double RADAR_RADIUS = 1200;

    /// <summary>
    /// Maximum driving turn rate measured in degrees/turn. This is the max. possible turn rate of the
    /// bot. Note that the speed of the bot has an impact on the turn rate. The faster speed the less
    /// turn rate.
    /// 
    /// The formula for the max. possible turn rate at a given speed is: MAX_TURN_RATE - 0.75 x
    /// abs(speed). Hence, the turn rate is at max. 10 degrees/turn when the speed is zero, and down to
    /// only 4 degrees/turn when the robot is at max speed (8 pixels/turn).
    /// </summary>
    const double MAX_TURN_RATE = 10;

    /// <summary>
    /// Maximum gun turn rate measured in degrees/turn.
    /// </summary>
    const double MAX_GUN_TURN_RATE = 20;

    /// <summary>
    /// Maximum radar turn rate measured in degrees/turn.
    /// </summary>
    const double MAX_RADAR_TURN_RATE = 45;

    /// <summary>
    /// Maximum absolute speed measured in pixels/turn.
    /// </summary>
    const double MAX_SPEED = 8;

    /// <summary>
    /// Maximum forward speed measured in pixels/turn. When the speed is positive the bot is moving forwards.
    /// </summary>
    const double MAX_FORWARD_SPEED = MAX_SPEED;

    /// <summary>
    /// Maximum backward speed measured in pixels/turn. When the speed is negative the bot is moving backwards.
    /// </summary>
    const double MAX_BACKWARD_SPEED = -MAX_SPEED;

    /// <summary>
    /// Minimum firepower. The gun will not fire with a power less than the minimum firepower.
    /// </summary>
    const double MIN_FIREPOWER = 0.1;

    /// <summary>
    /// Maximum firepower. The gun will fire with this firepower if the gun is set to fire with a higher firepower.
    /// </summary>
    const double MAX_FIREPOWER = 3;

    /// <summary>
    /// Minimum bullet speed measured in pixels/turn. The bullet speed is determined by this formula:
    /// 20 - 3 x firepower. The more fire power the slower bullet speed. Hence, the minimum bullet
    /// speed is 11 pixels/turn.
    /// </summary>
    const double MIN_BULLET_SPEED = 20 - 3 * MAX_FIREPOWER;

    /// <summary>
    /// Maximum bullet speed measured in pixels/turn. The bullet speed is determined by this formula:
    /// 20 - 3 x firepower. The less fire power the faster bullet speed. Hence, the maximum bullet
    /// speed is 17 pixels/turn.
    /// </summary>
    const double MAX_BULLET_SPEED = 20 - 3 * MIN_FIREPOWER;

    /// <summary>
    /// Acceleration that adds 1 pixel to the speed per turn when the bot is increasing its speed moving forwards.
    /// </summary>
    const double ACCELERATION = 1;

    /// <summary>
    /// Deceleration that subtract 2 pixels from the speed per turn when the bot is decreasing its
    /// speed moving backwards. Note that the deceleration is negative.
    /// </summary>
    const double DECELERATION = -2;
  }
}