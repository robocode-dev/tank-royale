using System;
using JetBrains.Annotations;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Abstract bot class provides convenient methods for movement, turning, and firing the gun.
/// Most bots should inherit from this class.
/// </summary>
[PublicAPI]
public abstract class Bot : BaseBot, IBot
{
    private readonly BotInternals _botInternals;

    /// <see cref="BaseBot()"/>
    protected Bot()
    {
        _botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo)"/>
    protected Bot(BotInfo botInfo) : base(botInfo)
    {
        _botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo, Uri)"/>
    protected Bot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl)
    {
        _botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <see cref="BaseBot(BotInfo, Uri, string)"/>
    protected Bot(BotInfo botInfo, Uri serverUrl, string serverSecret) : base(botInfo, serverUrl, serverSecret)
    {
        _botInternals = new BotInternals(this, BaseBotInternals);
    }

    /// <inheritdoc/>
    public override double TurnRate
    {
        set => _botInternals.SetTurnRate(value);
    }

    /// <inheritdoc/>
    public override double GunTurnRate
    {
        set => _botInternals.SetGunTurnRate(value);
    }

    /// <inheritdoc/>
    public override double RadarTurnRate
    {
        set => _botInternals.SetRadarTurnRate(value);
    }

    /// <inheritdoc/>
    public virtual void Run()
    {
    }

    /// <inheritdoc/>
    public new double TargetSpeed
    {
        set => _botInternals.SetTargetSpeed(value);
        get => BaseBotInternals.BotIntent.TargetSpeed ?? 0d;
    }

    /// <inheritdoc/>
    public bool IsRunning => BaseBotInternals.IsRunning;

    /// <inheritdoc/>
    public void SetForward(double distance)
    {
        _botInternals.SetForward(distance);
    }

    /// <inheritdoc/>
    public void Forward(double distance)
    {
        _botInternals.Forward(distance);
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
    public double DistanceRemaining => _botInternals.DistanceRemaining;

    /// <inheritdoc/>
    public void SetTurnLeft(double degrees)
    {
        _botInternals.SetTurnLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnLeft(double degrees)
    {
        _botInternals.TurnLeft(degrees);
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
    public double TurnRemaining => _botInternals.TurnRemaining;

    /// <inheritdoc/>
    public void SetTurnGunLeft(double degrees)
    {
        _botInternals.SetTurnGunLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnGunLeft(double degrees)
    {
        _botInternals.TurnGunLeft(degrees);
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
    public double GunTurnRemaining => _botInternals.GunTurnRemaining;

    /// <inheritdoc/>
    public void SetTurnRadarLeft(double degrees)
    {
        _botInternals.SetTurnRadarLeft(degrees);
    }

    /// <inheritdoc/>
    public void TurnRadarLeft(double degrees)
    {
        _botInternals.TurnRadarLeft(degrees);
    }

    /// <inheritdoc/>
    public void SetTurnRadarRight(double degrees)
    {
        SetTurnRadarLeft(-degrees);
    }

    /// <inheritdoc/>
    public void TurnRadarRight(double degrees)
    {
        _botInternals.TurnRadarLeft(-degrees);
    }

    /// <inheritdoc/>
    public double RadarTurnRemaining => _botInternals.RadarTurnRemaining;

    /// <inheritdoc/>
    public void Fire(double firepower)
    {
        _botInternals.Fire(firepower);
    }

    /// <inheritdoc/>
    public void Stop()
    {
        _botInternals.Stop(false);
    }

    /// <inheritdoc/>
    public void Stop(bool overwrite)
    {
        _botInternals.Stop(overwrite);
    }

    /// <inheritdoc/>
    public void Resume()
    {
        _botInternals.Resume();
    }

    /// <inheritdoc/>
    public void Rescan()
    {
        _botInternals.Rescan();
    }

    /// <inheritdoc/>
    public void WaitFor(Condition condition)
    {
        _botInternals.WaitFor(condition.Test);
    }
}