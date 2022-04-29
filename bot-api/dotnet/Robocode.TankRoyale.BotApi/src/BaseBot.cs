using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;
using static System.Double;

namespace Robocode.TankRoyale.BotApi
{
    /// <summary>
    /// Abstract bot class that takes care of communication between the bot and the server, and sends
    /// notifications through the event handlers. Most bots can inherit from this class to get access
    /// to basic methods.
    /// </summary>
    public abstract class BaseBot : IBaseBot
    {
        internal readonly BaseBotInternals __baseBotInternals;

        /// <summary>
        /// Constructor for initializing a new instance of the BaseBot class.
        /// This constructor should be used when both <see cref="BotInfo"/> and server URL is provided through
        /// environment variables, i.e., when starting up the bot using a booter. These environment
        /// variables must be set to provide the server URL and bot information, and are automatically
        /// set by the booter tool for Robocode.
        /// </summary>
        /// <example>
        /// Example of how to set the predefined environment variables used for connecting to the server:
        /// <ul>
        /// <li>SERVER_URL=ws://localhost:7654</li>
        /// <li>SERVER_SECRET=xzoEeVbnBe5TGjCny0R1yQ</li>
        /// </ul>
        ///
        /// Example of how to set the environment variables that covers the <see cref="BotInfo"/>:
        /// <ul>
        /// <li>BOT_NAME=MyBot</li>
        /// <li>BOT_VERSION=1.0</li>
        /// <li>BOT_AUTHORS=John Doe</li>
        /// <li>BOT_DESCRIPTION=Short description</li>
        /// <li>BOT_HOMEPAGE=https://somewhere.net/MyBot</li>
        /// <li>BOT_COUNTRY_CODES=us</li>
        /// <li>BOT_GAME_TYPES=melee,1v1</li>
        /// <li>BOT_PLATFORM=.Net 5.0</li>
        /// <li>BOT_PROG_LANG=C# 8.0</li>
        /// <li>BOT_INITIAL_POS=50,70, 270</li>
        /// </ul>
        ///
        /// These environment variables <em>must</em> be set prior to using this constructor:
        /// <ul>
        /// <li>BOT_NAME</li>
        /// <li>BOT_VERSION</li>
        /// <li>BOT_AUTHORS</li>
        /// <li>BOT_GAME_TYPES</li>
        /// </ul>
        /// These value can take multiple values separated by a comma:
        /// <ul>
        /// <li>BOT_AUTHORS, e.g. "John Doe, Jane Doe"</li>
        /// <li>BOT_COUNTRY_CODES, e.g. "se, no, dk"</li>
        /// <li>BOT_GAME_TYPES, e.g. "classic, melee, 1v1"</li>
        /// </ul>
        /// The <c>BOT_INITIAL_POS</c> variable is optional and should <em>only</em> be used for debugging.
        ///
        /// The SERVER_SECRET must be set if the server requires a server secret for the bots trying
        /// to connect. Otherwise, the bot will be disconnected as soon as it attempts to connect to
        /// the server.
        ///
        /// If the SERVER_URL is not set, then this default URL is used: ws://localhost:7654
        /// </example>
        public BaseBot()
        {
            __baseBotInternals = new BaseBotInternals(this, null, null, null);
        }

        /// <summary>
        /// Constructor for initializing a new instance of the BaseBot class.
        /// This constructor assumes the server URL and secret is provided by the environment
        /// variables SERVER_URL and SERVER_SECRET.
        /// </summary>
        /// <param name="botInfo">Is the bot info containing information about your bot.</param>
        public BaseBot(BotInfo botInfo)
        {
            __baseBotInternals = new BaseBotInternals(this, botInfo, null, null);
        }

        /// <summary>
        /// Constructor for initializing a new instance of the BaseBot class.
        /// </summary>
        /// <param name="botInfo">Is the bot info containing information about your bot.</param>
        /// <param name="serverUrl">Is the server URL</param>
        public BaseBot(BotInfo botInfo, Uri serverUrl)
        {
            __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, null);
        }

