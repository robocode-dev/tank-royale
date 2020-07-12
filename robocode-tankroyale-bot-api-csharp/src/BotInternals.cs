using System;
using System.Threading;

namespace Robocode.TankRoyale.BotApi
{
  public partial class Bot
  {
    internal class BotInternals
    {
      private double absDeceleration;

      private readonly IBot bot;
      private readonly BotEvents botEvents;

      internal double maxSpeed;
      internal double maxTurnRate;
      internal double maxGunTurnRate;
      internal double maxRadarTurnRate;

      internal double distanceRemaining;
      internal double turnRemaining;
      internal double gunTurnRemaining;
      internal double radarTurnRemaining;

      private bool isCollidingWithBot;
      private bool isOverDriving;

      private TickEvent currentTurn;

      internal Thread thread;
      private readonly Object nextTurn = new Object();

      private volatile bool isRunning;

      public BotInternals(IBot bot, BotEvents botEvents)
      {
        this.bot = bot;
        this.botEvents = botEvents;

        this.absDeceleration = Math.Abs(bot.Deceleration);

        this.maxSpeed = bot.MaxForwardSpeed;
        this.maxTurnRate = bot.MaxTurnRate;
        this.maxGunTurnRate = bot.MaxGunTurnRate;
        this.maxRadarTurnRate = bot.MaxRadarTurnRate;

        var internals = ((BaseBot)bot).__internals;

        botEvents.onDisconnectedManager.Add((OnDisconnected));
        botEvents.onGameEndedManager.Add(OnGameEnded);
        botEvents.onHitBotManager.Add(OnHitBot);
        botEvents.onHitWallManager.Add(OnHitWall);
        botEvents.onTickManager.Add(OnTick);
        botEvents.onDeathManager.Add(OnDeath);
      }

      internal bool IsRunning
      {
        get => isRunning;
      }

      private void OnTick(TickEvent evt)
      {
        currentTurn = evt;
        ProcessTurn();
      }

      private void OnDisconnected(DisconnectedEvent evt)
      {
        StopThread();
      }

      private void OnGameEnded(GameEndedEvent evt)
      {
        StopThread();
      }

      private void OnHitBot(BotHitBotEvent evt)
      {
        if (evt.IsRammed)
        {
          distanceRemaining = 0;
        }
        isCollidingWithBot = true;
      }

      private void OnHitWall(BotHitWallEvent evt)
      {
        distanceRemaining = 0;
      }

      private void OnDeath(BotDeathEvent evt)
      {
        if (evt.VictimId == bot.MyId)
        {
          StopThread();
        }
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
        isCollidingWithBot = false;

        // If this is the first turn -> Call the run method on the Bot class
        if (currentTurn.TurnNumber == 1)
        {
          if (isRunning)
          {
            StopThread();
          }
          StartThread();
        }

        lock (nextTurn)
        {
          // Let's go ;-)
          bot.Go();

          // Unblock waiting methods waiting for the next turn
          Monitor.PulseAll(nextTurn);
        }
      }

      private void StartThread()
      {
        thread = new Thread(new ThreadStart(bot.Run));
        thread.Start();
        isRunning = true;
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
        if (!isCollidingWithBot)
        {
          UpdateTurnRemaining();
        }
        UpdateGunTurnRemaining();
        UpdateRadarTurnRemaining();
      }

      private void UpdateTurnRemaining()
      {
        double absTurnRate = Math.Abs(bot.TurnRate);

        double turnRate = Math.Min(absTurnRate, bot.CalcMaxTurnRate(bot.Speed));
        if (bot.TurnRemaining < 0)
        {
          turnRate *= -1;
        }
        if (Math.Abs(bot.TurnRemaining) < absTurnRate)
        {
          if (bot.DoAdjustGunForBodyTurn)
          {
            gunTurnRemaining -= bot.TurnRemaining;
          }
          turnRemaining = 0;
        }
        else
        {
          if (bot.DoAdjustGunForBodyTurn)
          {
            gunTurnRemaining -= turnRate;
          }
          turnRemaining -= turnRate;
        }
        if (turnRemaining > 0)
        {
          bot.TurnRate = Math.Min(maxTurnRate, turnRemaining);
        }
        else
        {
          bot.TurnRate = Math.Max(-maxTurnRate, turnRemaining);
        }
      }

