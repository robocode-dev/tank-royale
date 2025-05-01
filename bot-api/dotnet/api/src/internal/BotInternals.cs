using System;
using Robocode.TankRoyale.BotApi.Events;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

sealed class BotInternals : IStopResumeListener
{
    private readonly IBot _bot;
    private readonly BaseBotInternals _baseBotInternals;

    private bool _overrideTurnRate;
    private bool _overrideGunTurnRate;
    private bool _overrideRadarTurnRate;
    private bool _overrideTargetSpeed;

    private double _previousDirection;
    private double _previousGunDirection;
    private double _previousRadarDirection;

    private bool _isOverDriving;

    private double _savedPreviousDirection;
    private double _savedPreviousGunDirection;
    private double _savedPreviousRadarDirection;

    private double _savedDistanceRemaining;
    private double _savedTurnRemaining;
    private double _savedGunTurnRemaining;
    private double _savedRadarTurnRemaining;

    public BotInternals(IBot bot, BaseBotInternals baseBotInternals)
    {
        _bot = bot;
        _baseBotInternals = baseBotInternals;

        baseBotInternals.SetStopResumeHandler(this);

        var internalEventHandlers = baseBotInternals.InternalEventHandlers;
        internalEventHandlers.OnGameAborted.Subscribe(OnGameAborted, 100);
        internalEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 90);
        internalEventHandlers.OnRoundEnded.Subscribe(OnRoundEnded, 90);
        internalEventHandlers.OnGameEnded.Subscribe(OnGameEnded, 90);
        internalEventHandlers.OnDisconnected.Subscribe(OnDisconnected, 90);
        internalEventHandlers.OnHitWall.Subscribe(OnHitWall, 90);
        internalEventHandlers.OnHitBot.Subscribe(OnHitBot, 90);
        internalEventHandlers.OnDeath.Subscribe(OnDeath, 90);
    }

    private void OnNextTurn(TickEvent evt)
    {
        if (evt.TurnNumber == 1)
            OnFirstTurn();

        ProcessTurn();
    }

    private void OnFirstTurn()
    {
        _baseBotInternals.StopThread(); // sanity before starting a new thread (later)
        ClearRemaining();
        _baseBotInternals.StartThread(_bot);
    }

    private void ClearRemaining()
    {
        DistanceRemaining = 0;
        TurnRemaining = 0;
        GunTurnRemaining = 0;
        RadarTurnRemaining = 0;

        _previousDirection = _bot.Direction;
        _previousGunDirection = _bot.GunDirection;
        _previousRadarDirection = _bot.RadarDirection;
    }

    private void OnGameAborted(object dummy)
    {
        _baseBotInternals.StopThread();
    }

    private void OnRoundEnded(RoundEndedEvent evt)
    {
        _baseBotInternals.StopThread();
    }

    private void OnGameEnded(GameEndedEvent evt)
    {
        _baseBotInternals.StopThread();
    }

    private void OnDisconnected(DisconnectedEvent evt)
    {
        _baseBotInternals.StopThread();
    }

    private void ProcessTurn()
    {
        // No movement is possible, when the bot has become disabled
        if (_bot.IsDisabled)
        {
            ClearRemaining();
        }
        else
        {
            UpdateTurnRemaining();
            UpdateGunTurnRemaining();
            UpdateRadarTurnRemaining();
            UpdateMovement();
        }
    }

    private void OnHitWall(HitWallEvent evt)
    {
        DistanceRemaining = 0;
    }

    private void OnHitBot(HitBotEvent evt)
    {
        if (evt.IsRammed)
            DistanceRemaining = 0;
    }

    private void OnDeath(DeathEvent evt)
    {
        _baseBotInternals.StopThread();
    }

    public void SetTurnRate(double turnRate)
    {
        _overrideTurnRate = false;
        TurnRemaining = ToInfiniteValue(turnRate);
        _baseBotInternals.TurnRate = turnRate;
    }

    public void SetGunTurnRate(double gunTurnRate)
    {
        _overrideGunTurnRate = false;
        GunTurnRemaining = ToInfiniteValue(gunTurnRate);
        _baseBotInternals.GunTurnRate = gunTurnRate;
    }

    public void SetRadarTurnRate(double radarTurnRate)
    {
        _overrideRadarTurnRate = false;
        RadarTurnRemaining = ToInfiniteValue(radarTurnRate);
        _baseBotInternals.RadarTurnRate = radarTurnRate;
    }

    private static double ToInfiniteValue(double turnRate)
    {
        return turnRate switch
        {
            > 0 => PositiveInfinity,
            < 0 => NegativeInfinity,
            _ => 0
        };
    }

    internal double DistanceRemaining { get; private set; }

    internal double TurnRemaining { get; private set; }

    internal double GunTurnRemaining { get; private set; }

    internal double RadarTurnRemaining { get; private set; }

    internal void SetTargetSpeed(double targetSpeed)
    {
        _overrideTargetSpeed = false;
        DistanceRemaining = targetSpeed switch
        {
            NaN => throw new ArgumentException("'targetSpeed' cannot be NaN"),
            > 0 => PositiveInfinity,
            < 0 => NegativeInfinity,
            _ => 0
        };

        _baseBotInternals.TargetSpeed = targetSpeed;
    }

    internal void SetForward(double distance)
    {
        _overrideTargetSpeed = true;
        if (IsNaN(distance))
            throw new ArgumentException("'distance' cannot be NaN");
        GetAndSetNewTargetSpeed(distance);
        DistanceRemaining = distance;
    }

    internal void Forward(double distance)
    {
        if (_bot.IsStopped)
            _bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetForward(distance);
            WaitFor(() => DistanceRemaining == 0 && _bot.Speed == 0);
        }
    }

    internal void SetTurnLeft(double degrees)
    {
        _overrideTurnRate = true;
        TurnRemaining = degrees;
        _baseBotInternals.TurnRate = degrees;
    }

    internal void TurnLeft(double degrees)
    {
        if (_bot.IsStopped)
            _bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnLeft(degrees);
            WaitFor(() => TurnRemaining == 0);
        }
    }

    internal void SetTurnGunLeft(double degrees)
    {
        _overrideGunTurnRate = true;
        GunTurnRemaining = degrees;
        _baseBotInternals.GunTurnRate = degrees;
    }

    internal void TurnGunLeft(double degrees)
    {
        if (_bot.IsStopped)
            _bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnGunLeft(degrees);
            WaitFor(() => GunTurnRemaining == 0);
        }
    }

    internal void SetTurnRadarLeft(double degrees)
    {
        _overrideRadarTurnRate = true;
        RadarTurnRemaining = degrees;
        _baseBotInternals.RadarTurnRate = degrees;
    }

    internal void TurnRadarLeft(double degrees)
    {
        if (_bot.IsStopped)
            _bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnRadarLeft(degrees);
            WaitFor(() => RadarTurnRemaining == 0);
        }
    }

    internal void Fire(double firepower)
    {
        _bot.SetFire(firepower);
        _bot.Go();
    }

    internal void Rescan()
    {
        EventInterruption.SetInterruptible(typeof(ScannedBotEvent), true);
        _bot.SetRescan();
        _bot.Go();
    }

    internal delegate bool ConditionDelegate();

    internal void WaitFor(ConditionDelegate condition)
    {
        do
        {
            _bot.Go();
        } while (_baseBotInternals.IsRunning && !condition());
    }

    internal void Stop(bool overwrite)
    {
        _baseBotInternals.SetStop(overwrite);
        _bot.Go();
    }

    internal void Resume()
    {
        _baseBotInternals.SetResume();
        _bot.Go();
    }

    public void OnStop()
    {
        _savedPreviousDirection = _previousDirection;
        _savedPreviousGunDirection = _previousGunDirection;
        _savedPreviousRadarDirection = _previousRadarDirection;

        _savedDistanceRemaining = DistanceRemaining;
        _savedTurnRemaining = TurnRemaining;
        _savedGunTurnRemaining = GunTurnRemaining;
        _savedRadarTurnRemaining = RadarTurnRemaining;
    }

    public void OnResume()
    {
        _previousDirection = _savedPreviousDirection;
        _previousGunDirection = _savedPreviousGunDirection;
        _previousRadarDirection = _savedPreviousRadarDirection;

        DistanceRemaining = _savedDistanceRemaining;
        TurnRemaining = _savedTurnRemaining;
        GunTurnRemaining = _savedGunTurnRemaining;
        RadarTurnRemaining = _savedRadarTurnRemaining;
    }

    private void UpdateTurnRemaining()
    {
        var delta = _bot.CalcDeltaAngle(_bot.Direction, _previousDirection);
        _previousDirection = _bot.Direction;

        if (!_overrideTurnRate)
            return; // called after previous direction has been calculated and stored!

        if (Math.Abs(TurnRemaining) <= Math.Abs(delta))
            TurnRemaining = 0;
        else
        {
            TurnRemaining -= delta;
            if (IsNearZero(TurnRemaining))
                TurnRemaining = 0;
        }

        _baseBotInternals.TurnRate = TurnRemaining;
    }

    private void UpdateGunTurnRemaining()
    {
        var delta = _bot.CalcDeltaAngle(_bot.GunDirection, _previousGunDirection);
        _previousGunDirection = _bot.GunDirection;

        if (!_overrideGunTurnRate)
            return; // called after previous direction has been calculated and stored!

        if (Math.Abs(GunTurnRemaining) <= Math.Abs(delta))
            GunTurnRemaining = 0;
        else
        {
            GunTurnRemaining -= delta;
            if (IsNearZero(GunTurnRemaining))
                GunTurnRemaining = 0;
        }

        _baseBotInternals.GunTurnRate = GunTurnRemaining;
    }

    private void UpdateRadarTurnRemaining()
    {
        var delta = _bot.CalcDeltaAngle(_bot.RadarDirection, _previousRadarDirection);
        _previousRadarDirection = _bot.RadarDirection;

        if (!_overrideRadarTurnRate)
            return; // called after previous direction has been calculated and stored!

        if (Math.Abs(RadarTurnRemaining) <= Math.Abs(delta))
            RadarTurnRemaining = 0;
        else
        {
            RadarTurnRemaining -= delta;
            if (IsNearZero(RadarTurnRemaining))
                RadarTurnRemaining = 0;
        }

        _baseBotInternals.RadarTurnRate = RadarTurnRemaining;
    }

    private void UpdateMovement()
    {
        if (!_overrideTargetSpeed)
        {
            if (Math.Abs(DistanceRemaining) < Math.Abs(_bot.Speed))
            {
                DistanceRemaining = 0;
            }
            else
            {
                DistanceRemaining -= _bot.Speed;
            }
        }
        else if (IsInfinity(DistanceRemaining))
        {
            _baseBotInternals.TargetSpeed =
                IsPositiveInfinity(DistanceRemaining) ? Constants.MaxSpeed : -Constants.MaxSpeed;
        }
        else
        {
            var distance = DistanceRemaining;

            // This is Nat Pavasant's method described here:
            // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            var newSpeed = GetAndSetNewTargetSpeed(distance);

            // If we are over-driving our distance and we are now at velocity=0 then we stopped
            if (IsNearZero(newSpeed) && _isOverDriving)
            {
                DistanceRemaining = 0;
                distance = 0;
                _isOverDriving = false;
            }

            // the overdrive flag
            if (Math.Sign(distance * newSpeed) != -1)
                _isOverDriving = _baseBotInternals.GetDistanceTraveledUntilStop(newSpeed) > Math.Abs(distance);

            DistanceRemaining = distance - newSpeed;
        }
    }

    private double GetAndSetNewTargetSpeed(double distance)
    {
        var speed = _baseBotInternals.GetNewTargetSpeed(_bot.Speed, distance);
        _baseBotInternals.TargetSpeed = speed;
        return speed;
    }

    private static bool IsNearZero(double value) => Math.Abs(value) < .00001;
}