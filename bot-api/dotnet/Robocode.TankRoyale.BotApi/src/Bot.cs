using System;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Abstract bot class provides convenient methods for movement, turning, and firing the gun.
/// Most bots should inherit from this class.
/// </summary>
public abstract class Bot : BaseBot, IBot
{
    private readonly BotInternals __botInternals;

    /// <see cref="BaseBot()"/>
    public Bot()
    {
        __botInternals = new BotInternals(this, __baseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo)"/>
    public Bot(BotInfo botInfo) : base(botInfo)
    {
        __botInternals = new BotInternals(this, __baseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo, Uri)"/>
    public Bot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl)
    {
        __botInternals = new BotInternals(this, __baseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo, Uri, string)"/>
    public Bot(BotInfo botInfo, Uri serverUrl, string serverSecret) : base(botInfo, serverUrl, serverSecret)
    {
        __botInternals = new BotInternals(this, __baseBotInternals);
    }

    /// <inheritdoc/>
    public override double TurnRate
    {
        set => __botInternals.SetTurnRate(value);
    }

    /// <inheritdoc/>
    public override double GunTurnRate
    {
        set => __botInternals.SetGunTurnRate(value);
    }

    /// <inheritdoc/>
    public override double RadarTurnRate
    {
        set => __botInternals.SetRadarTurnRate(value);
    }

    /// <inheritdoc/>
    public virtual void Run()
    {
    }

    /// <inheritdoc/>
    public new double TargetSpeed
    {
        set => __botInternals.SetTargetSpeed(value);
        get => __baseBotInternals.BotIntent.TargetSpeed ?? 0d;
    }

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
        __botInternals.Stop(false);
    }

    /// <inheritdoc/>
    public void Stop(bool overwrite)
    {
        __botInternals.Stop(overwrite);
    }
    
    /// <inheritdoc/>
    public void Resume()
    {
        __botInternals.Resume();
    }

    /// <inheritdoc/>
    public void Rescan()
    {
        __botInternals.Rescan();
    }

    /// <inheritdoc/>
    public void WaitFor(Condition condition)
    {
        __botInternals.WaitFor(condition);
    }
}