namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// </summary>
  public sealed class BotState
  {
    /// <summary>Energy level.</summary>
    public double Energy { get; }

    /// <summary>X coordinate.</summary>
    public double X { get; }

    /// <summary>Y coordinate.</summary>
    public double Y { get; }

    /// <summary>Driving direction in degrees.</summary>
    public double Direction { get; }

    /// <summary>Gun direction in degrees.</summary>
    public double GunDirection { get; }

    /// <summary>Radar direction in degrees.</summary>
    public double RadarDirection { get; }

    /// <summary>Radar sweep angle in degrees, i.e. angle between previous and current radar direction.</summary>
    public double RadarSweep { get; }

    /// <summary>Speed measured in pixels per turn.</summary>
    public double Speed { get; }

    /// <summary>Gun heat.</summary>
    public double GunHeat { get; }

    /// <summary>
    /// Constructor.
    /// </summary>
    /// <param name="energy">Energy level.</param>
    /// <param name="x">X coordinate.</param>
    /// <param name="y">Y coordinate.</param>
    /// <param name="direction">Driving direction in degrees.</param>
    /// <param name="gunDirection">Gun direction in degrees.</param>
    /// <param name="radarDirection">Radar direction in degrees.</param>
    /// <param name="radarSweep">Radar sweep angle in degrees, i.e. angle between previous and current radar direction.</param>
    /// <param name="speed">Speed measured in pixels per turn.</param>
    /// <param name="gunHeat">Gun heat.</param>
    BotState(double energy, double x, double y, double direction, double gunDirection,
      double radarDirection, double radarSweep, double speed, double gunHeat) =>
      (Energy, X, Y, Direction, GunDirection, RadarDirection, RadarSweep, Speed, GunHeat) =
      (energy, x, y, direction, gunDirection, radarDirection, radarSweep, speed, gunHeat);
  }
}