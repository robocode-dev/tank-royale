using System.Drawing;
using Newtonsoft.Json;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Represents the current bot state. 
/// </summary>
public sealed class BotState
{
    /// <summary>
    /// Flag specifying if the bot is a droid.
    /// </summary>
    /// <value><c>true</c> if the bot is a droid; <c>false</c> otherwise.</value>
    private bool IsDroid { get; }

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

    /// <summary>Turn rate of the body in degrees per turn (can be positive and negative).</summary>
    /// <value>The turn rate of the body in degrees per turn (can be positive and negative).</value>
    public double TurnRate { get; }

    /// <summary>Turn rate of the gun in degrees per turn (can be positive and negative).</summary>
    /// <value>The turn rate of the gun in degrees per turn (can be positive and negative).</value>
    public double GunTurnRate { get; }

    /// <summary>Turn rate of the radar in degrees per turn (can be positive and negative).</summary>
    /// <value>The turn rate of the radar in degrees per turn (can be positive and negative).</value>
    public double RadarTurnRate { get; }

    /// <summary>Gun heat.</summary>
    /// <value>The gun heat.</value>
    public double GunHeat { get; }

    /// <summary>Number of enemy bots left in the current round.</summary>
    /// <value>The enemy count.</value>
    public int EnemyCount { get; }

    /// <summary>
    /// Body color.
    /// </summary>
    /// <value>The body color.</value>
    public Color? BodyColor { get; }

    /// <summary>
    /// Gun turret color.
    /// </summary>
    /// <value>The gun turret color.</value>
    public Color? TurretColor { get; }

    /// <summary>
    /// Radar color.
    /// </summary>
    /// <value>The radar color.</value>
    public Color? RadarColor { get; }

    /// <summary>
    /// Bullet color.
    /// </summary>
    /// <value>The bullet color.</value>
    public Color? BulletColor { get; }

    /// <summary>
    /// Scan arc color.
    /// </summary>
    /// <value>The scan arc color.</value>
    public Color? ScanColor { get; }

    /// <summary>
    /// Tracks color.
    /// </summary>
    /// <value>The tracks color.</value>
    public Color? TracksColor { get; }

    /// <summary>
    /// Gun color.
    /// </summary>
    /// <value>The gun color.</value>
    public Color? GunColor { get; }

    /// <summary>
    /// Flag indicating if graphical debugging is enabled.
    /// </summary>
    /// <value><c>true</c> if the graphics debugging is enabled; <c>false</c> otherwise.</value>
    public bool IsDebuggingEnabled { get; }

    /// <summary>
    /// Initializes a new instance of the BotState class.
    /// </summary>
    /// <param name="isDroid">Flag specifying if the bot is a droid.</param>
    /// <param name="energy">Energy level.</param>
    /// <param name="x">X coordinate.</param>
    /// <param name="y">Y coordinate.</param>
    /// <param name="direction">Driving direction in degrees.</param>
    /// <param name="gunDirection">Gun direction in degrees.</param>
    /// <param name="radarDirection">Radar direction in degrees.</param>
    /// <param name="radarSweep">Radar sweep angle in degrees.</param>
    /// <param name="speed">Speed measured in pixels per turn.</param>
    /// <param name="turnRate">Turn rate of the body in degrees per turn.</param>
    /// <param name="gunTurnRate">Turn rate of the gun in degrees per turn.</param>
    /// <param name="radarTurnRate">Turn rate of the radar in degrees per turn.</param>
    /// <param name="gunHeat">Gun heat.</param>
    /// <param name="enemyCount">Number of enemies left.</param>
    /// <param name="bodyColor">Body color.</param>
    /// <param name="turretColor">Gun turret color.</param>
    /// <param name="radarColor">Radar color.</param>
    /// <param name="bulletColor">Bullet color.</param>
    /// <param name="scanColor">Scan arc color.</param>
    /// <param name="tracksColor">Tracks color.</param>
    /// <param name="gunColor">Gun color.</param>
    /// <param name="isDebuggingEnabled">Flag specifying if graphical debugging is enabled.</param>
    [JsonConstructor]
    public BotState(
        bool isDroid,
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
        Color? bodyColor,
        Color? turretColor,
        Color? radarColor,
        Color? bulletColor,
        Color? scanColor,
        Color? tracksColor,
        Color? gunColor,
        bool isDebuggingEnabled
    )
    {
        IsDroid = isDroid;
        Energy = energy;
        X = x;
        Y = y;
        Direction = direction;
        GunDirection = gunDirection;
        RadarDirection = radarDirection;
        RadarSweep = radarSweep;
        Speed = speed;
        TurnRate = turnRate;
        GunTurnRate = gunTurnRate;
        RadarTurnRate = radarTurnRate;
        GunHeat = gunHeat;
        EnemyCount = enemyCount;
        BodyColor = bodyColor;
        TurretColor = turretColor;
        RadarColor = radarColor;
        BulletColor = bulletColor;
        ScanColor = scanColor;
        TracksColor = tracksColor;
        GunColor = gunColor;
        IsDebuggingEnabled = isDebuggingEnabled;
    }
}