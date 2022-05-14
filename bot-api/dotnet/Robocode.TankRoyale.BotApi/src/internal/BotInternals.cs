using System;
using System.Threading;
using Robocode.TankRoyale.BotApi.Events;
using static System.Double;
using static System.Math;

namespace Robocode.TankRoyale.BotApi.Internal;

internal sealed class BotInternals : IStopResumeListener
{
    private readonly IBot bot;
    private readonly BaseBotInternals baseBotInternals;

    private Thread thread;

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

        BotEventHandlers botEventHandlers = baseBotInternals.BotEventHandlers;
        botEventHandlers.onGameAborted.Subscribe(OnGameAborted, 100);
        botEventHandlers.onNextTurn.Subscribe(OnNextTurn, 90);
        botEventHandlers.onRoundEnded.Subscribe(OnRoundEnded, 90);
        botEventHandlers.onGameEnded.Subscribe(OnGameEnded, 90);
        botEventHandlers.onDisconnected.Subscribe(OnDisconnected, 90);
        botEventHandlers.onHitWall.Subscribe(OnHitWall, 90);
        botEventHandlers.onHitBot.Subscribe(OnHitBot, 90);
        botEventHandlers.onDeath.Subscribe(OnDeath, 90);
    }

    private void OnNextTurn(TickEvent evt)
    {
        if (evt.TurnNumber == 1)
            OnFirstTurn();

        ProcessTurn();
    }

    private void OnFirstTurn()
    {
        StopThread(); // sanity before starting a new thread (later)
        ClearRemaining();
        StartThread();
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
        StopThread();
    }

    private void OnRoundEnded(RoundEndedEvent evt)
    {
        StopThread();
    }

    private void OnGameEnded(GameEndedEvent evt)
    {
        StopThread();
    }

    private void OnDisconnected(DisconnectedEvent evt)
    {
        StopThread();
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

    private void StartThread()
    {
        thread = new Thread(() =>
        {
            baseBotInternals.IsRunning = true;
            bot.Run();
            baseBotInternals.DisableEventQueue();
        });
        thread.Start();
    }

    private void StopThread()
    {
        if (!IsRunning)
            return;
            
        baseBotInternals.IsRunning = false;
    
        if (thread != null)
        {
            thread.Interrupt();
            thread = null;
        }
        baseBotInternals.DisableEventQueue();
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
        if (evt.VictimId == bot.MyId)
            StopThread();
    }

    internal bool IsRunning
    {
        get => baseBotInternals.IsRunning;
    }

    internal double DistanceRemaining { get; private set; }

    internal double TurnRemaining { get; private set; }

    internal double GunTurnRemaining { get; private set; }

    internal double RadarTurnRemaining { get; private set; }

    internal void SetTargetSpeed(double targetSpeed)
    {
        if (IsNaN(targetSpeed))
            throw new ArgumentException("targetSpeed cannot be NaN");

        if (targetSpeed > 0)
            DistanceRemaining = PositiveInfinity;
        else if (targetSpeed < 0)
            DistanceRemaining = NegativeInfinity;
        else
            DistanceRemaining = 0;

        baseBotInternals.BotIntent.TargetSpeed = targetSpeed;
    }

    internal void SetForward(double distance)
    {
        if (IsNaN(distance))
            throw new ArgumentException("distance cannot be NaN");

        var speed = baseBotInternals.GetNewSpeed(bot.Speed, distance);
        baseBotInternals.BotIntent.TargetSpeed = speed;

        DistanceRemaining = distance;
    }

    internal void Forward(double distance)
    {
        if (bot.IsStopped)
            bot.Go();
        else
        {
            SetForward(distance);
            do
                bot.Go();
            while (IsRunning && (Abs(DistanceRemaining) > 0 || Abs(bot.Speed) > 0));
        }
    }

    internal void SetTurnLeft(double degrees)
    {
        if (IsNaN(degrees))
            throw new ArgumentException("degrees cannot be NaN");

        TurnRemaining = degrees;
        baseBotInternals.BotIntent.TurnRate = degrees;
    }

    internal void TurnLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go();
        else
        {
            SetTurnLeft(degrees);
            do
                bot.Go();
            while (IsRunning && Abs(TurnRemaining) > 0 || Abs(bot.TurnRate) > 0);
        }
    }

    internal void SetTurnGunLeft(double degrees)
    {
        if (IsNaN(degrees))
            throw new ArgumentException("degrees cannot be NaN");

        GunTurnRemaining = degrees;
        baseBotInternals.BotIntent.GunTurnRate = degrees;
    }

    internal void TurnGunLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go();
        else
        {
            SetTurnGunLeft(degrees);
            do
                bot.Go();
            while (IsRunning && Abs(GunTurnRemaining) > 0 || Abs(bot.GunTurnRate) > 0);
        }
    }

    internal void SetTurnRadarLeft(double degrees)
    {
        if (IsNaN(degrees))
            throw new ArgumentException("degrees cannot be NaN");

        RadarTurnRemaining = degrees;
        baseBotInternals.BotIntent.RadarTurnRate = degrees;
    }

    internal void TurnRadarLeft(double degrees)
    {
        if (bot.IsStopped)
            bot.Go();
        else
        {
            SetTurnRadarLeft(degrees);
            do
                bot.Go();
            while (IsRunning && Abs(RadarTurnRemaining) > 0 || Abs(bot.RadarTurnRate) > 0);
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

    internal void WaitFor(Condition condition)
    {
        while (IsRunning && !condition.Test())
            bot.Go();
    }

    internal void Stop()
    {
        baseBotInternals.SetStop();
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

        if (Math.Abs(TurnRemaining) <= Math.Abs(delta))
            TurnRemaining = 0;
        else
        {
            TurnRemaining -= delta;
            if (IsNearZero(TurnRemaining))
                TurnRemaining = 0;
        }

        bot.TurnRate = TurnRemaining;
    }

    private void UpdateGunTurnRemaining()
    {
        var delta = bot.CalcDeltaAngle(bot.GunDirection, previousGunDirection);
        previousGunDirection = bot.GunDirection;

        if (Math.Abs(GunTurnRemaining) <= Math.Abs(delta))
            GunTurnRemaining = 0;
        else
        {
            GunTurnRemaining -= delta;
            if (IsNearZero(GunTurnRemaining))
                GunTurnRemaining = 0;
        }

        bot.GunTurnRate = GunTurnRemaining;
    }

    private void UpdateRadarTurnRemaining()
    {
        var delta = bot.CalcDeltaAngle(bot.RadarDirection, previousRadarDirection);
        previousRadarDirection = bot.RadarDirection;

        if (Math.Abs(RadarTurnRemaining) <= Math.Abs(delta))
            RadarTurnRemaining = 0;
        else
        {
            RadarTurnRemaining -= delta;
            if (IsNearZero(RadarTurnRemaining))
                RadarTurnRemaining = 0;
        }

        bot.RadarTurnRate = RadarTurnRemaining;
    }

    private void UpdateMovement()
    {
        if (IsInfinity(DistanceRemaining))
        {
            baseBotInternals.BotIntent.TargetSpeed =
                IsPositiveInfinity(DistanceRemaining) ? Constants.MaxSpeed : -Constants.MaxSpeed;
        }
        else
        {
            var distance = DistanceRemaining;

            // This is Nat Pavasant's method described here:
            // https://robowiki.net/wiki/User:Positive/Optimal_Velocity#Nat.27s_updateMovement
            var speed = baseBotInternals.GetNewSpeed(bot.Speed, distance);
            baseBotInternals.BotIntent.TargetSpeed = speed;

            // If we are over-driving our distance and we are now at velocity=0 then we stopped
            if (IsNearZero(speed) && isOverDriving)
            {
                DistanceRemaining = 0;
                distance = 0;
                isOverDriving = false;
            }

            // the overdrive flag
            if (Math.Sign(distance * speed) != -1)
                isOverDriving = baseBotInternals.GetDistanceTraveledUntilStop(speed) > Math.Abs(distance);

            DistanceRemaining = distance - speed;
        }
    }

    private static bool IsNearZero(double value) => Math.Abs(value) < .00001;
}