using System;
using System.IO;
using System.Threading;
using Robocode.TankRoyale.BotApi.Events;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

internal sealed class BotInternals : IStopResumeListener
{
    private readonly IBot bot;
    private readonly BaseBotInternals baseBotInternals;

    private Thread thread;

    private double previousDirection;
    private double previousGunDirection;
    private double previousRadarDirection;

    private double distanceRemaining;
    private double turnRemaining;
    private double gunTurnRemaining;
    private double radarTurnRemaining;

    private bool isOverDriving;

    private double savedPreviousDirection;
    private double savedPreviousGunDirection;
    private double savedPreviousRadarDirection;

    private double savedDistanceRemaining;
    private double savedTurnRemaining;
    private double savedGunTurnRemaining;
    private double savedRadarTurnRemaining;

    private readonly object movementLock = new();
    private readonly object turnLock = new();
    private readonly object gunTurnLock = new();
    private readonly object radarTurnLock = new();

    public BotInternals(IBot bot, BaseBotInternals baseBotInternals)
    {
        this.bot = bot;
        this.baseBotInternals = baseBotInternals;

        baseBotInternals.SetStopResumeHandler(this);

        var botEventHandlers = baseBotInternals.BotEventHandlers;
        botEventHandlers.OnGameAborted.Subscribe(OnGameAborted, 100);
        botEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 90);
        botEventHandlers.OnRoundEnded.Subscribe(OnRoundEnded, 90);
        botEventHandlers.OnGameEnded.Subscribe(OnGameEnded, 90);
        botEventHandlers.OnDisconnected.Subscribe(OnDisconnected, 90);
        botEventHandlers.OnHitWall.Subscribe(OnHitWall, 90);
        botEventHandlers.OnHitBot.Subscribe(OnHitBot, 90);
        botEventHandlers.OnDeath.Subscribe(OnDeath, 90);
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
            try
            {
                baseBotInternals.EnableEventHandling(true); // prevent event queue max limit to be reached
                bot.Run();

                // Skip every turn after the run method has exited
                while (baseBotInternals.IsRunning)
                {
                    bot.Go();
                }
            }
            finally
            {
                baseBotInternals.EnableEventHandling(false); // prevent event queue max limit to be reached
            }
        });
        thread.Start();
    }

    private void StopThread()
    {
        if (!IsRunning) return;

        baseBotInternals.IsRunning = false;

        if (thread == null) return;

        thread.Interrupt();
        try
        {
            thread.Join(100);
            if (thread.IsAlive)
            {
#pragma warning disable SYSLIB0006
                thread.Abort();
#pragma warning restore SYSLIB0006
            }
        }
        catch (ThreadInterruptedException)
        {
            // ignore
        }
        finally
        {
            thread = null;
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
        StopThread();
    }

    internal bool IsRunning => baseBotInternals.IsRunning;

    public void SetTurnRate(double turnRate) {
        if (IsNaN(turnRate)) {
            throw new ArgumentException("turnRate cannot be NaN");
        }
        baseBotInternals.BotIntent.TurnRate = turnRate;
        TurnRemaining = ToInfiniteValue(turnRate);
    }

    public void SetGunTurnRate(double gunTurnRate) {
        if (IsNaN(gunTurnRate)) {
            throw new ArgumentException("gunTurnRate cannot be NaN");
        }
        baseBotInternals.BotIntent.GunTurnRate = gunTurnRate;
        GunTurnRemaining = ToInfiniteValue(gunTurnRate);
    }

    public void SetRadarTurnRate(double radarTurnRate) {
        if (IsNaN(radarTurnRate)) {
            throw new ArgumentException("radarTurnRate cannot be NaN");
        }
        baseBotInternals.BotIntent.RadarTurnRate  = radarTurnRate;
        RadarTurnRemaining = ToInfiniteValue(radarTurnRate);
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

    internal double DistanceRemaining
    {
        get
        {
            lock (movementLock)
            {
                return distanceRemaining;
            }
        }
        private set
        {
            lock (movementLock)
            {
                distanceRemaining = value;
            }
        }
    }

    internal double TurnRemaining
    {
        get
        {
            lock (turnLock)
            {
                return turnRemaining;
            }
        }
        private set
        {
            lock (turnLock)
            {
                turnRemaining = value;
            }
        }
    }

    internal double GunTurnRemaining
    {
        get
        {
            lock (gunTurnLock)
            {
                return gunTurnRemaining;
            }
        }
        private set
        {
            lock (gunTurnLock)
            {
                gunTurnRemaining = value;
            }
        }
    }

    internal double RadarTurnRemaining
    {
        get
        {
            lock (radarTurnLock)
            {
                return radarTurnRemaining;
            }
        }
        private set
        {
            lock (radarTurnLock)
            {
                radarTurnRemaining = value;
            }
        }
    }

    internal void SetTargetSpeed(double targetSpeed)
    {
        DistanceRemaining = targetSpeed switch
        {
            NaN => throw new ArgumentException("targetSpeed cannot be NaN"),
            > 0 => PositiveInfinity,
            < 0 => NegativeInfinity,
            _ => 0
        };

        baseBotInternals.BotIntent.TargetSpeed = targetSpeed;
    }

    internal void SetForward(double distance)
    {
        if (IsNaN(distance))
            throw new ArgumentException("distance cannot be NaN");

        var speed = baseBotInternals.GetNewTargetSpeed(bot.Speed, distance);
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
            {
                bot.Go();
            } while (IsRunning && (DistanceRemaining != 0 || bot.Speed != 0));
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
            {
                bot.Go();
            } while (IsRunning && (TurnRemaining != 0 || bot.TurnRate != 0));
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
            {
                bot.Go();
            } while (IsRunning && (GunTurnRemaining != 0 || bot.GunTurnRate != 0));
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
            {
                bot.Go();
            } while (IsRunning && (RadarTurnRemaining != 0 || bot.RadarTurnRate != 0));
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
        do
        {
            bot.Go();
        } while (IsRunning && !condition.Test());
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
        lock (turnLock)
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

            baseBotInternals.BotIntent.TurnRate = TurnRemaining;
        }
    }

    private void UpdateGunTurnRemaining()
    {
        lock (gunTurnLock)
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

            baseBotInternals.BotIntent.GunTurnRate = GunTurnRemaining;
        }
    }

    private void UpdateRadarTurnRemaining()
    {
        lock (radarTurnLock)
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

            baseBotInternals.BotIntent.RadarTurnRate = RadarTurnRemaining;
        }
    }

    private void UpdateMovement()
    {
        lock (movementLock)
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
                var speed = baseBotInternals.GetNewTargetSpeed(bot.Speed, distance);
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
    }

    private static bool IsNearZero(double value) => Math.Abs(value) < .00001;
}