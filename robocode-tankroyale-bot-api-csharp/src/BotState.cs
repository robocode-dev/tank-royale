using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// </summary>
  public sealed class BotState
  {
    /// <summary>Energy level.</summary>
    /// <value>The energy level.</value>
    public double Energy { get; }

    /// <summary>X coordinate.</summary>
    /// <value>The X coordinate.</value>
    public double X { get; }

    /// <summary>Y coordinate.</summary>
    /// <value>The Y coordinate.</value>
    public double Y { get; }

    /// <summary>Driving direction in degrees.</summary>
    /// <value>The driving direction in degrees.</value>
    public double Direction { get; }

    /// <summary>Gun direction in degrees.</summary>
    /// <value>The gun direction in degrees.</value>
    public double GunDirection { get; }

    /// <summary>Radar direction in degrees.</summary>
    /// <value>The radar direction in degrees.</value>
    public double RadarDirection { get; }

    /// <summary>Radar sweep angle in degrees.</summary>
    /// <value>The radar sweep angle in degrees.</value>
    public double RadarSweep { get; }

    /// <summary>Speed measured in pixels per turn.</summary>
    /// <value>The speed measured in pixels per turn.</value>
    public double Speed { get; }

    /// <summary>Gun heat.</summary>
    /// <value>The gun heat.</value>
    public double GunHeat { get; }

    /// <summary>
    /// Initializes a new instance of the BotState class.
    /// </summary>
    /// <param name="energy">Energy level.</param>
    /// <param name="x">X coordinate.</param>
    /// <param name="y">Y coordinate.</param>
    /// <param name="direction">Driving direction in degrees.</param>
    /// <param name="gunDirection">Gun direction in degrees.</param>
    /// <param name="radarDirection">Radar direction in degrees.</param>
    /// <param name="radarSweep">Radar sweep angle in degrees.</param>
    /// <param name="speed">Speed measured in pixels per turn.</param>
    /// <param name="gunHeat">Gun heat.</param>
    [JsonConstructor]
    public BotState(double energy, double x, double y, double direction, double gunDirection,
      double radarDirection, double radarSweep, double speed, double gunHeat)
    {
      Energy = energy;
      X = x;
      Y = y;
      Direction = direction;
      GunDirection = gunDirection;
      RadarDirection = radarDirection;
      RadarSweep = radarSweep;
      Speed = speed;
      GunHeat = gunHeat;
    }
  }
}