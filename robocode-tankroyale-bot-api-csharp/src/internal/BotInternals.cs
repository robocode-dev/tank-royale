using System.Linq;
using System;
using System.Threading;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal sealed class BotInternals
  {
    private readonly IBot bot;
    private readonly BaseBotInternals baseBotInternals;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private bool isCollidingWithWall;
    private bool isOverDriving;

    private TickEvent currentTick;

    private Thread thread;
    private readonly Object nextTurnLock = new Object();
    private volatile bool isRunning;
    private volatile bool isStopped;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    public BotInternals(IBot bot, BaseBotInternals baseBotInternals)
    {
      this.bot = bot;
      this.baseBotInternals = baseBotInternals;

      BotEventHandlers botEventHandlers = baseBotInternals.BotEventHandlers;
      botEventHandlers.onProcessTurn.Subscribe(OnProcessTurn, 100);
      botEventHandlers.onDisconnected.Subscribe(OnDisconnected, 100);
      botEventHandlers.onGameEnded.Subscribe(OnGameEnded, 100);
      botEventHandlers.onHitBot.Subscribe(OnHitBot, 100);
      botEventHandlers.onHitWall.Subscribe(OnHitWall, 100);
      botEventHandlers.onDeath.Subscribe(OnDeath, 100);
    }

    private void OnDisconnected(DisconnectedEvent evt)
    {
      StopThread();
    }

    private void OnGameEnded(GameEndedEvent evt)
    {
      StopThread();
    }

    private void OnProcessTurn(TickEvent evt)
    {
      currentTick = evt;
      ProcessTurn();
    }

    private void OnHitBot(HitBotEvent evt)
    {
      if (evt.IsRammed)
      {
        distanceRemaining = 0;
      }
    }

    private void OnHitWall(HitWallEvent evt)
    {
      distanceRemaining = 0;
      isCollidingWithWall = true;
    }

    private void OnDeath(DeathEvent evt)
    {
      if (evt.VictimId == bot.MyId)
      {
        StopThread();
      }
    }

    internal bool IsRunning { get => isRunning; }

    internal double DistanceRemaining { get => distanceRemaining; }

    internal double TurnRemaining { get => turnRemaining; }

    internal double GunTurnRemaining { get => gunTurnRemaining; }

    internal double RadarTurnRemaining { get => radarTurnRemaining; }

    internal void SetForward(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      distanceRemaining = distance;
      double speed = baseBotInternals.GetNewSpeed(bot.Speed, distance);
      bot.TargetSpeed = speed;
    }

    internal void Forward(double distance)
    {
      BlockIfStopped();
      SetForward(distance);
      AwaitMovementComplete();
    }

    internal void SetTurnLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      turnRemaining = degrees;
      bot.TurnRate = degrees;
    }

    internal void TurnLeft(double degrees)
    {
      BlockIfStopped();
      SetTurnLeft(degrees);
      AwaitTurnComplete();
    }

    internal void SetTurnGunLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      gunTurnRemaining = degrees;
      bot.GunTurnRate = degrees;
    }

    internal void TurnGunLeft(double degrees)
    {
      BlockIfStopped();
      SetTurnGunLeft(degrees);
      AwaitGunTurnComplete();
    }

    internal void SetTurnRadarLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      radarTurnRemaining = degrees;
      bot.RadarTurnRate = degrees;
    }

    internal void TurnRadarLeft(double degrees)
    {
      BlockIfStopped();
      SetTurnRadarLeft(degrees);
      AwaitRadarTurnComplete();
    }

    internal void Fire(double firepower)
    {
      if (bot.SetFire(firepower))
      {
        AwaitGunFired();
      }
    }

    internal bool Scan()
    {
      bot.SetScan();
      AwaitNextTurn();

      // If a ScannedBotEvent is put in the events, the bot scanned another bot
      return bot.Events.Any(e => e is ScannedBotEvent);
    }

    private void ProcessTurn()
    {
      // No movement is possible, when the bot has become disabled
      if (bot.IsDisabled)
      {
        distanceRemaining = 0;
        turnRemaining = 0;
      }
      UpdateHeadings();
      UpdateMovement();

      // Reset collision flag after updating movement
      isCollidingWithWall = false;

      // If this is the first turn -> Call the run method on the Bot class
      if (currentTick.TurnNumber == 1) // TODO: Use onNewRound event?
      {
        if (isRunning)
        {
          StopThread();
        }
        StartThread();
      }

      lock (nextTurnLock)
      {
        // Unblock methods waiting for the next turn
        Monitor.PulseAll(nextTurnLock);
      }
    }

    private void StartThread()
    {
      thread = new Thread(new ThreadStart(bot.Run));
      isRunning = true; // Set this before the thread is starting as Run() needs it to be set
      thread.Start();
    }

    private void StopThread()
    {
      if (thread != null)
      {
        isRunning = false;
        thread.Interrupt();
        try
        {
          thread.Join();
        }
        catch (ThreadInterruptedException) { }
        thread = null;
      }
    }

    /** Updates the bot heading, gun heading, and radar heading. */
    private void UpdateHeadings()
    {
      UpdateTurnRemaining();
      UpdateGunTurnRemaining();
      UpdateRadarTurnRemaining();
    }

    private void UpdateTurnRemaining()
    {
      if (bot.DoAdjustGunForBodyTurn)
      {
        gunTurnRemaining -= bot.TurnRate;
      }
      turnRemaining -= bot.TurnRate;

      bot.TurnRate = turnRemaining;
    }

    private void UpdateGunTurnRemaining()
    {
      if (bot.DoAdjustRadarForGunTurn)
      {
        radarTurnRemaining -= bot.GunTurnRate;
      }
      gunTurnRemaining -= bot.GunTurnRate;

      bot.GunTurnRate = gunTurnRemaining;
    }

    private void UpdateRadarTurnRemaining()
    {
      radarTurnRemaining -= bot.RadarTurnRate;

      bot.RadarTurnRate = radarTurnRemaining;
    }

    // This is Nat Pavasant's method described here:
    // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
    private void UpdateMovement()
    {
      if (isCollidingWithWall) // TODO: add check for collision with bot?
      {
        return;
      }

      double distance = distanceRemaining;
      if (Double.IsNaN(distance))
      {
        distance = 0;
      }

      var speed = baseBotInternals.GetNewSpeed(bot.Speed, distance);
      bot.TargetSpeed = speed;

      // If we are over-driving our distance and we are now at velocity=0 then we stopped
      if (IsNearZero(speed) && isOverDriving)
      {
        distanceRemaining = 0;
        distance = 0;
        isOverDriving = false;
      }

      // If we are moving normally and the breaking distance is more than remaining distance, enable
      // the overdrive flag
      if (Math.Sign(distance * speed) != -1)
      {
        isOverDriving = baseBotInternals.GetDistanceTraveledUntilStop(speed) > Math.Abs(distance);
      }

      distanceRemaining = distance - speed;
    }

    internal bool IsStopped { get => isStopped; }

    internal void Stop()
    {
      SetStop();
      AwaitNextTurn();
    }

    internal void Resume()
    {
      SetResume();
      AwaitNextTurn();
    }

    internal void SetStop()
    {
      if (!isStopped)
      {
        isStopped = true;

        savedDistanceRemaining = distanceRemaining;
        savedTurnRemaining = turnRemaining;
        savedGunTurnRemaining = gunTurnRemaining;
        savedRadarTurnRemaining = radarTurnRemaining;
      }
      distanceRemaining = 0d;
      turnRemaining = 0d;
      gunTurnRemaining = 0d;
      radarTurnRemaining = 0d;

      bot.TargetSpeed = 0;
      bot.TurnRate = 0;
      bot.GunTurnRate = 0;
      bot.RadarTurnRate = 0;
    }

    internal void SetResume()
    {
      if (isStopped)
      {
        isStopped = false;

        distanceRemaining = savedDistanceRemaining;
        turnRemaining = savedTurnRemaining;
        gunTurnRemaining = savedGunTurnRemaining;
        radarTurnRemaining = savedRadarTurnRemaining;
      }
    }

    private bool IsNearZero(double value) => Math.Abs(value) < .00001;

    private void BlockIfStopped()
    {
      if (isStopped)
      {
        Await(() => !isStopped);
      }
    }

    private void AwaitMovementComplete()
    {
      Await(() => distanceRemaining == 0);
    }

    private void AwaitTurnComplete()
    {
      Await(() => turnRemaining == 0);
    }

    private void AwaitGunTurnComplete()
    {
      Await(() => gunTurnRemaining == 0);
    }

    private void AwaitRadarTurnComplete()
    {
      Await(() => radarTurnRemaining == 0);
    }

    private void AwaitGunFired()
    {
      Await(() => bot.GunHeat > 0);
    }

    private void AwaitNextTurn()
    {
      int turnNumber = bot.TurnNumber;
      Await(() => bot.TurnNumber > turnNumber);
    }

    internal void Await(Test test)
    {
      lock (nextTurnLock)
      {
        try
        {
          while (isRunning && !test.Invoke())
          {
            bot.Go();
            Monitor.Wait(nextTurnLock); // Wait for next turn
          }
        }
        catch (Exception)
        {
          isRunning = false;
          isStopped = false;
        }
      }
    }

    internal delegate bool Test();
  }
}