using System;
using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract bot class that takes care of communication between the bot and the server, and sends
  /// notifications through the event handlers. Most bots can inherit from this class to get access
  /// to basic methods.
  /// </summary>
  public partial class BaseBot : IBaseBot
  {
    internal readonly BaseBotInternals __baseBotInternals;

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class, which should be used when
    /// both BotInfo and server URI is provided through environment variables, i.e., when starting
    /// up the bot using a bootstrap. These environment variables must be set to provide the server
    /// URL and bot information, and are automatically set by the bootstrap tool for Robocode.
    ///
    /// Example of how to set the predefined environment variables:
    ///
    /// ROBOCODE_SERVER_URI=ws://localhost<br/>
    /// BOT_NAME=MyBot<br/>
    /// BOT_VERSION=1.0<br/>
    /// BOT_AUTHOR=fnl<br/>
    /// BOT_DESCRIPTION=Sample bot<br/>
    /// BOT_URL=https://mybot.somewhere.net<br/>
    /// BOT_COUNTRY_CODE=DK<br/>
    /// BOT_GAME_TYPES=melee,1v1<br/>
    /// BOT_PROG_PLATFORM=.Net Core 3.1<br/>
    /// BOT_PROG_LANG=C# 8<br/>
    /// </summary>
    public BaseBot()
    {
      __baseBotInternals = new BaseBotInternals(this, null, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class, which should be used when
    /// server URI is provided through the environment variable ROBOCODE_SERVER_URL.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    public BaseBot(BotInfo botInfo)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class, which should be used providing
    /// both the bot information and server URL for your bot.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URI</param>
    public BaseBot(BotInfo botInfo, Uri serverUrl)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl);
    }

    /// <inheritdoc/>
    public void Start()
    {
      __baseBotInternals.Connect();
      __baseBotInternals.exitEvent.WaitOne();
    }

    /// <inheritdoc/>
    public void Go()
    {
      __baseBotInternals.SendIntent();
    }

    /// <inheritdoc/>
    public String Variant
    {
      get => __baseBotInternals.ServerHandshake.Variant;
    }

    /// <inheritdoc/>
    public String Version
    {
      get => __baseBotInternals.ServerHandshake.Version;
    }

    /// <inheritdoc/>
    public int MyId
    {
      get => __baseBotInternals.MyId;
    }

    /// <inheritdoc/>
    public String GameType
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
      get
      {
        long passesMicroSeconds = (DateTime.Now.Ticks - __baseBotInternals.TicksStart) / 10;
        return (int)(__baseBotInternals.GameSetup.TurnTimeout - passesMicroSeconds);
      }
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

    /// <inheritdoc>
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
    public IEnumerable<Event> Events
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
        if (Math.Abs(value) > ((IBaseBot)this).MaxTurnRate)
        {
          value = ((IBaseBot)this).MaxTurnRate * Math.Sign(value);
        }
        __baseBotInternals.BotIntent.TurnRate = value;
      }
      get => __baseBotInternals.BotIntent.TurnRate ?? 0d;
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
        if (DoAdjustGunForBodyTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxGunTurnRate)
        {
          value = ((IBaseBot)this).MaxGunTurnRate * Math.Sign(value);
        }
        __baseBotInternals.BotIntent.GunTurnRate = value;
      }
      get => __baseBotInternals.BotIntent.GunTurnRate ?? 0d;
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
        if (DoAdjustRadarForGunTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxRadarTurnRate)
        {
          value = ((IBaseBot)this).MaxRadarTurnRate * Math.Sign(value);
        }
        __baseBotInternals.BotIntent.RadarTurnRate = value;
      }
      get => __baseBotInternals.BotIntent.RadarTurnRate ?? 0d;
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
        if (value > ((IBaseBot)this).MaxForwardSpeed)
        {
          value = ((IBaseBot)this).MaxForwardSpeed;
        }
        else if (value < ((IBaseBot)this).MaxBackwardSpeed)
        {
          value = ((IBaseBot)this).MaxBackwardSpeed;
        }
        __baseBotInternals.BotIntent.TargetSpeed = value;
      }
      get => __baseBotInternals.BotIntent.TargetSpeed ?? 0d;
    }

    /// <inheritdoc/>
    public bool SetFirepower(double firepower)
    {
      if (Double.IsNaN(firepower))
      {
        throw new ArgumentException("Firepower cannot be NaN");
      }
      if (GunHeat > 0)
      {
        return false; // cannot fire yet
      }
      if (firepower < ((IBaseBot)this).MinFirepower)
      {
        firepower = 0;
      }
      else if (firepower > ((IBaseBot)this).MaxFirepower)
      {
        firepower = ((IBaseBot)this).MaxFirepower;
      }
      __baseBotInternals.BotIntent.Firepower = firepower;
      return true;
    }

    /// <inheritdoc/>
    public bool DoAdjustGunForBodyTurn
    {
      set => __baseBotInternals.doAdjustGunForBodyTurn = value;
      get => __baseBotInternals.doAdjustGunForBodyTurn;
    }

    /// <inheritdoc/>
    public bool DoAdjustRadarForGunTurn
    {
      set => __baseBotInternals.doAdjustRadarForGunTurn = value;
      get => __baseBotInternals.doAdjustRadarForGunTurn;
    }

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
    public double CalcBulletSpeed(double firepower) => 20 - 3 * firepower;

    /// <inheritdoc/>
    public double CalcGunHeat(double firepower) => 1 + (firepower / 5);

    /// <inheritdoc/>
    public double CalcBearing(double direction) => NormalizeRelativeDegrees(direction - Direction);

    /// <inheritdoc/>
    public double CalcGunBearing(double direction) => NormalizeRelativeDegrees(direction - GunDirection);

    /// <inheritdoc/>
    public double CalcRadarBearing(double direction) => NormalizeRelativeDegrees(direction - RadarDirection);

    /// <inheritdoc/>
    public double NormalizeAbsoluteDegrees(double angle) => (angle %= 360) >= 0 ? angle : (angle + 360);

    /// <inheritdoc/>
    public double DirectionTo(double x, double y)
    {
      var dx = x - X;
      var dy = y - Y;
      return Math.Sqrt(dx * dx + dy * dy);
    }

    /// <inheritdoc/>
    public double DistanceTo(double x, double y) => NormalizeAbsoluteDegrees(Math.Atan2(x - X, y - Y));

    /// <inheritdoc/>
    public double NormalizeRelativeDegrees(double angle) => (angle %= 360) >= 0 ?
        ((angle < 180) ? angle : (angle - 360)) :
        ((angle >= -180) ? angle : (angle + 360));

    /// <inheritdoc/>
    public virtual void OnConnected(ConnectedEvent connectedEvent) => Console.WriteLine($"Connected to: {connectedEvent.ServerUri}");

    /// <inheritdoc/>
    public virtual void OnDisconnected(DisconnectedEvent disconnectedEvent) => Console.WriteLine($"Disconnected from: {disconnectedEvent.ServerUri}");

    /// <inheritdoc/>
    public virtual void OnConnectionError(ConnectionErrorEvent connectionErrorEvent) => Console.Error.WriteLine($"Connection error with: {connectionErrorEvent.ServerUri}");

    /// <inheritdoc/>
    public virtual void OnGameStarted(GameStartedEvent gameStatedEvent) { }

    /// <inheritdoc/>
    public virtual void OnGameEnded(GameEndedEvent gameEndedEvent) { }

    /// <inheritdoc/>
    public virtual void OnTick(TickEvent tickEvent) { }

    /// <inheritdoc/>
    public virtual void OnBotDeath(BotDeathEvent botDeathEvent) { }

    /// <inheritdoc/>
    public virtual void OnDeath(BotDeathEvent botDeathEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitBot(BotHitBotEvent botHitBotEvent) { }

    /// <inheritdoc/>
    public virtual void OnHitWall(BotHitWallEvent botHitWallEvent) { }

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
    public virtual void OnCondition(Condition condition) { }
  }
}