      private void UpdateGunTurnRemaining()
      {
        double absGunTurnRate = Math.Abs(bot.GunTurnRate);

        if (Math.Abs(bot.GunTurnRemaining) < absGunTurnRate)
        {
          if (bot.DoAdjustRadarForGunTurn)
          {
            radarTurnRemaining -= bot.GunTurnRemaining;
          }
          gunTurnRemaining = 0;
        }
        else
        {
          if (bot.DoAdjustRadarForGunTurn)
          {
            radarTurnRemaining -= bot.GunTurnRate;
          }
          gunTurnRemaining -= bot.GunTurnRate;
        }
        if (gunTurnRemaining > 0)
        {
          bot.GunTurnRate = Math.Min(maxGunTurnRate, gunTurnRemaining);
        }
        else
        {
          bot.GunTurnRate = Math.Max(-maxGunTurnRate, gunTurnRemaining);
        }
      }

      private void UpdateRadarTurnRemaining()
      {
        double absRadarTurnRate = Math.Abs(bot.RadarTurnRate);

        if (Math.Abs(bot.RadarTurnRemaining) < absRadarTurnRate)
        {
          radarTurnRemaining = 0;
        }
        else
        {
          radarTurnRemaining -= bot.RadarTurnRate;
        }
        if (radarTurnRemaining > 0)
        {
          bot.RadarTurnRate = Math.Min(maxRadarTurnRate, radarTurnRemaining);
        }
        else
        {
          bot.RadarTurnRate = Math.Max(-maxRadarTurnRate, radarTurnRemaining);
        }
      }

      // This is Nat Pavasants method described here:
      // http://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
      private void UpdateMovement()
      {
        double distance = distanceRemaining;
        if (Double.IsNaN(distance))
        {
          distance = 0;
        }

        var speed = GetNewSpeed(bot.Speed, distance);
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
          isOverDriving = GetDistanceTraveledUntilStop(speed) > Math.Abs(distance);
        }

        distanceRemaining = distance - speed;
      }

      /// <summary>
      /// Returns the new speed based on the current speed and distance to move.
      ///
      /// <param name="speed">Is the current speed</param>
      /// <param name="distance">Is the distance to move</param>
      /// <return>The new speed</return>
      //
      // Credits for this algorithm goes to Patrick Cupka (aka Voidious), Julian Kent (aka
      // Skilgannon), and Positive:
      // http://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
      private double GetNewSpeed(double speed, double distance)
      {
        if (distance < 0)
        {
          // If the distance is negative, then change it to be positive and change the sign of the
          // input velocity and the result
          return -GetNewSpeed(-speed, -distance);
        }

        double targetSpeed;
        if (distance == Double.PositiveInfinity)
        {
          targetSpeed = maxSpeed;
        }
        else
        {
          targetSpeed = Math.Min(GetMaxSpeed(distance), maxSpeed);
        }

        if (speed >= 0)
        {
          return Math.Max(speed - absDeceleration, Math.Min(targetSpeed, speed + bot.Acceleration));
        } // else
        return Math.Max(speed - bot.Acceleration, Math.Min(targetSpeed, speed + GetMaxDeceleration(-speed)));
      }

      private double GetMaxSpeed(double distance)
      {
        var decelTime =
          Math.Max(
            1,
            Math.Ceiling( // sum of 0... decelTime, solving for decelTime using quadratic formula
              (Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));

        if (decelTime == Double.PositiveInfinity)
        {
          return bot.MaxSpeed;
        }

        var decelDist =
          (decelTime / 2) *
          (decelTime - 1) // sum of 0..(decelTime-1)
          *
          absDeceleration;

        return ((decelTime - 1) * absDeceleration) + ((distance - decelDist) / decelTime);
      }

      private double GetMaxDeceleration(double speed)
      {
        var decelTime = speed / absDeceleration;
        var accelTime = (1 - decelTime);

        return Math.Min(1, decelTime) * absDeceleration + Math.Max(0, accelTime) * bot.Acceleration;
      }

      private double GetDistanceTraveledUntilStop(double speed)
      {
        speed = Math.Abs(speed);
        double distance = 0;
        while (speed > 0)
        {
          distance += (speed = GetNewSpeed(speed, 0));
        }
        return distance;
      }

      private bool IsNearZero(double value) => Math.Abs(value) < .00001;

      internal void AwaitMovementComplete()
      {
        Await(() => distanceRemaining == 0);
      }

      internal void AwaitTurnComplete()
      {
        Await(() => turnRemaining == 0);
      }

      internal void awaitGunTurnComplete()
      {
        Await(() => gunTurnRemaining == 0);
      }

      internal void awaitRadarTurnComplete()
      {
        Await(() => radarTurnRemaining == 0);
      }

      private void Await(Func<bool> condition)
      {
        lock (nextTurn)
        {
          // Loop while bot is running and condition has not been met
          while (isRunning && !condition.Invoke())
          {
            try
            {
              // Wait for next turn
              Monitor.Wait(nextTurn);
              botEvents.DispatchEvents(currentTurn);
            }
            catch (ThreadInterruptedException)
            {
              isRunning = false;
            }
          }
        }
      }
    }
  }
}