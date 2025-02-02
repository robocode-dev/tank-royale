using System;
using System.Collections.Generic;
using System.Drawing;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Internal;
using SvgNet.Interfaces;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Abstract bot class that takes care of communication between the bot and the server, and sends
/// notifications through the event handlers. Most bots can inherit from this class to get access
/// to basic methods.
/// </summary>
public abstract class BaseBot : IBaseBot
{
    internal readonly BaseBotInternals BaseBotInternals;

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
    /// <li>BOT_PLATFORM=.Net 6.0</li>
    /// <li>BOT_PROG_LANG=C# 10.0</li>
    /// <li>BOT_INITIAL_POS=50,70, 270</li>
    /// </ul>
    ///
    /// These environment variables <em>must</em> be set prior to using this constructor:
    /// <ul>
    /// <li>BOT_NAME</li>
    /// <li>BOT_VERSION</li>
    /// <li>BOT_AUTHORS</li>
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
    protected BaseBot() => BaseBotInternals = new BaseBotInternals(this, null, null, null);

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// This constructor assumes the server URL and secret is provided by the environment
    /// variables SERVER_URL and SERVER_SECRET.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    protected BaseBot(BotInfo botInfo) =>
        BaseBotInternals = new BaseBotInternals(this, botInfo, null, null);

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URL</param>
    protected BaseBot(BotInfo botInfo, Uri serverUrl) =>
        BaseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, null);

    /// <summary>
    /// Constructor for initializing a new instance of the BaseBot class.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUrl">Is the server URL</param>
    /// <param name="serverSecret">Is the server secret for bots</param>
    protected BaseBot(BotInfo botInfo, Uri serverUrl, string serverSecret) =>
        BaseBotInternals = new BaseBotInternals(this, botInfo, serverUrl, serverSecret);

    /// <inheritdoc/>
    public void Start() => BaseBotInternals.Start();

    /// <inheritdoc/>
    public void Go() => BaseBotInternals.Execute();

    /// <inheritdoc/>
    public string Variant => BaseBotInternals.Variant;

    /// <inheritdoc/>
    public string Version => BaseBotInternals.Version;

    /// <inheritdoc/>
    public int MyId => BaseBotInternals.MyId;

    /// <inheritdoc/>
    public string GameType => BaseBotInternals.GameSetup.GameType;

    /// <inheritdoc/>
    public int ArenaWidth => BaseBotInternals.GameSetup.ArenaWidth;

    /// <inheritdoc/>
    public int ArenaHeight => BaseBotInternals.GameSetup.ArenaHeight;

    /// <inheritdoc/>
    public int NumberOfRounds => BaseBotInternals.GameSetup.NumberOfRounds;

    /// <inheritdoc/>
    public double GunCoolingRate => BaseBotInternals.GameSetup.GunCoolingRate;

    /// <inheritdoc/>
    public int? MaxInactivityTurns => BaseBotInternals.GameSetup.MaxInactivityTurns;

    /// <inheritdoc/>
    public int TurnTimeout => BaseBotInternals.GameSetup.TurnTimeout;

    /// <inheritdoc/>
    public int TimeLeft => BaseBotInternals.TimeLeft;

    /// <inheritdoc/>
    public int RoundNumber => BaseBotInternals.CurrentTickOrThrow.RoundNumber;

    /// <inheritdoc/>
    public int TurnNumber => BaseBotInternals.CurrentTickOrThrow.TurnNumber;

    /// <inheritdoc/>
    public int EnemyCount => BaseBotInternals.CurrentTickOrThrow.BotState.EnemyCount;

    /// <inheritdoc/>
    public double Energy => BaseBotInternals.CurrentTickOrThrow.BotState.Energy;

    /// <inheritdoc/>
    public bool IsDisabled => Energy == 0;

    /// <inheritdoc/>
    public double X => BaseBotInternals.CurrentTickOrThrow.BotState.X;

    /// <inheritdoc/>
    public double Y => BaseBotInternals.CurrentTickOrThrow.BotState.Y;

    /// <inheritdoc/>
    public double Direction => BaseBotInternals.CurrentTickOrThrow.BotState.Direction;

    /// <inheritdoc/>
    public double GunDirection => BaseBotInternals.CurrentTickOrThrow.BotState.GunDirection;

    /// <inheritdoc/>
    public double RadarDirection => BaseBotInternals.CurrentTickOrThrow.BotState.RadarDirection;

    /// <inheritdoc/>
    public double Speed => BaseBotInternals.Speed;

    /// <inheritdoc/>
    public double GunHeat => BaseBotInternals.GunHeat;

    /// <inheritdoc/>
    public IEnumerable<BulletState> BulletStates => BaseBotInternals.BulletStates;

    /// <inheritdoc/>
    public IList<BotEvent> Events => BaseBotInternals.Events;

    /// <inheritdoc/>
    public void ClearEvents() => BaseBotInternals.ClearEvents();

    /// <inheritdoc/>
    public virtual double TurnRate
    {
        set => BaseBotInternals.TurnRate = value;
        get => BaseBotInternals.TurnRate;
    }

    /// <inheritdoc/>
    public double MaxTurnRate
    {
        set => BaseBotInternals.MaxTurnRate = value;
        get => BaseBotInternals.MaxTurnRate;
    }

    /// <inheritdoc/>
    public virtual double GunTurnRate
    {
        set => BaseBotInternals.GunTurnRate = value;
        get => BaseBotInternals.GunTurnRate;
    }

    /// <inheritdoc/>
    public double MaxGunTurnRate
    {
        set => BaseBotInternals.MaxGunTurnRate = value;
        get => BaseBotInternals.MaxGunTurnRate;
    }

    /// <inheritdoc/>
    public virtual double RadarTurnRate
    {
        set => BaseBotInternals.RadarTurnRate = value;
        get => BaseBotInternals.RadarTurnRate;
    }

    /// <inheritdoc/>
    public double MaxRadarTurnRate
    {
        set => BaseBotInternals.MaxRadarTurnRate = value;
        get => BaseBotInternals.MaxRadarTurnRate;
    }

    /// <inheritdoc/>
    public double TargetSpeed
    {
        set => BaseBotInternals.TargetSpeed = value;
        get => BaseBotInternals.TargetSpeed;
    }

    /// <inheritdoc/>
    public double MaxSpeed
    {
        set => BaseBotInternals.MaxSpeed = value;
        get => BaseBotInternals.MaxSpeed;
    }

    /// <inheritdoc/>
    public bool SetFire(double firepower) => BaseBotInternals.SetFire(firepower);

    /// <inheritdoc/>
    public void SetRescan() => BaseBotInternals.BotIntent.Rescan = true;

    public void SetFireAssist(bool enable) => BaseBotInternals.BotIntent.FireAssist = enable;
    
    /// <inheritdoc/>
    public bool Interruptible {
        set => BaseBotInternals.SetInterruptible(value);
    }

    /// <inheritdoc/>
    public double Firepower => BaseBotInternals.BotIntent.Firepower ?? 0;

    /// <inheritdoc/>
    public bool AdjustGunForBodyTurn
    {
        set => BaseBotInternals.BotIntent.AdjustGunForBodyTurn = value;
        get => BaseBotInternals.BotIntent.AdjustGunForBodyTurn ?? false;
    }

    /// <inheritdoc/>
    public bool AdjustRadarForBodyTurn
    {
        set => BaseBotInternals.BotIntent.AdjustRadarForBodyTurn = value;
        get => BaseBotInternals.BotIntent.AdjustRadarForBodyTurn ?? false;
    }

    /// <inheritdoc/>
    public bool AdjustRadarForGunTurn
    {
        set
        {
            var botIntent = BaseBotInternals.BotIntent;
            botIntent.AdjustRadarForGunTurn = value;
            botIntent.FireAssist = !value;
        }
        get => BaseBotInternals.BotIntent.AdjustRadarForGunTurn ?? false;
    }

    /// <inheritdoc/>
    public bool AddCustomEvent(Condition condition) => BaseBotInternals.AddCondition(condition);

    /// <inheritdoc/>
    public bool RemoveCustomEvent(Condition condition) => BaseBotInternals.RemoveCondition(condition);

    /// <inheritdoc/>
    public void SetStop() => BaseBotInternals.SetStop(false);

    /// <inheritdoc/>
    public void SetStop(bool overwrite) => BaseBotInternals.SetStop(overwrite);

    /// <inheritdoc/>
    public void SetResume() => BaseBotInternals.SetResume();

    /// <inheritdoc/>
    public bool IsStopped => BaseBotInternals.IsStopped;

    /// <inheritdoc/>
    public ICollection<int> TeammateIds => BaseBotInternals.TeammateIds;

    /// <inheritdoc/>
    public bool IsTeammate(int botId) => BaseBotInternals.IsTeammate(botId);

    /// <inheritdoc/>
    public void BroadcastTeamMessage(object message) => BaseBotInternals.BroadcastTeamMessage(message);

    /// <inheritdoc/>
    public void SendTeamMessage(int teammateId, object message) =>
        BaseBotInternals.SendTeamMessage(teammateId, message);

    /// <inheritdoc/>
    public Color? BodyColor
    {
        get => BaseBotInternals.BodyColor;
        set => BaseBotInternals.BodyColor = value;
    }

    /// <inheritdoc/>
    public Color? TurretColor
    {
        get => BaseBotInternals.TurretColor;
        set => BaseBotInternals.TurretColor = value;
    }

    /// <inheritdoc/>
    public Color? RadarColor
    {
        get => BaseBotInternals.RadarColor;
        set => BaseBotInternals.RadarColor = value;
    }

    /// <inheritdoc/>
    public Color? BulletColor
    {
        get => BaseBotInternals.BulletColor;
        set => BaseBotInternals.BulletColor = value;
    }

    /// <inheritdoc/>
    public Color? ScanColor
    {
        get => BaseBotInternals.ScanColor;
        set => BaseBotInternals.ScanColor = value;
    }

    /// <inheritdoc/>
    public Color? TracksColor
    {
        get => BaseBotInternals.TracksColor;
        set => BaseBotInternals.TracksColor = value;
    }

    /// <inheritdoc/>
    public Color? GunColor
    {
        get => BaseBotInternals.GunColor;
        set => BaseBotInternals.GunColor = value;
    }

    /// <inheritdoc/>
    public double CalcMaxTurnRate(double speed) =>
        Constants.MaxTurnRate - 0.75 * Math.Abs(Math.Clamp(speed, -Constants.MaxSpeed, Constants.MaxSpeed));

    /// <inheritdoc/>
    public virtual double CalcBulletSpeed(double firepower) =>
        20 - 3 * Math.Clamp(firepower, Constants.MinFirepower, Constants.MaxFirepower);

    /// <inheritdoc/>
    public virtual double CalcGunHeat(double firepower) =>
        1 + (Math.Clamp(firepower, Constants.MinFirepower, Constants.MaxFirepower) / 5);

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
        var angle = targetAngle - sourceAngle;
        angle += (angle > 180) ? -360 : (angle < -180) ? 360 : 0;
        return angle;
    }

    /// <inheritdoc/>
    public virtual int GetEventPriority(Type eventType) => BaseBotInternals.GetPriority(eventType);

    /// <inheritdoc/>
    public virtual void SetEventPriority(Type eventType, int priority) =>
        BaseBotInternals.SetPriority(eventType, priority);

    /// <inheritdoc/>
    public virtual bool IsDebuggingEnabled => BaseBotInternals.CurrentTickOrThrow.BotState.IsDebuggingEnabled;

    /// <inheritdoc/>
    public virtual IGraphics Graphics => BaseBotInternals.Graphics;
    
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
    public virtual void OnBotDeath(BotDeathEvent botDeathEvent)
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
    public virtual void OnHitByBullet(HitByBulletEvent bulletHitBotEvent)
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
    
    /// <inheritdoc/>
    public virtual void OnTeamMessage(TeamMessageEvent teamMessageEvent)
    {
    }
}