        /// <summary>
        /// Constructor for initializing a new instance of the BaseBot class.
        /// </summary>
        /// <param name="botInfo">Is the bot info containing information about your bot.</param>
        /// <param name="serverUrl">Is the server URL</param>
        /// <param name="serverSecret">Is the server secret for bots</param>
        public BaseBot(BotInfo botInfo, Uri serverUrl, string serverSecret)
        {
            __baseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, serverSecret);
        }

        /// <inheritdoc/>
        public void Start()
        {
            __baseBotInternals.Start();
        }

        /// <inheritdoc/>
        public void Go()
        {
            __baseBotInternals.Execute();
        }

        /// <inheritdoc/>
        public string Variant => __baseBotInternals.Variant;

        /// <inheritdoc/>
        public string Version => __baseBotInternals.Version;

        /// <inheritdoc/>
        public int MyId => __baseBotInternals.MyId;

        /// <inheritdoc/>
        public string GameType => __baseBotInternals.GameSetup.GameType;

        /// <inheritdoc/>
        public int ArenaWidth => __baseBotInternals.GameSetup.ArenaWidth;

        /// <inheritdoc/>
        public int ArenaHeight => __baseBotInternals.GameSetup.ArenaHeight;

        /// <inheritdoc/>
        public int NumberOfRounds => __baseBotInternals.GameSetup.NumberOfRounds;

        /// <inheritdoc/>
        public double GunCoolingRate => __baseBotInternals.GameSetup.GunCoolingRate;

        /// <inheritdoc/>
        public int? MaxInactivityTurns => __baseBotInternals.GameSetup.MaxInactivityTurns;

        /// <inheritdoc/>
        public int TurnTimeout => __baseBotInternals.GameSetup.TurnTimeout;

        /// <inheritdoc/>
        public int TimeLeft => __baseBotInternals.TimeLeft;

        /// <inheritdoc/>
        public int RoundNumber => __baseBotInternals.CurrentTick.RoundNumber;

        /// <inheritdoc/>
        public int TurnNumber => __baseBotInternals.CurrentTick.TurnNumber;

        /// <inheritdoc/>
        public int EnemyCount => __baseBotInternals.CurrentTick.EnemyCount;

        /// <inheritdoc/>
        public double Energy => __baseBotInternals.CurrentTick.BotState.Energy;

        /// <inheritdoc/>
        public bool IsDisabled => Energy == 0;

        /// <inheritdoc/>
        public double X => __baseBotInternals.CurrentTick.BotState.X;

        /// <inheritdoc/>
        public double Y => __baseBotInternals.CurrentTick.BotState.Y;

        /// <inheritdoc/>
        public double Direction => __baseBotInternals.CurrentTick.BotState.Direction;

        /// <inheritdoc/>
        public double GunDirection => __baseBotInternals.CurrentTick.BotState.GunDirection;

        /// <inheritdoc/>
        public double RadarDirection => __baseBotInternals.CurrentTick.BotState.RadarDirection;

        /// <inheritdoc/>
        public double Speed => __baseBotInternals.CurrentTick.BotState.Speed;

        /// <inheritdoc/>
        public double GunHeat => __baseBotInternals.CurrentTick.BotState.GunHeat;

        /// <inheritdoc/>
        public IEnumerable<BulletState> BulletStates => __baseBotInternals.CurrentTick.BulletStates;

        /// <inheritdoc/>
        public IEnumerable<BotEvent> Events => __baseBotInternals.CurrentTick.Events;

        /// <inheritdoc/>
        public double TurnRate
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("TurnRate cannot be NaN");
                }

                __baseBotInternals.BotIntent.TurnRate = value;
            }
            get => __baseBotInternals.CurrentTick.BotState.TurnRate;
        }

        /// <inheritdoc/>
        public double MaxTurnRate
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("MaxTurnRate cannot be NaN");
                }

                __baseBotInternals.MaxTurnRate = value;
            }
            get => __baseBotInternals.MaxTurnRate;
        }

        /// <inheritdoc/>
        public double GunTurnRate
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("GunTurnRate cannot be NaN");
                }

                __baseBotInternals.BotIntent.GunTurnRate = value;
            }
            get => __baseBotInternals.CurrentTick.BotState.GunTurnRate;
        }

        /// <inheritdoc/>
        public double MaxGunTurnRate
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("MaxGunTurnRate cannot be NaN");
                }

                __baseBotInternals.MaxGunTurnRate = value;
            }
            get => __baseBotInternals.MaxGunTurnRate;
        }

        /// <inheritdoc/>
        public double RadarTurnRate
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("RadarTurnRate cannot be NaN");
                }

                __baseBotInternals.BotIntent.RadarTurnRate = value;
            }
            get => __baseBotInternals.CurrentTick.BotState.RadarTurnRate;
        }

        /// <inheritdoc/>
        public double MaxRadarTurnRate
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("MaxRadarTurnRate cannot be NaN");
                }

                __baseBotInternals.MaxRadarTurnRate = value;
            }
            get => __baseBotInternals.MaxRadarTurnRate;
        }

        /// <inheritdoc/>
        public double TargetSpeed
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("TargetSpeed cannot be NaN");
                }

                __baseBotInternals.BotIntent.TargetSpeed = value;
            }
            get => __baseBotInternals.BotIntent.TargetSpeed ?? 0d;
        }

        /// <inheritdoc/>
        public double MaxSpeed
        {
            set
            {
                if (IsNaN(value))
                {
                    throw new ArgumentException("MaxSpeed cannot be NaN");
                }

                __baseBotInternals.MaxSpeed = value;
            }
            get => __baseBotInternals.MaxSpeed;
        }

        /// <inheritdoc/>
        public bool SetFire(double firepower)
        {
            return __baseBotInternals.SetFire(firepower);
        }

        /// <inheritdoc/>
        public void SetRescan()
        {
            __baseBotInternals.BotIntent.Rescan = true;
        }

        /// <inheritdoc/>
        public bool Interruptible
        {
            set => __baseBotInternals.SetInterruptible(value);
        }

        /// <inheritdoc/>
        public double Firepower => __baseBotInternals.BotIntent.Firepower ?? 0;

        /// <inheritdoc/>
        public bool AdjustGunForBodyTurn
        {
            set => __baseBotInternals.BotIntent.AdjustGunForBodyTurn = value;
            get => __baseBotInternals.BotIntent.AdjustGunForBodyTurn ?? false;
        }

        /// <inheritdoc/>
        public bool AdjustRadarForBodyTurn
        {
            set => __baseBotInternals.BotIntent.AdjustRadarForBodyTurn = value;
            get => __baseBotInternals.BotIntent.AdjustRadarForBodyTurn ?? false;
        }

        /// <inheritdoc/>
        public bool AdjustRadarForGunTurn
        {
            set => __baseBotInternals.BotIntent.AdjustRadarForGunTurn = value;
            get => __baseBotInternals.BotIntent.AdjustRadarForGunTurn ?? false;
        }

        /// <inheritdoc/>
        public void AddCustomEvent(Condition condition)
        {
            __baseBotInternals.AddCondition(condition);
        }

        /// <inheritdoc/>
        public void RemoveCustomEvent(Condition condition)
        {
            __baseBotInternals.RemoveCondition(condition);
        }

        /// <inheritdoc/>
        public void SetStop()
        {
            __baseBotInternals.SetStop();
        }

        /// <inheritdoc/>
        public void SetResume()
        {
            __baseBotInternals.SetResume();
        }

        /// <inheritdoc/>
        public bool IsStopped => __baseBotInternals.IsStopped;

        /// <inheritdoc/>
        public Color BodyColor
        {
            get => __baseBotInternals.CurrentTick.BotState.BodyColor;
            set => __baseBotInternals.BotIntent.BodyColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public Color TurretColor
        {
            get => __baseBotInternals.CurrentTick.BotState.TurretColor;
            set => __baseBotInternals.BotIntent.TurretColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public Color RadarColor
        {
            get => __baseBotInternals.CurrentTick.BotState.RadarColor;
            set => __baseBotInternals.BotIntent.RadarColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public Color BulletColor
        {
            get => __baseBotInternals.CurrentTick.BotState.BulletColor;
            set => __baseBotInternals.BotIntent.BulletColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public Color ScanColor
        {
            get => __baseBotInternals.CurrentTick.BotState.ScanColor;
            set => __baseBotInternals.BotIntent.ScanColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public Color TracksColor
        {
            get => __baseBotInternals.CurrentTick.BotState.TracksColor;
            set => __baseBotInternals.BotIntent.TracksColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public Color GunColor
        {
            get => __baseBotInternals.CurrentTick.BotState.GunColor;
            set => __baseBotInternals.BotIntent.GunColor = ToIntentColor(value);
        }

        /// <inheritdoc/>
        public double CalcMaxTurnRate(double speed) => Constants.MaxTurnRate - 0.75 * Math.Abs(speed);

        /// <inheritdoc/>
        public virtual double CalcBulletSpeed(double firepower) => 20 - 3 * firepower;

        /// <inheritdoc/>
        public virtual double CalcGunHeat(double firepower) => 1 + (firepower / 5);

        /// <inheritdoc/>
        public virtual double CalcBearing(double direction) => NormalizeRelativeAngle(direction - Direction);

        /// <inheritdoc/>
        public virtual double CalcGunBearing(double direction) => NormalizeRelativeAngle(direction - GunDirection);

        /// <inheritdoc/>
        public virtual double CalcRadarBearing(double direction) => NormalizeRelativeAngle(direction - RadarDirection);

        /// <inheritdoc/>
        public virtual double DirectionTo(double x, double y) =>
            NormalizeAbsoluteAngle(180 * Math.Atan2(y - Y, x - X) / Math.PI);

        /// <inheritdoc/>
        public virtual double BearingTo(double x, double y) => NormalizeRelativeAngle(DirectionTo(x, y) - Direction);

        /// <inheritdoc/>
        public virtual double GunBearingTo(double x, double y) =>
            NormalizeRelativeAngle(DirectionTo(x, y) - GunDirection);

        /// <inheritdoc/>
        public virtual double RadarBearingTo(double x, double y) =>
            NormalizeRelativeAngle(DirectionTo(x, y) - RadarDirection);

        /// <inheritdoc/>
        public virtual double DistanceTo(double x, double y)
        {
            var dx = x - X;
            var dy = y - Y;
            return Math.Sqrt(dx * dx + dy * dy);
        }

        /// <inheritdoc/>
        public virtual double NormalizeAbsoluteAngle(double angle) => (angle %= 360) >= 0 ? angle : (angle + 360);

        /// <inheritdoc/>
        public virtual double NormalizeRelativeAngle(double angle) => (angle %= 360) >= 0
            ? ((angle < 180) ? angle : (angle - 360))
            : ((angle >= -180) ? angle : (angle + 360));

        /// <inheritdoc/>
        public virtual double CalcDeltaAngle(double targetAngle, double sourceAngle)
        {
            double angle = targetAngle - sourceAngle;
            angle += (angle > 180) ? -360 : (angle < -180) ? 360 : 0;
            return angle;
        }

        /// <inheritdoc/>
        public virtual void OnConnected(ConnectedEvent connectedEvent) =>
            Console.WriteLine($"Connected to {connectedEvent.ServerUri}");

        /// <inheritdoc/>
        public virtual void OnDisconnected(DisconnectedEvent disconnectedEvent)
        {
            var msg = $"Disconnected from {disconnectedEvent.ServerUri}";
            if (disconnectedEvent.StatusCode != null)
            {
                msg += $", status code: {disconnectedEvent.StatusCode}";
            }

            if (disconnectedEvent.Reason != null)
            {
                msg += $", reason: {disconnectedEvent.Reason}";
            }

            Console.WriteLine(msg);
        }

        /// <inheritdoc/>
        public virtual void OnConnectionError(ConnectionErrorEvent connectionErrorEvent)
        {
            Console.Error.WriteLine($"Connection error with {connectionErrorEvent.ServerUri}");

            var exception = connectionErrorEvent.Exception;
            if (exception != null)
            {
                Console.Error.WriteLine(exception.StackTrace);
            }
        }

        /// <inheritdoc/>
        public virtual void OnGameStarted(GameStartedEvent gameStatedEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnGameEnded(GameEndedEvent gameEndedEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnRoundStarted(RoundStartedEvent roundStatedEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnRoundEnded(RoundEndedEvent roundEndedEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnTick(TickEvent tickEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnBotDeath(DeathEvent botDeathEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnDeath(DeathEvent botDeathEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnHitBot(HitBotEvent botHitBotEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnHitWall(HitWallEvent botHitWallEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnBulletFired(BulletFiredEvent bulletFiredEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnHitByBullet(BulletHitBotEvent bulletHitBotEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnBulletHit(BulletHitBotEvent bulletHitBotEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnBulletHitBullet(BulletHitBulletEvent bulletHitBulletEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnBulletHitWall(BulletHitWallEvent bulletHitWallEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnScannedBot(ScannedBotEvent scannedBotEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnSkippedTurn(SkippedTurnEvent skippedTurnEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnWonRound(WonRoundEvent wonRoundEvent)
        {
        }

        /// <inheritdoc/>
        public virtual void OnCustomEvent(CustomEvent customEvent)
        {
        }

        private static string ToIntentColor(Color color)
        {
            return color == null ? null : "#" + color.ToHex();
        }
    }
}