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
    private readonly BotInternals botInternals;

    /// <see cref="BaseBot()"/>
    protected Bot()
    {
        botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo)"/>
    protected Bot(BotInfo botInfo) : base(botInfo)
    {
        botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo, Uri)"/>
    protected Bot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl)
    {
        botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo, Uri, string)"/>
    protected Bot(BotInfo botInfo, Uri serverUrl, string serverSecret) : base(botInfo, serverUrl, serverSecret)
    {
        botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <inheritdoc/>
    public override double TurnRate
    {
        set => botInternals.SetTurnRate(value);
    }

    /// <inheritdoc/>
    public override double GunTurnRate
    {
        set => botInternals.SetGunTurnRate(value);
    }

    /// <inheritdoc/>
    public override double RadarTurnRate
    {
        set => botInternals.SetRadarTurnRate(value);
    }

    /// <inheritdoc/>
    public virtual void Run()
    {
    }

    /// <inheritdoc/>
    public new double TargetSpeed
    {
        set => botInternals.SetTargetSpeed(value);
        get => BaseBotInternals.BotIntent.TargetSpeed ?? 0d;
    }

    /// <inheritdoc/>
    public bool IsRunning => BaseBotInternals.IsRunning;

    /// <inheritdoc/>
    public void SetForward(double distance)
    {
        botInternals.SetForward(distance);
    }

    /// <inheritdoc/>
    public void Forward(double distance)
    {
        botInternals.Forward(distance);
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
    public double DistanceRemaining => botInternals.DistanceRemaining;

    /// <inheritdoc/>
    public void SetTurnLeft(double degrees)
    {
        botInternals.SetTurnLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnLeft(double degrees)
    {
        botInternals.TurnLeft(degrees);
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
    public double TurnRemaining => botInternals.TurnRemaining;

    /// <inheritdoc/>
    public void SetTurnGunLeft(double degrees)
    {
        botInternals.SetTurnGunLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnGunLeft(double degrees)
    {
        botInternals.TurnGunLeft(degrees);
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
    public double GunTurnRemaining => botInternals.GunTurnRemaining;

    /// <inheritdoc/>
    public void SetTurnRadarLeft(double degrees)
    {
        botInternals.SetTurnRadarLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnRadarLeft(double degrees)
    {
        botInternals.TurnRadarLeft(degrees);
    }

    /// <inheritdoc/>
    public void SetTurnRadarRight(double degrees)
    {
        SetTurnRadarLeft(-degrees);
    }

    /// <inheritdoc/>
    public void TurnRadarRight(double degrees)
    {
        botInternals.TurnRadarLeft(-degrees);
    }

    /// <inheritdoc/>
    public double RadarTurnRemaining => botInternals.RadarTurnRemaining;

    /// <inheritdoc/>
    public void Fire(double firepower)
    {
        botInternals.Fire(firepower);
    }

    /// <inheritdoc/>
    public void Stop()
    {
        botInternals.Stop(false);
    }

    /// <inheritdoc/>
    public void Stop(bool overwrite)
    {
        botInternals.Stop(overwrite);
    }

    /// <inheritdoc/>
    public void Resume()
    {
        botInternals.Resume();
    }

    /// <inheritdoc/>
    public void Rescan()
    {
        botInternals.Rescan();
    }

    /// <inheritdoc/>
    public void WaitFor(Condition condition)
    {
        botInternals.WaitFor(condition.Test);
    }
}