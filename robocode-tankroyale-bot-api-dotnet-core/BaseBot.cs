namespace Robocode.TankRoyale
{
  public class BaseBot : IBaseBot
  {
    public int BoundingCircleRadius { get { return 18; } }

    public double RadarRadius { get { return 1200; } }

    public double MaxTurnRate { get { return 10; } }

    public double MaxGunTurnRate { get { return 20; } }

    public double MaxRadarTurnRate { get { return 45; } }

    public double MaxSpeed { get { return 8; } }

    public double MaxForwardSpeed { get { return MaxSpeed; } }

    public double MaxBackwardSpeed { get { return -MaxSpeed; } }

    public double MinFirepower { get { return 0.1; } }

    public double MaxFirepower { get { return 3; } }

    public double MinBulletSpeed { get { return 20 - 3 * MaxFirepower; } }

    public double MaxBulletSpeed { get { return 20 - 3 * MinFirepower; } }

    public double Acceleration { get { return 1; } }

    public double Deceleration { get { return -2; } }
  }
}