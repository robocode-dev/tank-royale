using System;
using Robocode.TankRoyale.BotApi.Events;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

internal sealed class BotInternals : IStopResumeListener
{
    private readonly IBot bot;
    private readonly BaseBotInternals baseBotInternals;

    private bool overrideTurnRate;
    private bool overrideGunTurnRate;
    private bool overrideRadarTurnRate;
    private bool overrideTargetSpeed;

    private double previousDirection;
    private double previousGunDirection;
    private double previousRadarDirection;

    private bool isOverDriving;

    private double savedPreviousDirection;
    private double savedPreviousGunDirection;
    private double savedPreviousRadarDirection;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    public BotInternals(IBot bot, BaseBotInternals baseBotInternals)
    {
        this.bot = bot;
        this.baseBotInternals = baseBotInternals;

        baseBotInternals.SetStopResumeHandler(this);

        var internalEventHandlers = baseBotInternals.InstantEventHandlers;
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
        baseBotInternals.StopThread(); // sanity before starting a new thread (later)
        ClearRemaining();
        baseBotInternals.StartThread(bot);
    }

    private void ClearRemaining()
    {
        DistanceRemaining = 0;
        TurnRemaining = 0;
        GunTurnRemaining = 0;
        RadarTurnRemaining = 0;

        previousDirection = bot.Direction;
        previousGunDirection = bot.GunDirection;
        previousRadarDirection = bot.RadarDirection;
    }

    private void OnGameAborted(object dummy)
    {
        baseBotInternals.StopThread();
    }

    private void OnRoundEnded(RoundEndedEvent evt)
    {
        baseBotInternals.StopThread();
    }

    private void OnGameEnded(GameEndedEvent evt)
    {
        baseBotInternals.StopThread();
    }

    private void OnDisconnected(DisconnectedEvent evt)
    {
        baseBotInternals.StopThread();
    }

    private void ProcessTurn()
    {
        // No movement is possible, when the bot has become disabled
        if (bot.IsDisabled)
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
        baseBotInternals.StopThread();
    }

    public void SetTurnRate(double turnRate)
    {
        overrideTurnRate = false;
        TurnRemaining = ToInfiniteValue(turnRate);
        baseBotInternals.TurnRate = turnRate;
    }

    public void SetGunTurnRate(double gunTurnRate)
    {
        overrideGunTurnRate = false;
        GunTurnRemaining = ToInfiniteValue(gunTurnRate);
        baseBotInternals.GunTurnRate = gunTurnRate;
    }

    public void SetRadarTurnRate(double radarTurnRate)
    {
        overrideRadarTurnRate = false;
        RadarTurnRemaining = ToInfiniteValue(radarTurnRate);
        baseBotInternals.RadarTurnRate = radarTurnRate;
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
        overrideTargetSpeed = false;
        DistanceRemaining = targetSpeed switch
        {
            NaN => throw new ArgumentException("'targetSpeed' cannot be NaN"),
            > 0 => PositiveInfinity,
            < 0 => NegativeInfinity,
            _ => 0
        };

        baseBotInternals.TargetSpeed = targetSpeed;
    }

    internal void SetForward(double distance)
    {
        overrideTargetSpeed = true;
        if (IsNaN(distance))
            throw new ArgumentException("'distance' cannot be NaN");
        GetAndSetNewTargetSpeed(distance);
        DistanceRemaining = distance;
    }

