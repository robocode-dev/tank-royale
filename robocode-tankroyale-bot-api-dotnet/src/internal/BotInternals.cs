using System.Linq;
using System;
using System.Threading;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal sealed class BotInternals : IStopResumeListener
  {
    private readonly IBot bot;
    private readonly BaseBotInternals baseBotInternals;

    private Thread thread;
    private readonly Object threadMonitor = new Object();
    private bool isRunning;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private bool isOverDriving;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    public BotInternals(IBot bot, BaseBotInternals baseBotInternals)
    {
      this.bot = bot;
      this.baseBotInternals = baseBotInternals;

      BotEventHandlers botEventHandlers = baseBotInternals.BotEventHandlers;
      botEventHandlers.onNextTurn.Subscribe(OnNextTurn, 100);
      botEventHandlers.onRoundStarted.Subscribe(OnRoundStarted, 100);
      botEventHandlers.onRoundEnded.Subscribe(OnRoundEnded, 100);
      botEventHandlers.onGameEnded.Subscribe(OnGameEnded, 100);
      botEventHandlers.onDisconnected.Subscribe(OnDisconnected, 100);
      botEventHandlers.onHitWall.Subscribe(OnHitWall, 100);
      botEventHandlers.onHitBot.Subscribe(OnHitBot, 100);
      botEventHandlers.onDeath.Subscribe(OnDeath, 100);
    }

    private void OnNextTurn(TickEvent evt)
    {
      if (evt.TurnNumber == 1)
      {
        StopThread(); // sanity before starting a new thread (later)
        StartThread();
      }
      ProcessTurn();
    }

    private void OnRoundStarted(RoundStartedEvent evt)
    {
      ClearRemainings();
    }

    private void ClearRemainings()
    {
      distanceRemaining = 0d;
      turnRemaining = 0d;
      gunTurnRemaining = 0d;
      radarTurnRemaining = 0d;
    }

    private void OnRoundEnded(RoundEndedEvent evt)
    {
      StopThread();
    }

    private void OnGameEnded(GameEndedEvent evt)
    {
      StopThread();
    }

    private void OnDisconnected(DisconnectedEvent evt)
    {
      StopThread();
    }

    private void ProcessTurn()
    {
      // No movement is possible, when the bot has become disabled
      if (bot.IsDisabled)
      {
        ClearRemainings();
      }
      else
      {
        UpdateTurnRemaining();
        UpdateGunTurnRemaining();
        UpdateRadarTurnRemaining();
        UpdateMovement();
      }
    }

    private void StartThread()
    {
      lock (threadMonitor)
      {
        thread = new Thread(new ThreadStart(bot.Run));
        isRunning = true; // before starting thread!
        thread.Start();
      }
    }

    private void StopThread()
    {
      lock (threadMonitor)
      {
        if (thread != null)
        {
          isRunning = false;
          try
          {
            thread.Join(TimeSpan.FromMilliseconds(100));
          }
          finally
          {
            thread = null;
          }
        }
      }
    }

    private void OnHitWall(HitWallEvent evt)
    {
      distanceRemaining = 0;
    }

    private void OnHitBot(HitBotEvent evt)
    {
      if (evt.IsRammed)
        distanceRemaining = 0;
    }

    private void OnDeath(DeathEvent evt)
    {
      if (evt.VictimId == bot.MyId)
        StopThread();
    }

    internal bool IsRunning { get => isRunning; }

    internal double DistanceRemaining { get => distanceRemaining; }

    internal double TurnRemaining { get => turnRemaining; }

    internal double GunTurnRemaining { get => gunTurnRemaining; }

    internal double RadarTurnRemaining { get => radarTurnRemaining; }

    internal void SetTargetSpeed(double targetSpeed)
    {
      if (Double.IsNaN(targetSpeed))
        throw new ArgumentException("targetSpeed cannot be NaN");

      if (targetSpeed > 0)
        distanceRemaining = Double.PositiveInfinity;
      else if (targetSpeed < 0)
        distanceRemaining = Double.NegativeInfinity;
      else
        distanceRemaining = 0;

      baseBotInternals.BotIntent.TargetSpeed = targetSpeed;
    }

    internal void SetForward(double distance)
    {
      if (Double.IsNaN(distance))
        throw new ArgumentException("distance cannot be NaN");

      distanceRemaining = distance;
      double speed = baseBotInternals.GetNewSpeed(bot.Speed, distance);
      baseBotInternals.BotIntent.TargetSpeed = speed;
    }

    internal void Forward(double distance)
    {
      if (bot.IsStopped)
        bot.Go();
      else
      {
        SetForward(distance);
        do
          bot.Go();
        while (distanceRemaining != 0);
      }
    }

    internal void SetTurnLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
        throw new ArgumentException("degrees cannot be NaN");

      turnRemaining = degrees;
      baseBotInternals.BotIntent.TurnRate = degrees;
    }

    internal void TurnLeft(double degrees)
    {
      if (bot.IsStopped)
        bot.Go();
      else
      {
        SetTurnLeft(degrees);
        do
          bot.Go();
        while (turnRemaining != 0);
      }
    }

    internal void SetTurnGunLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
        throw new ArgumentException("degrees cannot be NaN");

      gunTurnRemaining = degrees;
      baseBotInternals.BotIntent.GunTurnRate = degrees;
    }

    internal void TurnGunLeft(double degrees)
    {
      if (bot.IsStopped)
        bot.Go();
      else
      {
        SetTurnGunLeft(degrees);
        do
          bot.Go();
        while (gunTurnRemaining != 0);
      }
    }

    internal void SetTurnRadarLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
        throw new ArgumentException("degrees cannot be NaN");

      radarTurnRemaining = degrees;
      baseBotInternals.BotIntent.RadarTurnRate = degrees;
    }

    internal void TurnRadarLeft(double degrees)
    {
      if (bot.IsStopped)
        bot.Go();
      else
      {
        SetTurnRadarLeft(degrees);
        do
          bot.Go();
        while (radarTurnRemaining != 0);
      }
    }

    internal void Fire(double firepower)
    {
      if (bot.SetFire(firepower))
        bot.Go();
    }

    internal void Scan()
    {
      bot.SetScan();
      bool scan = baseBotInternals.BotIntent.Scan == true;
      bot.Go();

      if (scan && bot.Events.Any(e => e is ScannedBotEvent))
        throw new RescanException();
    }

    internal void WaitFor(Condition condition)
    {
      while (!condition.Test())
        bot.Go();
    }

    internal void Stop()
    {
      baseBotInternals.SetStop();
      bot.Go();
    }

    internal void Resume()
    {
      baseBotInternals.SetResume();
      bot.Go();
    }

    public void OnStop()
    {
      savedDistanceRemaining = distanceRemaining;
      savedTurnRemaining = turnRemaining;
      savedGunTurnRemaining = gunTurnRemaining;
      savedRadarTurnRemaining = radarTurnRemaining;
    }

    public void OnResume()
    {
      distanceRemaining = savedDistanceRemaining;
      turnRemaining = savedTurnRemaining;
      gunTurnRemaining = savedGunTurnRemaining;
      radarTurnRemaining = savedRadarTurnRemaining;
    }

    private void UpdateTurnRemaining()
    {
      double turnRate = bot.TurnRate;
      if (Math.Abs(turnRemaining) <= Math.Abs(turnRate))
      {
        turnRate = turnRemaining;
        bot.TurnRate = turnRate;
      }
      if (bot.DoAdjustGunForBodyTurn)
        gunTurnRemaining -= turnRate;

      turnRemaining -= turnRate;
      if (Math.Abs(turnRemaining) <= Math.Abs(turnRate))
      {
        turnRate = turnRemaining;
        bot.TurnRate = turnRate;
      }
    }

    private void UpdateGunTurnRemaining()
    {
      double gunTurnRate = bot.GunTurnRate;
      if (Math.Abs(gunTurnRemaining) <= Math.Abs(gunTurnRate))
      {
        gunTurnRate = gunTurnRemaining;
        bot.GunTurnRate = gunTurnRate;
      }
      if (bot.DoAdjustRadarForGunTurn)
        radarTurnRemaining -= gunTurnRate;

      gunTurnRemaining -= gunTurnRate;
      if (Math.Abs(gunTurnRemaining) <= Math.Abs(gunTurnRate))
      {
        gunTurnRate = gunTurnRemaining;
        bot.GunTurnRate = gunTurnRate;
      }
    }

    private void UpdateRadarTurnRemaining()
    {
      double radarTurnRate = bot.RadarTurnRate;
      if (Math.Abs(radarTurnRemaining) <= Math.Abs(radarTurnRate))
      {
        radarTurnRate = radarTurnRemaining;
        bot.RadarTurnRate = radarTurnRate;
      }

      radarTurnRemaining -= radarTurnRate;
      if (Math.Abs(radarTurnRemaining) <= Math.Abs(radarTurnRate))
      {
        radarTurnRate = radarTurnRemaining;
        bot.RadarTurnRate = radarTurnRate;
      }
    }

    private void UpdateMovement()
    {
      if (Double.IsInfinity(distanceRemaining))
      {
        baseBotInternals.BotIntent.TargetSpeed =
          (distanceRemaining == Double.PositiveInfinity) ?
            (double)((IBaseBot)bot).MaxSpeed :
            -(double)((IBaseBot)bot).MaxSpeed;
      }
      else
      {
        double distance = distanceRemaining;

        // This is Nat Pavasant's method described here:
        // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
        double speed = baseBotInternals.GetNewSpeed(bot.Speed, distance);
        baseBotInternals.BotIntent.TargetSpeed = speed;

        // If we are over-driving our distance and we are now at velocity=0 then we stopped
        if (IsNearZero(speed) && isOverDriving)
        {
          distanceRemaining = 0;
          distance = 0;
          isOverDriving = false;
        }

        // the overdrive flag
        if (Math.Sign(distance * speed) != -1)
          isOverDriving = baseBotInternals.GetDistanceTraveledUntilStop(speed) > Math.Abs(distance);

        distanceRemaining = distance - speed;
      }
    }

    private bool IsNearZero(double value) => Math.Abs(value) < .00001;
  }
}