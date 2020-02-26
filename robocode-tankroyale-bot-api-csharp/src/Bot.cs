using System;

namespace Robocode.TankRoyale.BotApi
{
  public partial class Bot : BaseBot, IBot
  {
    readonly BotInternals __botInternals;

    public Bot() : base()
    {
      __botInternals = new BotInternals(this);
    }

    public Bot(BotInfo botInfo) : base(botInfo)
    {
      __botInternals = new BotInternals(this);
    }

    public Bot(BotInfo botInfo, Uri serverUri) : base(botInfo, serverUri)
    {
      __botInternals = new BotInternals(this);
    }

    public virtual void Run() { }

    public bool IsRunning => __botInternals.IsRunning;

    public void SetForward(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __botInternals.distanceRemaining = distance;
    }

    public void Forward(double distance)
    {
      SetForward(distance);
      Go();
      __botInternals.AwaitMovementComplete();
    }

    public void SetBack(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __botInternals.distanceRemaining = -distance;
    }

    public void Back(double distance)
    {
      SetBack(distance);
      Go();
      __botInternals.AwaitMovementComplete();
    }

    public double DistanceRemaining => __botInternals.distanceRemaining;

    public void SetMaxSpeed(double maxSpeed)
    {
      if (maxSpeed < 0)
      {
        maxSpeed = 0;
      }
      else if (maxSpeed > ((IBot)this).MaxSpeed)
      {
        maxSpeed = ((IBot)this).MaxSpeed;
      }
      __botInternals.maxSpeed = maxSpeed;
    }

    public void SetTurnLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.turnRemaining = degrees;
    }

    public void TurnLeft(double degrees)
    {
      SetTurnLeft(degrees);
      Go();
      __botInternals.AwaitTurnComplete();
    }

    public void SetTurnRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.turnRemaining = -degrees;
    }

    public void TurnRight(double degrees)
    {
      SetTurnRight(degrees);
      Go();
      __botInternals.AwaitTurnComplete();
    }

    public double TurnRemaining => __botInternals.turnRemaining;

    public void SetMaxTurnRate(double maxTurnRate)
    {
      if (maxTurnRate < 0)
      {
        maxTurnRate = 0;
      }
      else if (maxTurnRate > ((IBot)this).MaxTurnRate)
      {
        maxTurnRate = ((IBot)this).MaxTurnRate;
      }
      __botInternals.maxTurnRate = maxTurnRate;
    }

    public void SetTurnGunLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.gunTurnRemaining = degrees;
    }

    public void TurnGunLeft(double degrees)
    {
      SetTurnGunLeft(degrees);
      Go();
      __botInternals.awaitGunTurnComplete();
    }

    public void SetTurnGunRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.gunTurnRemaining = -degrees;
    }

    public void TurnGunRight(double degrees)
    {
      SetTurnGunRight(degrees);
      Go();
      __botInternals.awaitGunTurnComplete();
    }

    public double GunTurnRemaining => __botInternals.gunTurnRemaining;

    public void SetMaxGunTurnRate(double maxGunTurnRate)
    {
      if (maxGunTurnRate < 0)
      {
        maxGunTurnRate = 0;
      }
      else if (maxGunTurnRate > ((IBot)this).MaxGunTurnRate)
      {
        maxGunTurnRate = ((IBot)this).MaxGunTurnRate;
      }
      __botInternals.maxGunTurnRate = maxGunTurnRate;
    }

    public void SetTurnRadarLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.radarTurnRemaining = degrees;
    }

    public void TurnRadarLeft(double degrees)
    {
      SetTurnRadarLeft(degrees);
      Go();
      __botInternals.awaitRadarTurnComplete();
    }

    public void SetTurnRadarRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.radarTurnRemaining = -degrees;
    }

    public void TurnRadarRight(double degrees)
    {
      SetTurnRadarRight(degrees);
      Go();
      __botInternals.awaitRadarTurnComplete();
    }

    public double RadarTurnRemaining => __botInternals.radarTurnRemaining;

    public void SetMaxRadarTurnRate(double maxRadarTurnRate)
    {
      if (maxRadarTurnRate < 0)
      {
        maxRadarTurnRate = 0;
      }
      else if (maxRadarTurnRate > ((IBot)this).MaxRadarTurnRate)
      {
        maxRadarTurnRate = ((IBot)this).MaxRadarTurnRate;
      }
      __botInternals.maxRadarTurnRate = maxRadarTurnRate;
    }

    public void Fire(double firepower)
    {
      Console.WriteLine("Fire");

      Firepower = firepower;
      Go();
    }
  }
}