    internal void Forward(double distance)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetForward(distance);
            WaitFor(() => DistanceRemaining == 0 && bot.Speed == 0);
        }
    }

    internal void SetTurnLeft(double degrees)
    {
        overrideTurnRate = true;
        TurnRemaining = degrees;
        baseBotInternals.TurnRate = degrees;
    }

    internal void TurnLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnLeft(degrees);
            WaitFor(() => TurnRemaining == 0);
        }
    }

    internal void SetTurnGunLeft(double degrees)
    {
        overrideGunTurnRate = true;
        GunTurnRemaining = degrees;
        baseBotInternals.GunTurnRate = degrees;
    }

    internal void TurnGunLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnGunLeft(degrees);
            WaitFor(() => GunTurnRemaining == 0);
        }
    }

    internal void SetTurnRadarLeft(double degrees)
    {
        overrideRadarTurnRate = true;
        RadarTurnRemaining = degrees;
        baseBotInternals.RadarTurnRate = degrees;
    }

    internal void TurnRadarLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go(); // skip turn by doing nothing in the turn
        else
        {
            SetTurnRadarLeft(degrees);
            WaitFor(() => RadarTurnRemaining == 0);
        }
    }

    internal void Fire(double firepower)
    {
        if (bot.SetFire(firepower))
            bot.Go();
    }

    internal void Rescan()
    {
        baseBotInternals.SetScannedBotEventInterruptible();
        bot.SetRescan();
        bot.Go();
    }

    internal delegate bool ConditionDelegate();

    internal void WaitFor(ConditionDelegate condition)
    {
        do
        {
            bot.Go();
        } while (baseBotInternals.IsRunning && !condition());
    }

    internal void Stop(bool overwrite)
    {
        baseBotInternals.SetStop(overwrite);
        bot.Go();
    }

    internal void Resume()
    {
        baseBotInternals.SetResume();
        bot.Go();
    }

    public void OnStop()
    {
        savedPreviousDirection = previousDirection;
        savedPreviousGunDirection = previousGunDirection;
        savedPreviousRadarDirection = previousRadarDirection;

        savedDistanceRemaining = DistanceRemaining;
        savedTurnRemaining = TurnRemaining;
        savedGunTurnRemaining = GunTurnRemaining;
        savedRadarTurnRemaining = RadarTurnRemaining;
    }

    public void OnResume()
    {
        previousDirection = savedPreviousDirection;
        previousGunDirection = savedPreviousGunDirection;
        previousRadarDirection = savedPreviousRadarDirection;

        DistanceRemaining = savedDistanceRemaining;
        TurnRemaining = savedTurnRemaining;
        GunTurnRemaining = savedGunTurnRemaining;
        RadarTurnRemaining = savedRadarTurnRemaining;
    }

    private void UpdateTurnRemaining()
    {
        var delta = bot.CalcDeltaAngle(bot.Direction, previousDirection);
        previousDirection = bot.Direction;

        if (!overrideTurnRate)
            return; // called after previous direction has been calculated and stored!

        if (Math.Abs(TurnRemaining) <= Math.Abs(delta))
            TurnRemaining = 0;
        else
        {
            TurnRemaining -= delta;
            if (IsNearZero(TurnRemaining))
                TurnRemaining = 0;
        }

        baseBotInternals.TurnRate = TurnRemaining;
    }

    private void UpdateGunTurnRemaining()
    {
        var delta = bot.CalcDeltaAngle(bot.GunDirection, previousGunDirection);
        previousGunDirection = bot.GunDirection;

        if (!overrideGunTurnRate)
            return; // called after previous direction has been calculated and stored!

        if (Math.Abs(GunTurnRemaining) <= Math.Abs(delta))
            GunTurnRemaining = 0;
        else
        {
            GunTurnRemaining -= delta;
            if (IsNearZero(GunTurnRemaining))
                GunTurnRemaining = 0;
        }

        baseBotInternals.GunTurnRate = GunTurnRemaining;
    }

    private void UpdateRadarTurnRemaining()
    {
        var delta = bot.CalcDeltaAngle(bot.RadarDirection, previousRadarDirection);
        previousRadarDirection = bot.RadarDirection;

        if (!overrideRadarTurnRate)
            return; // called after previous direction has been calculated and stored!

        if (Math.Abs(RadarTurnRemaining) <= Math.Abs(delta))
            RadarTurnRemaining = 0;
        else
        {
            RadarTurnRemaining -= delta;
            if (IsNearZero(RadarTurnRemaining))
                RadarTurnRemaining = 0;
        }

        baseBotInternals.RadarTurnRate = RadarTurnRemaining;
    }

    private void UpdateMovement()
    {
        if (!overrideTargetSpeed)
        {
            if (Math.Abs(DistanceRemaining) < Math.Abs(bot.Speed))
            {
                DistanceRemaining = 0;
            }
            else
            {
                DistanceRemaining -= bot.Speed;
            }
        }
        else if (IsInfinity(DistanceRemaining))
        {
            baseBotInternals.TargetSpeed =
                IsPositiveInfinity(DistanceRemaining) ? Constants.MaxSpeed : -Constants.MaxSpeed;
        }
        else
        {
            var distance = DistanceRemaining;

            // This is Nat Pavasant's method described here:
            // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            var newSpeed = GetAndSetNewTargetSpeed(distance);

            // If we are over-driving our distance and we are now at velocity=0 then we stopped
            if (IsNearZero(newSpeed) && isOverDriving)
            {
                DistanceRemaining = 0;
                distance = 0;
                isOverDriving = false;
            }

            // the overdrive flag
            if (Math.Sign(distance * newSpeed) != -1)
                isOverDriving = baseBotInternals.GetDistanceTraveledUntilStop(newSpeed) > Math.Abs(distance);

            DistanceRemaining = distance - newSpeed;
        }
    }

    private double GetAndSetNewTargetSpeed(double distance)
    {
        var speed = baseBotInternals.GetNewTargetSpeed(bot.Speed, distance);
        baseBotInternals.TargetSpeed = speed;
        return speed;
    }

    private static bool IsNearZero(double value) => Math.Abs(value) < .00001;
}