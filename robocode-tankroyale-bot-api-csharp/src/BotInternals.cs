using System;
using System.Threading;
using System.Collections.Specialized;
using System.Collections;

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

      private bool isCollidingWithWall;
      private bool isCollidingWithBot;
      private bool isOverDriving;

      private TickEvent currentTick;

      internal Thread thread;
      private readonly Object nextTurnLock = new Object();
      private volatile bool isRunning;
      private readonly Object isStoppedLock = new Object();
      private volatile bool isStopped;

      private double savedDistanceRemaining;
      private double savedTurnRemaining;
      private double savedGunTurnRemaining;
      private double savedRadarTurnRemaining;

      private readonly OrderedDictionary pendingCommands = new OrderedDictionary();

      public BotInternals(IBot bot, BotEvents botEvents)
      {
        this.bot = bot;
        this.botEvents = botEvents;

        this.absDeceleration = Math.Abs(bot.Deceleration);

        this.maxSpeed = bot.MaxForwardSpeed;
        this.maxTurnRate = bot.MaxTurnRate;
        this.maxGunTurnRate = bot.MaxGunTurnRate;
        this.maxRadarTurnRate = bot.MaxRadarTurnRate;

        botEvents.onDisconnectedManager.Subscribe(OnDisconnected, 100);
        botEvents.onGameEndedManager.Subscribe(OnGameEnded, 100);
        botEvents.onHitBotManager.Subscribe(OnHitBot, 100);
        botEvents.onHitWallManager.Subscribe(OnHitWall, 100);
        botEvents.onTickManager.Subscribe(OnTick, 100);
        botEvents.onDeathManager.Subscribe(OnDeath, 100);
      }

      internal bool IsRunning
      {
        get => isRunning;
      }

      private void OnDisconnected(DisconnectedEvent evt)
      {
        StopThread();
      }

      private void OnGameEnded(GameEndedEvent evt)
      {
        StopThread();
      }

      private void OnTick(TickEvent evt)
      {
        currentTick = evt;
        ProcessTurn();
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
        isCollidingWithWall = true;
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

        isCollidingWithWall = false;
        isCollidingWithBot = false;

        // If this is the first turn -> Call the run method on the Bot class
        if (currentTick.TurnNumber == 1)
        {
          if (isRunning)
          {
            StopThread();
          }
          StartThread();
        }

        lock (nextTurnLock)
        {
          // Let's go ;-)
          bot.Go();

          // Unblock methods waiting for the next turn
          Monitor.PulseAll(nextTurnLock);
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
        UpdateTurnRemaining();
        UpdateGunTurnRemaining();
        UpdateRadarTurnRemaining();
      }

      private void UpdateTurnRemaining()
      {
        if (isCollidingWithBot)
        {
          return;
        }
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
        if (isCollidingWithWall)
        {
          return;
        }

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
      // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
      internal double GetNewSpeed(double speed, double distance)
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

      internal void Stop()
      {
        lock (isStoppedLock)
        {
          if (!isStopped)
          {
            savedDistanceRemaining = distanceRemaining;
            savedTurnRemaining = turnRemaining;
            savedGunTurnRemaining = gunTurnRemaining;
            savedRadarTurnRemaining = radarTurnRemaining;

            distanceRemaining = 0d;
            turnRemaining = 0d;
            gunTurnRemaining = 0d;
            radarTurnRemaining = 0d;

            bot.TargetSpeed = 0;
            bot.TurnRate = 0;
            bot.GunTurnRate = 0;
            bot.RadarTurnRate = 0;

            isStopped = true;
            Monitor.PulseAll(isStoppedLock);
          }
        }
      }

      internal void Resume()
      {
        lock (isStoppedLock)
        {
          if (isStopped)
          {
            distanceRemaining = savedDistanceRemaining;
            turnRemaining = savedTurnRemaining;
            gunTurnRemaining = savedGunTurnRemaining;
            radarTurnRemaining = savedRadarTurnRemaining;

            isStopped = false;
            Monitor.PulseAll(isStoppedLock);
          }
        }
      }

      private void SetScan(bool doScan)
      {
        ((BaseBot)bot).__baseBotInternals.botIntent.Scan = doScan;
      }

      private bool IsNearZero(double value) => Math.Abs(value) < .00001;

      internal void WaitIfStopped()
      {
        lock (isStoppedLock)
        {
          while (isStopped)
          {
            try
            {
              Monitor.Wait(isStoppedLock);
            }
            catch (ThreadInterruptedException)
            {
              return;
            }
          }
        }
      }

      internal void Await()
      {
        lock (nextTurnLock)
        {
          try
          {
            while (isRunning && pendingCommands.Count > 0)
            {
              // Fetch next pending command
              IDictionaryEnumerator enumerator = pendingCommands.GetEnumerator();
              enumerator.MoveNext();
              var entry = enumerator.Entry;
              Command cmd = (Command)entry.Value;

              // Run the command, if it is not running already
              if (!cmd.IsRunning())
              {
                cmd.Run();
                // Send the bot intend if the bot is now running
                if (cmd.IsRunning())
                {
                  bot.Go();
                }
              }
              // Loop while bot is running and command is not done yet
              while (isRunning && !cmd.IsDone())
              {
                try
                {
                  // Wait for next turn and fire events
                  Monitor.Wait(nextTurnLock);
                  botEvents.FireEvents(currentTick);
                }
                catch (ThreadInterruptedException)
                {
                  isRunning = false;
                }
              }
              // Remove the command
              cmd.BeforeDestroy();
              pendingCommands.Remove(entry.Key);
            }
          }
          catch (Exception ex)
          {
            Console.Error.WriteLine(ex.ToString());
          }
        }
      }

      internal void QueueForward(double distance)
      {
        QueueCommand(new MoveCommand(this, distance));
      }

      internal void QueueTurn(double degrees)
      {
        QueueCommand(new TurnCommand(this, degrees));
      }

      internal void QueueGunTurn(double degrees)
      {
        QueueCommand(new GunTurnCommand(this, degrees));
      }

      internal void QueueRadarTurn(double degrees)
      {
        QueueCommand(new RadarTurnCommand(this, degrees));
      }

      internal void QueueFireGun(double firepower)
      {
        QueueCommand(new FireGunCommand(this, firepower));
      }

      internal void QueueStop()
      {
        QueueCommand(new StopCommand(this));
      }

      internal void QueueResume()
      {
        QueueCommand(new ResumeCommand(this));
      }

      internal void QueueScan()
      {
        QueueCommand(new ScanCommand(this));
      }

      internal void QueueCondition(Condition condition)
      {
        QueueCommand(new ConditionCommand(this, condition));
      }

      internal void FireConditionMet(Condition condition)
      {
        botEvents.FireConditionMet(condition);
      }

      private void QueueCommand(Command command)
      {
        pendingCommands.Remove(command.GetType());
        pendingCommands.Add(command.GetType(), command);
      }

      private abstract class Command
      {
        protected BotInternals outerInstance;

        internal bool isRunning;

        internal Command(BotInternals outerInstance)
        {
          this.outerInstance = outerInstance;
        }

        internal bool IsRunning()
        {
          return isRunning;
        }

        internal abstract void Run(); // must set isRunning

        internal abstract bool IsDone();

        internal virtual void BeforeDestroy() { }
      }

      private sealed class MoveCommand : Command
      {
        readonly double distance;

        internal MoveCommand(BotInternals outerInstance, double distance) : base(outerInstance)
        {
          this.distance = distance;
        }

        internal override void Run()
        {
          outerInstance.bot.SetForward(distance);
          isRunning = true;
        }

        internal override bool IsDone()
        {
          return outerInstance.distanceRemaining == 0;
        }
      }

      private sealed class TurnCommand : Command
      {
        readonly double degrees;

        internal TurnCommand(BotInternals outerInstance, double degrees) : base(outerInstance)
        {
          this.degrees = degrees;
        }

        internal override void Run()
        {
          outerInstance.bot.SetTurnLeft(degrees);
          isRunning = true;
        }

        internal override bool IsDone()
        {
          return outerInstance.turnRemaining == 0;
        }
      }

      private sealed class GunTurnCommand : Command
      {
        readonly double degrees;

        internal GunTurnCommand(BotInternals outerInstance, double degrees) : base(outerInstance)
        {
          this.degrees = degrees;
        }

        internal override void Run()
        {
          outerInstance.bot.SetTurnGunLeft(degrees);
          isRunning = true;
        }

        internal override bool IsDone()
        {
          return outerInstance.gunTurnRemaining == 0;
        }
      }

      private sealed class RadarTurnCommand : Command
      {
        readonly double degrees;

        internal RadarTurnCommand(BotInternals outerInstance, double degrees) : base(outerInstance)
        {
          this.degrees = degrees;
        }

        internal override void Run()
        {
          outerInstance.bot.SetTurnRadarLeft(degrees);
          isRunning = true;
        }

        internal override bool IsDone()
        {
          return outerInstance.radarTurnRemaining == 0;
        }
      }

      private sealed class FireGunCommand : Command
      {
        readonly double firepower;

        internal FireGunCommand(BotInternals outerInstance, double firepower) : base(outerInstance)
        {
          this.firepower = firepower;
        }

        internal override void Run()
        {
          isRunning = outerInstance.bot.SetFire(firepower);
        }

        internal override bool IsDone()
        {
          return outerInstance.bot.GunHeat > 0;
        }
      }

      private sealed class ConditionCommand : Command
      {
        readonly Condition condition;

        internal ConditionCommand(BotInternals outerInstance, Condition condition) : base(outerInstance)
        {
          this.condition = condition;
        }

        internal override void Run()
        {
          isRunning = true;
        }

        internal override bool IsDone()
        {
          return condition.Test();
        }
      }

      private abstract class RunAndAwaitNextTurnCommand : Command
      {
        private readonly int turnNumber;

        internal RunAndAwaitNextTurnCommand(BotInternals outerInstance) : base(outerInstance)
        {
          this.turnNumber = outerInstance.bot.TurnNumber;
        }

        internal override bool IsDone()
        {
          return true;
        }
      }

      private sealed class StopCommand : RunAndAwaitNextTurnCommand
      {
        internal StopCommand(BotInternals outerInstance) : base(outerInstance) { }

        internal override void Run()
        {
          outerInstance.Stop();
          isRunning = true;
        }
      }

      private sealed class ResumeCommand : RunAndAwaitNextTurnCommand
      {
        internal ResumeCommand(BotInternals outerInstance) : base(outerInstance) { }

        internal override void Run()
        {
          outerInstance.Resume();
          isRunning = true;
        }
      }

      private sealed class ScanCommand : RunAndAwaitNextTurnCommand
      {
        internal ScanCommand(BotInternals outerInstance) : base(outerInstance) { }

        internal override void Run()
        {
          outerInstance.SetScan(false);
          isRunning = true;
        }

        internal override void BeforeDestroy()
        {
          outerInstance.bot.SetScan(false);
        }
      }
    }
  }
}