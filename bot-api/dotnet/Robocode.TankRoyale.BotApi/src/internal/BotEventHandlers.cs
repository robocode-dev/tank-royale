using System;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal;

internal sealed class BotEventHandlers
{
    private readonly IBaseBot baseBot;

    internal readonly EventHandler<ConnectedEvent> OnConnected = new();
    internal readonly EventHandler<DisconnectedEvent> OnDisconnected = new();
    internal readonly EventHandler<ConnectionErrorEvent> OnConnectionError = new();

    internal readonly EventHandler<GameStartedEvent> OnGameStarted = new();
    internal readonly EventHandler<GameEndedEvent> OnGameEnded = new();
    internal readonly EventHandler<object> OnGameAborted = new();
    internal readonly EventHandler<RoundStartedEvent> OnRoundStarted = new();
    internal readonly EventHandler<RoundEndedEvent> OnRoundEnded = new();
    internal readonly EventHandler<TickEvent> OnTick = new();
    internal readonly EventHandler<SkippedTurnEvent> OnSkippedTurn = new();
    internal readonly EventHandler<DeathEvent> OnDeath = new();
    internal readonly EventHandler<BotDeathEvent> OnBotDeath = new();
    internal readonly EventHandler<HitBotEvent> OnHitBot = new();
    internal readonly EventHandler<HitWallEvent> OnHitWall = new();
    internal readonly EventHandler<BulletFiredEvent> OnBulletFired = new();
    internal readonly EventHandler<HitByBulletEvent> OnHitByBullet = new();
    internal readonly EventHandler<BulletHitBotEvent> OnBulletHit = new();
    internal readonly EventHandler<BulletHitBulletEvent> OnBulletHitBullet = new();
    internal readonly EventHandler<BulletHitWallEvent> OnBulletHitWall = new();
    internal readonly EventHandler<ScannedBotEvent> OnScannedBot = new();
    internal readonly EventHandler<WonRoundEvent> OnWonRound = new();
    internal readonly EventHandler<CustomEvent> OnCustomEvent = new();
    internal readonly EventHandler<TeamMessageEvent> OnTeamMessageEvent = new();

    internal readonly EventHandler<TickEvent> OnNextTurn = new();

    // Events
    private event EventHandler<ConnectedEvent>.Subscriber ConnectedEvent;
    private event EventHandler<DisconnectedEvent>.Subscriber DisconnectedEvent;
    private event EventHandler<ConnectionErrorEvent>.Subscriber ConnectionErrorEvent;
    private event EventHandler<GameStartedEvent>.Subscriber GameStartedEvent;
    private event EventHandler<GameEndedEvent>.Subscriber GameEndedEvent;
    private event EventHandler<object>.Subscriber GameAbortedEvent;
    private event EventHandler<RoundStartedEvent>.Subscriber RoundStartedEvent;
    private event EventHandler<RoundEndedEvent>.Subscriber RoundEndedEvent;
    private event EventHandler<TickEvent>.Subscriber TickEvent;
    private event EventHandler<SkippedTurnEvent>.Subscriber SkippedTurnEvent;
    private event EventHandler<DeathEvent>.Subscriber DeathEvent;
    private event EventHandler<BotDeathEvent>.Subscriber BotDeathEvent;
    private event EventHandler<HitBotEvent>.Subscriber HitBotEvent;
    private event EventHandler<HitWallEvent>.Subscriber HitWallEvent;
    private event EventHandler<BulletFiredEvent>.Subscriber BulletFiredEvent;
    private event EventHandler<HitByBulletEvent>.Subscriber HitByBulletEvent;
    private event EventHandler<BulletHitBotEvent>.Subscriber BulletHitEvent;
    private event EventHandler<BulletHitBulletEvent>.Subscriber BulletHitBulletEvent;
    private event EventHandler<BulletHitWallEvent>.Subscriber BulletHitWallEvent;
    private event EventHandler<ScannedBotEvent>.Subscriber ScannedBotEvent;
    private event EventHandler<WonRoundEvent>.Subscriber WonRoundEvent;
    private event EventHandler<CustomEvent>.Subscriber CustomEvent;
    private event EventHandler<TeamMessageEvent>.Subscriber TeamMessageEvent;

    private event EventHandler<TickEvent>.Subscriber NextTurnEvent;

