using System;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract bot class provides convenient methods for movement, turning, and firing the gun.
  /// Most bots should inherit from this class.
  /// </summary>
  public partial class Bot : BaseBot, IBot
  {
    readonly BotInternals __botInternals;

    // <inheritdoc/> does not work with the default constructor?
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
    public Bot() : base()
    {
      __botInternals = new BotInternals(this);
    }

    /// <inheritdoc/>
    public Bot(BotInfo botInfo) : base(botInfo)
    {
      __botInternals = new BotInternals(this);
    }

    /// <inheritdoc/>
    public Bot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl)
    {
      __botInternals = new BotInternals(this);
    }

    /// <inheritdoc/>
    public virtual void Run() { }

    /// <inheritdoc/>
    public bool IsRunning => __botInternals.IsRunning;

    /// <inheritdoc/>
    public void SetForward(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __botInternals.distanceRemaining = distance;
    }

    /// <inheritdoc/>
    public void Forward(double distance)
    {
      SetForward(distance);
      Go();
      __botInternals.AwaitMovementComplete();
    }

    /// <inheritdoc/>
    public void SetBack(double distance)
    {
      if (Double.IsNaN(distance))
      {
        throw new ArgumentException("distance cannot be NaN");
      }
      __botInternals.distanceRemaining = -distance;
    }

    /// <inheritdoc/>
    public void Back(double distance)
    {
      SetBack(distance);
      Go();
      __botInternals.AwaitMovementComplete();
    }

    /// <inheritdoc/>
    public double DistanceRemaining => __botInternals.distanceRemaining;

    /// <inheritdoc/>
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

    /// <inheritdoc/>
    public void SetTurnLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.turnRemaining = degrees;
    }

    /// <inheritdoc/>
    public void TurnLeft(double degrees)
    {
      SetTurnLeft(degrees);
      Go();
      __botInternals.AwaitTurnComplete();
    }

    /// <inheritdoc/>
    public void SetTurnRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.turnRemaining = -degrees;
    }

    /// <inheritdoc/>
    public void TurnRight(double degrees)
    {
      SetTurnRight(degrees);
      Go();
      __botInternals.AwaitTurnComplete();
    }

    /// <inheritdoc/>
    public double TurnRemaining => __botInternals.turnRemaining;

    /// <inheritdoc/>
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

    /// <inheritdoc/>
    public void SetTurnGunLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.gunTurnRemaining = degrees;
    }

    /// <inheritdoc/>
    public void TurnGunLeft(double degrees)
    {
      SetTurnGunLeft(degrees);
      Go();
      __botInternals.awaitGunTurnComplete();
    }

    /// <inheritdoc/>
    public void SetTurnGunRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.gunTurnRemaining = -degrees;
    }

    /// <inheritdoc/>
    public void TurnGunRight(double degrees)
    {
      SetTurnGunRight(degrees);
      Go();
      __botInternals.awaitGunTurnComplete();
    }

    /// <inheritdoc/>
    public double GunTurnRemaining => __botInternals.gunTurnRemaining;

    /// <inheritdoc/>
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

    /// <inheritdoc/>
    public void SetTurnRadarLeft(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.radarTurnRemaining = degrees;
    }

    /// <inheritdoc/>
    public void TurnRadarLeft(double degrees)
    {
      SetTurnRadarLeft(degrees);
      Go();
      __botInternals.awaitRadarTurnComplete();
    }

    /// <inheritdoc/>
    public void SetTurnRadarRight(double degrees)
    {
      if (Double.IsNaN(degrees))
      {
        throw new ArgumentException("degrees cannot be NaN");
      }
      __botInternals.radarTurnRemaining = -degrees;
    }

    /// <inheritdoc/>
    public void TurnRadarRight(double degrees)
    {
      SetTurnRadarRight(degrees);
      Go();
      __botInternals.awaitRadarTurnComplete();
    }

    /// <inheritdoc/>
    public double RadarTurnRemaining => __botInternals.radarTurnRemaining;

    /// <inheritdoc/>
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

    /// <inheritdoc/>
    public void Fire(double firepower)
    {
      Console.WriteLine("Fire");

      Firepower = firepower;
      Go();
      Firepower = 0;
    }
  }
}