using System;
using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract Bot containing convenient methods for movement, turning, and firing the gun.
  /// Most bots should inherit from this class.
  /// </summary>
  public partial class BaseBot : IBaseBot
  {
    internal readonly BaseBotInternals __baseBotInternals;

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot, which should be used when both
    /// BotInfo and server URI is provided through environment variables, i.e., when starting up
    /// the bot using a bootstrap. These environment variables must be set to provide the server
    /// URL and bot information, and are automatically set by the bootstrap tool for Robocode.
    /// </summary>
    /// <example>
    /// ROBOCODE_SERVER_URI=ws://localhost:55000
    /// BOT_NAME=MyBot
    /// BOT_VERSION=1.0
    /// BOT_AUTHOR=fnl
    /// BOT_DESCRIPTION=Sample bot
    /// BOT_URL=https://mybot.robocode.dev
    /// BOT_COUNTRY_CODE=DK
    /// BOT_GAME_TYPES=melee,1v1
    /// BOT_PROG_PLATFORM=.Net Core 3.1
    /// BOT_PROG_LANG=C# 8
    /// </example>
    public BaseBot()
    {
      __baseBotInternals = new BaseBotInternals(this, null, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot, which should be used when
    /// server URI is provided through the environment variable ROBOCODE_SERVER_URL.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    public BaseBot(BotInfo botInfo)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, null);
    }

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot, which should be used providing
    /// both the bot information and server URL for your bot.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URI</param>
    public BaseBot(BotInfo botInfo, Uri serverUrl)
    {
      __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl);
    }

    public void Start()
    {
      __baseBotInternals.Connect();
      __baseBotInternals.exitEvent.WaitOne();
    }

    public void Go()
    {
      __baseBotInternals.SendIntent();
    }

    public String Variant
    {
      get => __baseBotInternals.ServerHandshake.Variant;
    }

    public String Version
    {
      get => __baseBotInternals.ServerHandshake.Version;
    }

    public int MyId
    {
      get => __baseBotInternals.MyId;
    }

    public String GameType
    {
      get => __baseBotInternals.GameSetup.GameType;
    }

    public int ArenaWidth
    {
      get => __baseBotInternals.GameSetup.ArenaWidth;
    }

    public int ArenaHeight
    {
      get => __baseBotInternals.GameSetup.ArenaHeight;
    }

    public int NumberOfRounds
    {
      get => __baseBotInternals.GameSetup.NumberOfRounds;
    }

    public double GunCoolingRate
    {
      get => __baseBotInternals.GameSetup.GunCoolingRate;
    }

    public int? MaxInactivityTurns
    {
      get => __baseBotInternals.GameSetup.MaxInactivityTurns;
    }

    public int TurnTimeout
    {
      get => __baseBotInternals.GameSetup.TurnTimeout;
    }

    public int TimeLeft
    {
      get
      {
        long passesMicroSeconds = (DateTime.Now.Ticks - __baseBotInternals.TicksStart) / 10;
        return (int)(__baseBotInternals.GameSetup.TurnTimeout - passesMicroSeconds);
      }
    }

    public int RoundNumber
    {
      get => __baseBotInternals.CurrentTurn.RoundNumber;
    }

    public int TurnNumber
    {
      get => __baseBotInternals.CurrentTurn.TurnNumber;
    }

    public double Energy
    {
      get => __baseBotInternals.CurrentTurn.BotState.Energy;
    }

    public bool IsDisabled
    {
      get => Energy == 0;
    }

    public double X
    {
      get => __baseBotInternals.CurrentTurn.BotState.X;
    }

    public double Y
    {
      get => __baseBotInternals.CurrentTurn.BotState.Y;
    }

    public double Direction
    {
      get => __baseBotInternals.CurrentTurn.BotState.Direction;
    }

    public double GunDirection
    {
      get => __baseBotInternals.CurrentTurn.BotState.GunDirection;
    }

    public double RadarDirection
    {
      get => __baseBotInternals.CurrentTurn.BotState.RadarDirection;
    }

    public double Speed
    {
      get => __baseBotInternals.CurrentTurn.BotState.Speed;
    }

    public double GunHeat
    {
      get => __baseBotInternals.CurrentTurn.BotState.GunHeat;
    }

    public IEnumerable<BulletState> BulletStates
    {
      get => __baseBotInternals.CurrentTurn.BulletStates;
    }

    public IEnumerable<Event> Events
    {
      get => __baseBotInternals.CurrentTurn.Events;
    }

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
          value = ((IBaseBot)this).MaxTurnRate * (value > 0 ? 1 : -1);
        }
        __baseBotInternals.BotIntent.TurnRate = value;
      }
      get => __baseBotInternals.BotIntent.TurnRate ?? 0d;
    }

    public double GunTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("GunTurnRate cannot be NaN");
        }
        if (IsAdjustGunForBodyTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxGunTurnRate)
        {
          value = ((IBaseBot)this).MaxGunTurnRate * (value > 0 ? 1 : -1);
        }
        __baseBotInternals.BotIntent.GunTurnRate = value;
      }
      get => __baseBotInternals.BotIntent.GunTurnRate ?? 0d;
    }

    public double RadarTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("RadarTurnRate cannot be NaN");
        }
        if (IsAdjustRadarForGunTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxRadarTurnRate)
        {
          value = ((IBaseBot)this).MaxRadarTurnRate * (value > 0 ? 1 : -1);
        }
        __baseBotInternals.BotIntent.RadarTurnRate = value;
      }
      get => __baseBotInternals.BotIntent.RadarTurnRate ?? 0d;
    }

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

    public double Firepower
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("Firepower cannot be NaN");
        }
        if (GunHeat == 0)
        {
          if (value < ((IBaseBot)this).MinFirepower)
          {
            value = 0;
          }
          else if (value > ((IBaseBot)this).MaxFirepower)
          {
            value = ((IBaseBot)this).MaxFirepower;
          }
          __baseBotInternals.BotIntent.Firepower = value;
        }
      }
      get => __baseBotInternals.BotIntent.Firepower ?? 0d;
    }

    public bool IsAdjustGunForBodyTurn
    {
      set => __baseBotInternals.isAdjustGunForBodyTurn = value;
      get => __baseBotInternals.isAdjustGunForBodyTurn;
    }

    public bool IsAdjustRadarForGunTurn
    {
      set => __baseBotInternals.isAdjustRadarForGunTurn = value;
      get => __baseBotInternals.isAdjustRadarForGunTurn;
    }

    public double CalcMaxTurnRate(double speed)
    {
      return ((IBaseBot)this).MaxTurnRate - 0.75 * Math.Abs(speed);
    }

    public double CalcBulletSpeed(double firepower)
    {
      return 20 - 3 * firepower;
    }

    public double CalcGunHeat(double firepower)
    {
      return 1 + (firepower / 5);
    }

    public virtual void OnConnected(ConnectedEvent connectedEvent) { }

    public virtual void OnDisconnected(DisconnectedEvent disconnectedEvent) { }

    public virtual void OnConnectionError(ConnectionErrorEvent connectionErrorEvent) { }

    public virtual void OnGameStarted(GameStartedEvent gameStatedEvent) { }

    public virtual void OnGameEnded(GameEndedEvent gameEndedEvent) { }

    public virtual void OnTick(TickEvent tickEvent) { }

    public virtual void OnBotDeath(BotDeathEvent botDeathEvent) { }

    public virtual void OnHitBot(BotHitBotEvent botHitBotEvent) { }

    public virtual void OnHitWall(BotHitWallEvent botHitWallEvent) { }

    public virtual void OnBulletFired(BulletFiredEvent bulletFiredEvent) { }

    public virtual void OnHitByBullet(BulletHitBotEvent bulletHitBotEvent) { }

    public virtual void OnBulletHit(BulletHitBotEvent bulletHitBotEvent) { }

    public virtual void OnBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent) { }

    public virtual void OnBulletHitWall(BulletHitWallEvent bulletHitWallEvent) { }

    public virtual void OnScannedBot(ScannedBotEvent scannedBotEvent) { }

    public virtual void OnSkippedTurn(SkippedTurnEvent skippedTurnEvent) { }

    public virtual void OnWonRound(WonRoundEvent wonRoundEvent) { }
  }
}