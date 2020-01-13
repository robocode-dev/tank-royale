using System;
using System.Threading;

namespace Robocode.TankRoyale.BotApi
{
  abstract class Bot : BaseBot, IBot
  {
    readonly __BotInternals __internals;

    public Bot() : base()
    {
      __internals = new __BotInternals(this);
    }

    public Bot(BotInfo botInfo) : base(botInfo)
    {
      __internals = new __BotInternals(this);
    }

    public Bot(BotInfo botInfo, Uri serverUri) : base(botInfo, serverUri)
    {
      __internals = new __BotInternals(this);
    }

    public bool IsRunning()
    {
      return __internals.IsRunning;
    }

    public void SetForward(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __internals.distanceRemaining = distance;
    }

    public void Forward(double distance)
    {
      SetForward(distance);
      Go();
      __internals.AwaitMovementComplete();
    }

    public void SetBack(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __internals.distanceRemaining = -distance;
    }

    public void Back(double distance)
    {
      SetBack(distance);
      Go();
      __internals.AwaitMovementComplete();
    }

    public double DistanceRemaining => __internals.distanceRemaining;

    public void SetMaxSpeed(double maxSpeed)
    {
      if (maxSpeed < 0)
      {
        maxSpeed = 0;
      }
      else if (maxSpeed > ((IBaseBot)this).MaxSpeed)
      {
        maxSpeed = ((IBaseBot)this).MaxSpeed;
      }
      __internals.maxSpeed = maxSpeed;
    }

    public void SetTurnLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __internals.turnRemaining = degrees;
    }

    public void TurnLeft(double degrees)
    {
      SetTurnLeft(degrees);
      Go();
      __internals.AwaitTurnComplete();
    }

    public void SetTurnRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __internals.turnRemaining = -degrees;
    }

    public void TurnRight(double degrees)
    {
      SetTurnRight(degrees);
      Go();
      __internals.AwaitTurnComplete();
    }

    public double TurnRemaining => __internals.turnRemaining;

    public void SetMaxTurnRate(double maxTurnRate)
    {
      if (maxTurnRate < 0)
      {
        maxTurnRate = 0;
      }
      else if (maxTurnRate > ((IBaseBot)this).MaxTurnRate)
      {
        maxTurnRate = ((IBaseBot)this).MaxTurnRate;
      }
      __internals.maxTurnRate = maxTurnRate;
    }

    public void SetTurnGunLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __internals.gunTurnRemaining = degrees;
    }

    public void TurnGunLeft(double degrees)
    {
      SetTurnGunLeft(degrees);
      Go();
      __internals.awaitGunTurnComplete();
    }

    public void SetTurnGunRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __internals.gunTurnRemaining = -degrees;
    }

    public void TurnGunRight(double degrees)
    {
      SetTurnGunRight(degrees);
      Go();
      __internals.awaitGunTurnComplete();
    }

    public double GunTurnRemaining => __internals.gunTurnRemaining;

    public void SetMaxGunTurnRate(double maxGunTurnRate)
    {
      if (maxGunTurnRate < 0)
      {
        maxGunTurnRate = 0;
      }
      else if (maxGunTurnRate > ((IBaseBot)this).MaxGunTurnRate)
      {
        maxGunTurnRate = ((IBaseBot)this).MaxGunTurnRate;
      }
      __internals.maxGunTurnRate = maxGunTurnRate;
    }

    public void SetTurnRadarLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __internals.radarTurnRemaining = degrees;
    }

    public void TurnRadarLeft(double degrees)
    {
      SetTurnRadarLeft(degrees);
      Go();
      __internals.awaitRadarTurnComplete();
    }

    public void SetTurnRadarRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __internals.radarTurnRemaining = -degrees;
    }

    public void TurnRadarRight(double degrees)
    {
      SetTurnRadarRight(degrees);
      Go();
      __internals.awaitRadarTurnComplete();
    }

    public double RadarTurnRemaining => __internals.radarTurnRemaining;

    public void SetMaxRadarTurnRate(double maxRadarTurnRate)
    {
      if (maxRadarTurnRate < 0)
      {
        maxRadarTurnRate = 0;
      }
      else if (maxRadarTurnRate > ((IBaseBot)this).MaxRadarTurnRate)
      {
        maxRadarTurnRate = ((IBaseBot)this).MaxRadarTurnRate;
      }
      __internals.maxRadarTurnRate = maxRadarTurnRate;
    }

    public void Fire(double firepower)
    {
      Firepower = firepower;
      Go();
    }

    internal class __BotInternals
    {
      private IBot parent;

      internal double maxSpeed;
      internal double maxTurnRate;
      internal double maxGunTurnRate;
      internal double maxRadarTurnRate;

      internal double distanceRemaining;
      internal double turnRemaining;
      internal double gunTurnRemaining;
      internal double radarTurnRemaining;

      internal Thread thread;
      private readonly ManualResetEvent isBlocked = new ManualResetEvent(false);

      public __BotInternals(IBot parent)
      {
        this.parent = parent;

        this.maxSpeed = parent.MaxForwardSpeed;
        this.maxTurnRate = parent.MaxTurnRate;
        this.maxGunTurnRate = parent.MaxGunTurnRate;
        this.maxRadarTurnRate = parent.MaxRadarTurnRate;
      }

      internal bool IsRunning
      {
        get => thread != null && thread.IsAlive;
      }

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
        lock (isBlocked)
        {
          while (IsRunning && condition.Invoke())
          {
            isBlocked.WaitOne();
            isBlocked.Reset();
          }
        }
      }
    }
  }
}