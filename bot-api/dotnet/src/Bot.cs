using System;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract bot class provides convenient methods for movement, turning, and firing the gun.
  /// Most bots should inherit from this class.
  /// </summary>
  public abstract class Bot : BaseBot, IBot
  {
    private readonly BotInternals __botInternals;

    // <inheritdoc/> does not work with the default constructor?
    /// <summary>
    /// Constructor for initializing a new instance of the Bot class, which should be used when
    /// both BotInfo and server URI is provided through environment variables, i.e., when starting
    /// up the bot using a booter. These environment variables must be set to provide the server
    /// URL and bot information, and are automatically set by the booter tool for Robocode.
    ///
    /// Example of how to set the predefined environment variables:
    ///
    /// SERVER_URL=ws://localhost<br/>
    /// SERVER_SECRET=xzoEeVbnBe5TGjCny0R1yQ<br>
    /// BOT_NAME=MyBot<br/>
    /// BOT_VERSION=1.0<br/>
    /// BOT_AUTHOR=fnl<br/>
    /// BOT_DESCRIPTION=Sample bot<br/>
    /// BOT_URL=https://mybot.somewhere.net<br/>
    /// BOT_COUNTRY_CODE=DK<br/>
    /// BOT_GAME_TYPES=melee,1v1<br/>
    /// BOT_PLATFORM=.Net Core 3.1<br/>
    /// BOT_PROG_LANG=C# 8<br/>
    /// </summary>
    public Bot() : base()
    {
      __botInternals = new BotInternals(this, base.__baseBotInternals);
    }

    /// <inheritdoc/>
    public Bot(BotInfo botInfo) : base(botInfo)
    {
      __botInternals = new BotInternals(this, base.__baseBotInternals);
    }

    /// <inheritdoc/>
    public Bot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl)
    {
      __botInternals = new BotInternals(this, base.__baseBotInternals);
    }

    /// <inheritdoc/>
    public virtual void Run() { }

    /// <inheritdoc/>
    public bool IsRunning => __botInternals.IsRunning;

    /// <inheritdoc/>
    public void SetForward(double distance)
    {
      __botInternals.SetForward(distance);
    }

    /// <inheritdoc/>
    public void Forward(double distance)
    {
      __botInternals.Forward(distance);
    }

    /// <inheritdoc/>
    public void SetBack(double distance)
    {
      SetForward(-distance);
    }

    /// <inheritdoc/>
    public void Back(double distance)
    {
      Forward(-distance);
    }

    /// <inheritdoc/>
    public double DistanceRemaining => __botInternals.DistanceRemaining;

    /// <inheritdoc/>
    public void SetTurnLeft(double degrees)
    {
      __botInternals.SetTurnLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnLeft(double degrees)
    {
      __botInternals.TurnLeft(degrees);
    }

    /// <inheritdoc/>
    public void SetTurnRight(double degrees)
    {
      SetTurnLeft(-degrees);
    }

    /// <inheritdoc/>
    public void TurnRight(double degrees)
    {
      TurnLeft(-degrees);
    }

    /// <inheritdoc/>
    public double TurnRemaining => __botInternals.TurnRemaining;

    /// <inheritdoc/>
    public void SetTurnGunLeft(double degrees)
    {
      __botInternals.SetTurnGunLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnGunLeft(double degrees)
    {
      __botInternals.TurnGunLeft(degrees);
    }

    /// <inheritdoc/>
    public void SetTurnGunRight(double degrees)
    {
      SetTurnGunLeft(-degrees);
    }

    /// <inheritdoc/>
    public void TurnGunRight(double degrees)
    {
      TurnGunLeft(-degrees);
    }

    /// <inheritdoc/>
    public double GunTurnRemaining => __botInternals.GunTurnRemaining;

    /// <inheritdoc/>
    public void SetTurnRadarLeft(double degrees)
    {
      __botInternals.SetTurnRadarLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnRadarLeft(double degrees)
    {
      __botInternals.TurnRadarLeft(degrees);
    }

    /// <inheritdoc/>
    public void SetTurnRadarRight(double degrees)
    {
      SetTurnRadarLeft(-degrees);
    }

    /// <inheritdoc/>
    public void TurnRadarRight(double degrees)
    {
      __botInternals.TurnRadarLeft(-degrees);
    }

    /// <inheritdoc/>
    public double RadarTurnRemaining => __botInternals.RadarTurnRemaining;

    /// <inheritdoc/>
    public void Fire(double firepower)
    {
      __botInternals.Fire(firepower);
    }

    /// <inheritdoc/>
    public void Stop()
    {
      __botInternals.Stop();
    }

    /// <inheritdoc/>
    public void Resume()
    {
      __botInternals.Resume();
    }

    /// <inheritdoc/>
    public void Scan()
    {
      __botInternals.Scan();
    }

    /// <inheritdoc/>
    public void WaitFor(Condition condition)
    {
      __botInternals.WaitFor(condition);
    }

    /// <inheritdoc/>
    public void SetTargetSpeed(double targetSpeed)
    {
      __botInternals.SetTargetSpeed(targetSpeed);
    }
  }
}