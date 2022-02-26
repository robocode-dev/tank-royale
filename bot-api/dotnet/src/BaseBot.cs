using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract bot class that takes care of communication between the bot and the server, and sends
  /// notifications through the event handlers. Most bots can inherit from this class to get access
  /// to basic methods.
  /// </summary>
  public abstract class BaseBot : IBaseBot
  {
    internal readonly BaseBotInternals __baseBotInternals;

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// This constructor should be used when  both BotInfo and server URL is provided through
    /// environment variables, i.e., when starting up the bot using a booter. These environment
    /// variables must be set to provide the server URL and bot information, and are automatically
    /// set by the booter tool for Robocode.
    ///
    /// Example of how to set the predefined environment variables:
    ///
    /// ROBOCODE_SERVER_URL=ws://localhost<br/>
    /// ROBOCODE_SERVER_SECRET=xzoEeVbnBe5TGjCny0R1yQ
    /// BOT_NAME=MyBot<br/>
    /// BOT_VERSION=1.0<br/>
    /// BOT_AUTHORS=John Doe<br/>
    /// BOT_DESCRIPTION=A short description<br/>
    /// BOT_URL=http://somewhere.net/MyBot<br/>
    /// BOT_COUNTRY_CODES=us<br/>
    /// BOT_GAME_TYPES=classic, melee, 1v1<br/>
    /// BOT_PROG_PLATFORM=.Net 5.0<br/>
    /// BOT_PROG_LANG=C# 8.0<br/>
    /// </summary>
    public BaseBot()
    {
      __baseBotInternals = new BaseBotInternals(this, null, null, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// This constructor assumes the server URL and secret is provided by the environment
    /// variables ROBOCODE_SERVER_URL and ROBOCODE_SERVER_SECRET.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    public BaseBot(BotInfo botInfo)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, null, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URL</param>
    public BaseBot(BotInfo botInfo, Uri serverUrl)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URL</param>
    /// <param name="serverSecret">Is the server secret</param>
    public BaseBot(BotInfo botInfo, Uri serverUrl, string serverSecret)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, serverSecret);
    }

    /// <inheritdoc/>
    public void Start()
    {
      __baseBotInternals.Start();
    }

    /// <inheritdoc/>
    public void Go()
    {
      __baseBotInternals.Execute();
    }

    /// <inheritdoc/>
    public string Variant
    {
      get => __baseBotInternals.Variant;
    }

    /// <inheritdoc/>
    public string Version
    {
      get => __baseBotInternals.Version;
    }

    /// <inheritdoc/>
    public int MyId
    {
      get => __baseBotInternals.MyId;
    }

    /// <inheritdoc/>
    public string GameType
    {
      get => __baseBotInternals.GameSetup.GameType;
    }

    /// <inheritdoc/>
    public int ArenaWidth
    {
      get => __baseBotInternals.GameSetup.ArenaWidth;
    }

    /// <inheritdoc/>
    public int ArenaHeight
    {
      get => __baseBotInternals.GameSetup.ArenaHeight;
    }

    /// <inheritdoc/>
    public int NumberOfRounds
    {
      get => __baseBotInternals.GameSetup.NumberOfRounds;
    }

    /// <inheritdoc/>
    public double GunCoolingRate
    {
      get => __baseBotInternals.GameSetup.GunCoolingRate;
    }

    /// <inheritdoc/>
    public int? MaxInactivityTurns
    {
      get => __baseBotInternals.GameSetup.MaxInactivityTurns;
    }

    /// <inheritdoc/>
    public int TurnTimeout
    {
      get => __baseBotInternals.GameSetup.TurnTimeout;
    }

    /// <inheritdoc/>
    public int TimeLeft
    {
      get => __baseBotInternals.TimeLeft;
    }

    /// <inheritdoc/>
    public int RoundNumber
    {
      get => __baseBotInternals.CurrentTick.RoundNumber;
    }

    /// <inheritdoc/>
    public int TurnNumber
    {
      get => __baseBotInternals.CurrentTick.TurnNumber;
    }

    /// <inheritdoc/>
    public int EnemyCount
    {
      get => __baseBotInternals.CurrentTick.EnemyCount;
    }

    /// <inheritdoc/>
    public double Energy
    {
      get => __baseBotInternals.CurrentTick.BotState.Energy;
    }

    /// <inheritdoc/>
    public bool IsDisabled
    {
      get => Energy == 0;
    }

    /// <inheritdoc/>
    public double X
    {
      get => __baseBotInternals.CurrentTick.BotState.X;
    }

    /// <inheritdoc/>
    public double Y
    {
      get => __baseBotInternals.CurrentTick.BotState.Y;
    }

    /// <inheritdoc/>
    public double Direction
    {
      get => __baseBotInternals.CurrentTick.BotState.Direction;
    }

    /// <inheritdoc/>
    public double GunDirection
    {
      get => __baseBotInternals.CurrentTick.BotState.GunDirection;
    }

    /// <inheritdoc/>
    public double RadarDirection
    {
      get => __baseBotInternals.CurrentTick.BotState.RadarDirection;
    }

    /// <inheritdoc/>
    public double Speed
    {
      get => __baseBotInternals.CurrentTick.BotState.Speed;
    }

    /// <inheritdoc/>
    public double GunHeat
    {
      get => __baseBotInternals.CurrentTick.BotState.GunHeat;
    }

    /// <inheritdoc/>
    public IEnumerable<BulletState> BulletStates
    {
      get => __baseBotInternals.CurrentTick.BulletStates;
    }

    /// <inheritdoc/>
    public IEnumerable<BotEvent> Events
    {
      get => __baseBotInternals.CurrentTick.Events;
    }

    /// <inheritdoc/>
    public double TurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("TurnRate cannot be NaN");
        }
        __baseBotInternals.BotIntent.TurnRate = value;
      }
      get => __baseBotInternals.CurrentTick.BotState.TurnRate;
    }

    /// <inheritdoc/>
    public void SetMaxTurnRate(double maxTurnRate)
    {
      __baseBotInternals.SetMaxTurnRate(maxTurnRate);
    }

    /// <inheritdoc/>
    public double GunTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("GunTurnRate cannot be NaN");
        }
        __baseBotInternals.BotIntent.GunTurnRate = value;
      }
      get => __baseBotInternals.CurrentTick.BotState.GunTurnRate;
    }

    /// <inheritdoc/>
    public void SetMaxGunTurnRate(double maxGunTurnRate)
    {
      __baseBotInternals.SetMaxGunTurnRate(maxGunTurnRate);
    }

    /// <inheritdoc/>
    public double RadarTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("RadarTurnRate cannot be NaN");
        }
        __baseBotInternals.BotIntent.RadarTurnRate = value;
      }
      get => __baseBotInternals.CurrentTick.BotState.RadarTurnRate;
    }

    /// <inheritdoc/>
    public void SetMaxRadarTurnRate(double maxRadarTurnRate)
    {
      __baseBotInternals.SetMaxRadarTurnRate(maxRadarTurnRate);
    }

    /// <inheritdoc/>
    public double TargetSpeed
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("TargetSpeed cannot be NaN");
        }
        __baseBotInternals.BotIntent.TargetSpeed = value;
      }
      get => __baseBotInternals.BotIntent.TargetSpeed ?? 0d;
    }

    /// <inheritdoc/>
    public void SetMaxSpeed(double maxSpeed)
    {
      __baseBotInternals.SetMaxSpeed(maxSpeed);
    }

    /// <inheritdoc/>
    public bool SetFire(double firepower)
    {
      return __baseBotInternals.SetFire(firepower);
    }

    /// <inheritdoc/>
    public void SetScan()
    {
      __baseBotInternals.BotIntent.Scan = true;
    }

    /// <inheritdoc/>
    public double Firepower
    {
      get => __baseBotInternals.BotIntent.Firepower ?? 0;
    }

    /// <inheritdoc/>
    public bool DoAdjustGunForBodyTurn
    {
      set => __baseBotInternals.BotIntent.AdjustGunForBodyTurn = value;
      get => __baseBotInternals.BotIntent.AdjustGunForBodyTurn ?? false;
    }

    /// <inheritdoc/>
    public bool DoAdjustRadarForGunTurn
    {
      set => __baseBotInternals.BotIntent.AdjustRadarForGunTurn = value;
      get => __baseBotInternals.BotIntent.AdjustRadarForGunTurn ?? false;
    }

    /// <inheritdoc/>
    public void AddCustomEvent(Condition condition)
    {
      __baseBotInternals.AddCondition(condition);
    }

    /// <inheritdoc/>
    public void RemoveCustomEvent(Condition condition)
    {
      __baseBotInternals.RemoveCondition(condition);
    }

    /// <inheritdoc/>
    public void SetStop()
    {
      __baseBotInternals.SetStop();
    }

    /// <inheritdoc/>
    public void SetResume()
    {
      __baseBotInternals.SetResume();
    }

    /// <inheritdoc/>
    public bool IsStopped => __baseBotInternals.IsStopped;

    /// <inheritdoc/>
    public int? GetBodyColor() => __baseBotInternals.CurrentTick.BotState.BodyColor;

    /// <inheritdoc/>
    public void SetBodyColor(string bodyColor) => __baseBotInternals.BotIntent.BodyColor = bodyColor;

    /// <inheritdoc/>
    public int? GetTurretColor() => __baseBotInternals.CurrentTick.BotState.TurretColor;

    /// <inheritdoc/>
    public void SetTurretColor(string turretColor) => __baseBotInternals.BotIntent.TurretColor = turretColor;

    /// <inheritdoc/>
    public int? GetRadarColor() => __baseBotInternals.CurrentTick.BotState.RadarColor;

    /// <inheritdoc/>
    public void SetRadarColor(string radarColor) => __baseBotInternals.BotIntent.RadarColor = radarColor;

    /// <inheritdoc/>
    public int? GetBulletColor() => __baseBotInternals.CurrentTick.BotState.BulletColor;

    /// <inheritdoc/>
    public void SetBulletColor(string bulletColor) => __baseBotInternals.BotIntent.BulletColor = bulletColor;

    /// <inheritdoc/>
    public int? GetScanColor() => __baseBotInternals.CurrentTick.BotState.ScanColor;

    /// <inheritdoc/>
    public void SetScanColor(string scanColor) => __baseBotInternals.BotIntent.ScanColor = scanColor;

    /// <inheritdoc/>
    public int? GetTracksColor() => __baseBotInternals.CurrentTick.BotState.TracksColor;

    /// <inheritdoc/>
    public void SetTracksColor(string tracksColor) => __baseBotInternals.BotIntent.TracksColor = tracksColor;

    /// <inheritdoc/>
    public int? GetGunColor() => __baseBotInternals.CurrentTick.BotState.GunColor;

    /// <inheritdoc/>
    public void SetGunColor(string gunColor) => __baseBotInternals.BotIntent.GunColor = gunColor;

    /// <inheritdoc/>
    public double CalcMaxTurnRate(double speed) => ((IBaseBot)this).MaxTurnRate - 0.75 * Math.Abs(speed);

    /// <inheritdoc/>
    public virtual double CalcBulletSpeed(double firepower) => 20 - 3 * firepower;

    /// <inheritdoc/>
    public virtual double CalcGunHeat(double firepower) => 1 + (firepower / 5);

    /// <inheritdoc/>
    public virtual double CalcBearing(double direction) => NormalizeRelativeAngle(direction - Direction);

    /// <inheritdoc/>
    public virtual double CalcGunBearing(double direction) => NormalizeRelativeAngle(direction - GunDirection);

    /// <inheritdoc/>
    public virtual double CalcRadarBearing(double direction) => NormalizeRelativeAngle(direction - RadarDirection);

    /// <inheritdoc/>
    public virtual double DirectionTo(double x, double y) => NormalizeAbsoluteAngle(180 * Math.Atan2(y - Y, x - X) / Math.PI);

    /// <inheritdoc/>
    public virtual double BearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - Direction);

    /// <inheritdoc/>
    public virtual double GunBearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - GunDirection);

    /// <inheritdoc/>
    public virtual double RadarBearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - RadarDirection);

    /// <inheritdoc/>
    public virtual double DistanceTo(double x, double y)
    {
      var dx = x - X;
      var dy = y - Y;
      return Math.Sqrt(dx * dx + dy * dy);
    }

    /// <inheritdoc/>
    public virtual double NormalizeAbsoluteAngle(double angle) => (angle %= 360) >= 0 ? angle : (angle + 360);

    /// <inheritdoc/>
    public virtual double NormalizeRelativeAngle(double angle) => (angle %= 360) >= 0 ?
        ((angle < 180) ? angle : (angle - 360)) :
        ((angle >= -180) ? angle : (angle + 360));

    /// <inheritdoc/>
    public virtual double CalcDeltaAngle(double targetAngle, double sourceAngle)
    {
      double angle = targetAngle - sourceAngle;
      angle += (angle > 180) ? -360 : (angle < -180) ? 360 : 0;
      return angle;
    }

    /// <inheritdoc/>
    public virtual void OnConnected(ConnectedEvent connectedEvent) => Console.WriteLine($"Connected to: {connectedEvent.ServerUri}");

    /// <inheritdoc/>
    public virtual void OnDisconnected(DisconnectedEvent disconnectedEvent) => Console.WriteLine($"Disconnected from: {disconnectedEvent.ServerUri}");

    /// <inheritdoc/>
    public virtual void OnConnectionError(ConnectionErrorEvent connectionErrorEvent) => Console.Error.WriteLine($"Connection error with {connectionErrorEvent.ServerUri}: " + connectionErrorEvent.Exception.Message);

    /// <inheritdoc/>
    public virtual void OnGameStarted(GameStartedEvent gameStatedEvent) { }

    /// <inheritdoc/>
    public virtual void OnGameEnded(GameEndedEvent gameEndedEvent) { }

    /// <inheritdoc/>
    public virtual void OnRoundStarted(RoundStartedEvent roundStatedEvent) { }

    /// <inheritdoc/>
    public virtual void OnRoundEnded(RoundEndedEvent roundEndedEvent) { }

    /// <inheritdoc/>
    public virtual void OnTick(TickEvent tickEvent) { }

    /// <inheritdoc/>
    public virtual void OnBotDeath(DeathEvent botDeathEvent) { }

    /// <inheritdoc/>
    public virtual void OnDeath(DeathEvent botDeathEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitBot(HitBotEvent botHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitWall(HitWallEvent botHitWallEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletFired(BulletFiredEvent bulletFiredEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitByBullet(BulletHitBotEvent bulletHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletHit(BulletHitBotEvent bulletHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) { }

    /// <inheritdoc/>
    public virtual void OnBulletHitWall(BulletHitWallEvent bulletHitWallEvent) { }

    /// <inheritdoc/>
    public virtual void OnScannedBot(ScannedBotEvent scannedBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnSkippedTurn(SkippedTurnEvent skippedTurnEvent) { }

    /// <inheritdoc/>
    public virtual void OnWonRound(WonRoundEvent wonRoundEvent) { }

    /// <inheritdoc/>
    public virtual void OnCustomEvent(CustomEvent customEvent) { }
  }
}