    internal BotEventHandlers(IBaseBot baseBot)
    {
        this.baseBot = baseBot;

        // Regular handlers
        ConnectedEvent += OnConnected.Publish;
        DisconnectedEvent += OnDisconnected.Publish;
        ConnectionErrorEvent += OnConnectionError.Publish;
        GameStartedEvent += OnGameStarted.Publish;
        GameEndedEvent += OnGameEnded.Publish;
        GameAbortedEvent += OnGameAborted.Publish;
        RoundStartedEvent += OnRoundStarted.Publish;
        RoundEndedEvent += OnRoundEnded.Publish;
        TickEvent += OnTick.Publish;
        SkippedTurnEvent += OnSkippedTurn.Publish;
        DeathEvent += OnDeath.Publish;
        BotDeathEvent += OnBotDeath.Publish;
        HitBotEvent += OnHitBot.Publish;
        HitWallEvent += OnHitWall.Publish;
        BulletFiredEvent += OnBulletFired.Publish;
        HitByBulletEvent += OnHitByBullet.Publish;
        BulletHitEvent += OnBulletHit.Publish;
        BulletHitBulletEvent += OnBulletHitBullet.Publish;
        BulletHitWallEvent += OnBulletHitWall.Publish;
        ScannedBotEvent += OnScannedBot.Publish;
        WonRoundEvent += OnWonRound.Publish;
        CustomEvent += OnCustomEvent.Publish;

        NextTurnEvent += OnNextTurn.Publish;

        // Subscribe to bot events
        OnConnected.Subscribe(baseBot.OnConnected);
        OnDisconnected.Subscribe(baseBot.OnDisconnected);
        OnConnectionError.Subscribe(baseBot.OnConnectionError);
        OnGameStarted.Subscribe(baseBot.OnGameStarted);
        OnGameEnded.Subscribe(baseBot.OnGameEnded);
        OnRoundStarted.Subscribe(baseBot.OnRoundStarted);
        OnRoundEnded.Subscribe(baseBot.OnRoundEnded);
        OnTick.Subscribe(baseBot.OnTick);
        OnSkippedTurn.Subscribe(baseBot.OnSkippedTurn);
        OnDeath.Subscribe(baseBot.OnDeath);
        OnBotDeath.Subscribe(baseBot.OnBotDeath);
        OnHitBot.Subscribe(baseBot.OnHitBot);
        OnHitWall.Subscribe(baseBot.OnHitWall);
        OnBulletFired.Subscribe(baseBot.OnBulletFired);
        OnHitByBullet.Subscribe(baseBot.OnHitByBullet);
        OnBulletHit.Subscribe(baseBot.OnBulletHit);
        OnBulletHitBullet.Subscribe(baseBot.OnBulletHitBullet);
        OnBulletHitWall.Subscribe(baseBot.OnBulletHitWall);
        OnScannedBot.Subscribe(baseBot.OnScannedBot);
        OnWonRound.Subscribe(baseBot.OnWonRound);
        OnCustomEvent.Subscribe(baseBot.OnCustomEvent);
    }

    internal void FireConnectedEvent(ConnectedEvent evt)
    {
        ConnectedEvent?.Invoke(evt);
    }

    internal void FireDisconnectedEvent(DisconnectedEvent evt)
    {
        DisconnectedEvent?.Invoke(evt);
    }

    internal void FireConnectionErrorEvent(ConnectionErrorEvent evt)
    {
        ConnectionErrorEvent?.Invoke(evt);
    }

    internal void FireGameStartedEvent(GameStartedEvent evt)
    {
        GameStartedEvent?.Invoke(evt);
    }

    internal void FireGameEndedEvent(GameEndedEvent evt)
    {
        GameEndedEvent?.Invoke(evt);
    }

    internal void FireGameAbortedEvent()
    {
        GameAbortedEvent?.Invoke(null);
    }

    internal void FireRoundStartedEvent(RoundStartedEvent evt)
    {
        RoundStartedEvent?.Invoke(evt);
    }

    internal void FireRoundEndedEvent(RoundEndedEvent evt)
    {
        RoundEndedEvent?.Invoke(evt);
    }

    internal void FireSkippedTurnEvent(SkippedTurnEvent evt)
    {
        SkippedTurnEvent?.Invoke(evt);
    }

    internal void FireNextTurn(TickEvent evt)
    {
        NextTurnEvent?.Invoke(evt);
    }


    internal void Fire(BotEvent evt)
    {
        switch (evt)
        {
            case TickEvent tickEvent:
                TickEvent?.Invoke(tickEvent);
                break;
            case ScannedBotEvent scannedBotEvent:
                ScannedBotEvent?.Invoke(scannedBotEvent);
                break;
            case SkippedTurnEvent skippedTurnEvent:
                SkippedTurnEvent?.Invoke(skippedTurnEvent);
                break;
            case HitBotEvent botHitBotEvent:
                HitBotEvent?.Invoke(botHitBotEvent);
                break;
            case HitWallEvent botHitWallEvent:
                HitWallEvent?.Invoke(botHitWallEvent);
                break;
            case BulletFiredEvent bulletFiredEvent:
                BulletFiredEvent?.Invoke(bulletFiredEvent);
                break;
            case BulletHitWallEvent bulletHitWallEvent:
                BulletHitWallEvent?.Invoke(bulletHitWallEvent);
                break;
            case HitByBulletEvent hitByBulletEvent:
                HitByBulletEvent?.Invoke(hitByBulletEvent);
                break;
            case BulletHitBotEvent bulletHitBotEvent:
                BulletHitEvent?.Invoke(bulletHitBotEvent);
                break;
            case BotDeathEvent botDeathEvent:
                BotDeathEvent?.Invoke(botDeathEvent);
                break;
            case DeathEvent deathEvent:
                DeathEvent?.Invoke(deathEvent);
                break;
            case BulletHitBulletEvent bulletHitBulletEvent:
                BulletHitBulletEvent?.Invoke(bulletHitBulletEvent);
                break;
            case WonRoundEvent wonRoundEvent:
                WonRoundEvent?.Invoke(wonRoundEvent);
                break;
            case CustomEvent customEvent:
                CustomEvent?.Invoke(customEvent);
                break;
            case TeamMessageEvent teamMessageEvent:
                TeamMessageEvent?.Invoke(teamMessageEvent);
                break;
            default:
                throw new InvalidOperationException("Unhandled event: " + evt);
        }
    }
}