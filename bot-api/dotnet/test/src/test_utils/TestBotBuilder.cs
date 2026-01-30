using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils;

/// <summary>
/// Builder/factory for creating configurable test bots.
///
/// This builder allows tests to create bots with specific behaviors without
/// creating separate bot classes for each test scenario. It supports:
/// <list type="bullet">
///   <item>Predefined behavior profiles (passive, aggressive, scanning)</item>
///   <item>Custom event handler callbacks for targeted testing</item>
///   <item>Fluent API for easy configuration</item>
/// </list>
///
/// Example usage:
/// <code>
/// var bot = TestBotBuilder.Create()
///     .WithBehavior(BotBehavior.Passive)
///     .OnTick(e => bot.TurnRate = 5)
///     .Build(serverUrl);
/// </code>
/// </summary>
public class TestBotBuilder
{
    /// <summary>
    /// Predefined bot behavior profiles.
    /// </summary>
    public enum BotBehavior
    {
        /// <summary>Does nothing - just calls Go() each tick.</summary>
        Passive,

        /// <summary>Moves forward and fires continuously.</summary>
        Aggressive,

        /// <summary>Continuously rotates radar to scan for enemies.</summary>
        Scanning,

        /// <summary>Custom behavior defined by callbacks only.</summary>
        Custom
    }

    // Bot info configuration
    private string _name = "TestBot";
    private string _version = "1.0";
    private string[] _authors = { "Test Author" };
    private string _description = "Test bot for unit tests";
    private string _platform = ".NET 8";
    private string _programmingLang = "C#";

    // Behavior configuration
    private BotBehavior _behavior = BotBehavior.Passive;

    // Event callbacks
    private Action<TickEvent>? _onTickCallback;
    private Action<ScannedBotEvent>? _onScannedBotCallback;
    private Action<HitBotEvent>? _onHitBotCallback;
    private Action<HitWallEvent>? _onHitWallCallback;
    private Action<BulletFiredEvent>? _onBulletFiredCallback;
    private Action<HitByBulletEvent>? _onHitByBulletCallback;
    private Action<BulletHitBotEvent>? _onBulletHitBotCallback;
    private Action<DeathEvent>? _onDeathCallback;
    private Action<RoundStartedEvent>? _onRoundStartedCallback;
    private Action<RoundEndedEvent>? _onRoundEndedCallback;
    private Action<GameStartedEvent>? _onGameStartedCallback;
    private Action<GameEndedEvent>? _onGameEndedCallback;
    private Action? _onRunCallback;

    private TestBotBuilder() { }

    /// <summary>
    /// Create a new TestBotBuilder instance.
    /// </summary>
    /// <returns>A new builder.</returns>
    public static TestBotBuilder Create() => new();

    /// <summary>
    /// Set the bot name.
    /// </summary>
    /// <param name="name">The bot name.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder WithName(string name)
    {
        _name = name;
        return this;
    }

    /// <summary>
    /// Set the bot version.
    /// </summary>
    /// <param name="version">The bot version.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder WithVersion(string version)
    {
        _version = version;
        return this;
    }

    /// <summary>
    /// Set the bot authors.
    /// </summary>
    /// <param name="authors">The bot authors.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder WithAuthors(params string[] authors)
    {
        _authors = authors;
        return this;
    }

    /// <summary>
    /// Set the bot description.
    /// </summary>
    /// <param name="description">The bot description.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder WithDescription(string description)
    {
        _description = description;
        return this;
    }

    /// <summary>
    /// Set the bot behavior profile.
    /// </summary>
    /// <param name="behavior">The behavior profile.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder WithBehavior(BotBehavior behavior)
    {
        _behavior = behavior;
        return this;
    }

    /// <summary>
    /// Set the onTick callback.
    /// </summary>
    /// <param name="callback">The callback to invoke on tick events.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnTick(Action<TickEvent> callback)
    {
        _onTickCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onScannedBot callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when a bot is scanned.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnScannedBot(Action<ScannedBotEvent> callback)
    {
        _onScannedBotCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onHitBot callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when hitting another bot.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnHitBot(Action<HitBotEvent> callback)
    {
        _onHitBotCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onHitWall callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when hitting a wall.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnHitWall(Action<HitWallEvent> callback)
    {
        _onHitWallCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onBulletFired callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when a bullet is fired.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnBulletFired(Action<BulletFiredEvent> callback)
    {
        _onBulletFiredCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onHitByBullet callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when hit by a bullet.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnHitByBullet(Action<HitByBulletEvent> callback)
    {
        _onHitByBulletCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onBulletHit callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when the bot's bullet hits another bot.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnBulletHit(Action<BulletHitBotEvent> callback)
    {
        _onBulletHitBotCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onDeath callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when the bot dies.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnDeath(Action<DeathEvent> callback)
    {
        _onDeathCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onRoundStarted callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when a round starts.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnRoundStarted(Action<RoundStartedEvent> callback)
    {
        _onRoundStartedCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onRoundEnded callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when a round ends.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnRoundEnded(Action<RoundEndedEvent> callback)
    {
        _onRoundEndedCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onGameStarted callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when the game starts.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnGameStarted(Action<GameStartedEvent> callback)
    {
        _onGameStartedCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onGameEnded callback.
    /// </summary>
    /// <param name="callback">The callback to invoke when the game ends.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnGameEnded(Action<GameEndedEvent> callback)
    {
        _onGameEndedCallback = callback;
        return this;
    }

    /// <summary>
    /// Set the onRun callback.
    /// </summary>
    /// <param name="callback">The callback to invoke in the Run() method.</param>
    /// <returns>This builder.</returns>
    public TestBotBuilder OnRun(Action callback)
    {
        _onRunCallback = callback;
        return this;
    }

    /// <summary>
    /// Build a bot using the default server URL (MockedServer.ServerUrl).
    /// </summary>
    /// <returns>A configured bot instance.</returns>
    public Bot Build()
    {
        return Build(MockedServer.ServerUrl);
    }

    /// <summary>
    /// Build a bot using the specified server URL.
    /// </summary>
    /// <param name="serverUrl">The server URL to connect to.</param>
    /// <returns>A configured bot instance.</returns>
    public Bot Build(Uri serverUrl)
    {
        var botInfoBuilder = BotInfo.Builder()
            .SetName(_name)
            .SetVersion(_version);

        foreach (var author in _authors)
        {
            botInfoBuilder.AddAuthor(author);
        }

        if (!string.IsNullOrEmpty(_description))
        {
            botInfoBuilder.SetDescription(_description);
        }

        botInfoBuilder.SetPlatform(_platform);
        botInfoBuilder.SetProgrammingLang(_programmingLang);

        var botInfo = botInfoBuilder.Build();

        var config = new TestBotConfig
        {
            Behavior = _behavior,
            OnTickCallback = _onTickCallback,
            OnScannedBotCallback = _onScannedBotCallback,
            OnHitBotCallback = _onHitBotCallback,
            OnHitWallCallback = _onHitWallCallback,
            OnBulletFiredCallback = _onBulletFiredCallback,
            OnHitByBulletCallback = _onHitByBulletCallback,
            OnBulletHitBotCallback = _onBulletHitBotCallback,
            OnDeathCallback = _onDeathCallback,
            OnRoundStartedCallback = _onRoundStartedCallback,
            OnRoundEndedCallback = _onRoundEndedCallback,
            OnGameStartedCallback = _onGameStartedCallback,
            OnGameEndedCallback = _onGameEndedCallback,
            OnRunCallback = _onRunCallback
        };

        return new ConfigurableTestBot(botInfo, serverUrl, config);
    }

    /// <summary>
    /// Internal configuration for the test bot.
    /// </summary>
    private class TestBotConfig
    {
        public BotBehavior Behavior { get; set; }
        public Action<TickEvent>? OnTickCallback { get; set; }
        public Action<ScannedBotEvent>? OnScannedBotCallback { get; set; }
        public Action<HitBotEvent>? OnHitBotCallback { get; set; }
        public Action<HitWallEvent>? OnHitWallCallback { get; set; }
        public Action<BulletFiredEvent>? OnBulletFiredCallback { get; set; }
        public Action<HitByBulletEvent>? OnHitByBulletCallback { get; set; }
        public Action<BulletHitBotEvent>? OnBulletHitBotCallback { get; set; }
        public Action<DeathEvent>? OnDeathCallback { get; set; }
        public Action<RoundStartedEvent>? OnRoundStartedCallback { get; set; }
        public Action<RoundEndedEvent>? OnRoundEndedCallback { get; set; }
        public Action<GameStartedEvent>? OnGameStartedCallback { get; set; }
        public Action<GameEndedEvent>? OnGameEndedCallback { get; set; }
        public Action? OnRunCallback { get; set; }
    }

    /// <summary>
    /// Internal bot implementation that delegates to callbacks.
    /// </summary>
    private class ConfigurableTestBot : Bot
    {
        private readonly TestBotConfig _config;

        public ConfigurableTestBot(BotInfo botInfo, Uri serverUrl, TestBotConfig config)
            : base(botInfo, serverUrl)
        {
            _config = config;
        }

        public override void Run()
        {
            // Call custom run callback if provided
            _config.OnRunCallback?.Invoke();

            // Behavior-specific run logic
            while (IsRunning)
            {
                switch (_config.Behavior)
                {
                    case BotBehavior.Passive:
                        // Do nothing, just wait for next turn
                        break;

                    case BotBehavior.Aggressive:
                        // Move forward and fire
                        SetForward(100);
                        SetFire(1);
                        break;

                    case BotBehavior.Scanning:
                        // Spin radar continuously
                        RadarTurnRate = Constants.MaxRadarTurnRate;
                        break;

                    case BotBehavior.Custom:
                        // No default behavior, rely on callbacks
                        break;
                }

                Go();
            }
        }

        public override void OnTick(TickEvent tickEvent)
        {
            _config.OnTickCallback?.Invoke(tickEvent);
        }

        public override void OnScannedBot(ScannedBotEvent scannedBotEvent)
        {
            // Behavior-specific actions
            if (_config.Behavior == BotBehavior.Aggressive)
            {
                // Turn gun toward scanned bot and fire
                var bearingToTarget = GunBearingTo(scannedBotEvent.X, scannedBotEvent.Y);
                SetTurnGunLeft(bearingToTarget);
                SetFire(2);
            }

            _config.OnScannedBotCallback?.Invoke(scannedBotEvent);
        }

        public override void OnHitBot(HitBotEvent botHitBotEvent)
        {
            // Behavior-specific actions
            if (_config.Behavior == BotBehavior.Aggressive)
            {
                // Fire at point-blank range
                SetFire(3);
            }

            _config.OnHitBotCallback?.Invoke(botHitBotEvent);
        }

        public override void OnHitWall(HitWallEvent botHitWallEvent)
        {
            // Behavior-specific actions
            if (_config.Behavior == BotBehavior.Aggressive)
            {
                // Reverse direction when hitting wall
                SetForward(-100);
            }

            _config.OnHitWallCallback?.Invoke(botHitWallEvent);
        }

        public override void OnBulletFired(BulletFiredEvent bulletFiredEvent)
        {
            _config.OnBulletFiredCallback?.Invoke(bulletFiredEvent);
        }

        public override void OnHitByBullet(HitByBulletEvent hitByBulletEvent)
        {
            _config.OnHitByBulletCallback?.Invoke(hitByBulletEvent);
        }

        public override void OnBulletHit(BulletHitBotEvent bulletHitBotEvent)
        {
            _config.OnBulletHitBotCallback?.Invoke(bulletHitBotEvent);
        }

        public override void OnDeath(DeathEvent deathEvent)
        {
            _config.OnDeathCallback?.Invoke(deathEvent);
        }

        public override void OnRoundStarted(RoundStartedEvent roundStartedEvent)
        {
            _config.OnRoundStartedCallback?.Invoke(roundStartedEvent);
        }

        public override void OnRoundEnded(RoundEndedEvent roundEndedEvent)
        {
            _config.OnRoundEndedCallback?.Invoke(roundEndedEvent);
        }

        public override void OnGameStarted(GameStartedEvent gameStartedEvent)
        {
            _config.OnGameStartedCallback?.Invoke(gameStartedEvent);
        }

        public override void OnGameEnded(GameEndedEvent gameEndedEvent)
        {
            _config.OnGameEndedCallback?.Invoke(gameEndedEvent);
        }
    }
}
