using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Class used for bot event handlers on the public API that are may or may not be triggered bot event queue, and might
/// not be handled immediately by the bot logic.
/// </summary>
sealed class BotEventHandlers
{
    internal readonly EventHandler<ConnectedEvent> OnConnected = new();
    internal readonly EventHandler<DisconnectedEvent> OnDisconnected = new();
    internal readonly EventHandler<ConnectionErrorEvent> OnConnectionError = new();

    internal readonly EventHandler<GameStartedEvent> OnGameStarted = new();
    internal readonly EventHandler<GameEndedEvent> OnGameEnded = new();
    internal readonly EventHandler<GameAbortedEvent> OnGameAborted = new();
    internal readonly EventHandler<RoundStartedEvent> OnRoundStarted = new();
    internal readonly EventHandler<RoundEndedEvent> OnRoundEnded = new();

    private readonly EventHandler<TickEvent> OnTick = new();
    internal readonly EventHandler<SkippedTurnEvent> OnSkippedTurn = new();
    private readonly EventHandler<DeathEvent> OnDeath = new();
    private readonly EventHandler<BotDeathEvent> OnBotDeath = new();
    private readonly EventHandler<HitBotEvent> OnHitBot = new();
    private readonly EventHandler<HitWallEvent> OnHitWall = new();
    private readonly EventHandler<BulletFiredEvent> OnBulletFired = new();
    private readonly EventHandler<HitByBulletEvent> OnHitByBullet = new();
    private readonly EventHandler<BulletHitBotEvent> OnBulletHit = new();
    private readonly EventHandler<BulletHitBulletEvent> OnBulletHitBullet = new();
    private readonly EventHandler<BulletHitWallEvent> OnBulletHitWall = new();
    private readonly EventHandler<ScannedBotEvent> OnScannedBot = new();
    private readonly EventHandler<WonRoundEvent> OnWonRound = new();
    private readonly EventHandler<CustomEvent> OnCustomEvent = new();
    private readonly EventHandler<TeamMessageEvent> OnTeamMessage = new();

    private readonly Dictionary<Type, Action<IEvent>> _eventHandlers = new();


    internal BotEventHandlers(IBaseBot baseBot)
    {
        InitializeEventHandlers();
        SubscribeToEventHandlers(baseBot);
    }

    private void InitializeEventHandlers()
    {
        _eventHandlers[typeof(ConnectedEvent)] = e => OnConnected.Publish((ConnectedEvent)e);
        _eventHandlers[typeof(DisconnectedEvent)] = e => OnDisconnected.Publish((DisconnectedEvent)e);
        _eventHandlers[typeof(ConnectionErrorEvent)] = e => OnConnectionError.Publish((ConnectionErrorEvent)e);

        _eventHandlers[typeof(GameStartedEvent)] = e => OnGameStarted.Publish((GameStartedEvent)e);
        _eventHandlers[typeof(GameEndedEvent)] = e => OnGameEnded.Publish((GameEndedEvent)e);
        _eventHandlers[typeof(GameAbortedEvent)] = e => OnGameAborted.Publish((GameAbortedEvent)e);
        _eventHandlers[typeof(RoundStartedEvent)] = e => OnRoundStarted.Publish((RoundStartedEvent)e);
        _eventHandlers[typeof(RoundEndedEvent)] = e => OnRoundEnded.Publish((RoundEndedEvent)e);

        _eventHandlers[typeof(TickEvent)] = e => OnTick.Publish((TickEvent)e);
        _eventHandlers[typeof(SkippedTurnEvent)] = e => OnSkippedTurn.Publish((SkippedTurnEvent)e);
        _eventHandlers[typeof(DeathEvent)] = e => OnDeath.Publish((DeathEvent)e);
        _eventHandlers[typeof(BotDeathEvent)] = e => OnBotDeath.Publish((BotDeathEvent)e);
        _eventHandlers[typeof(HitBotEvent)] = e => OnHitBot.Publish((HitBotEvent)e);
        _eventHandlers[typeof(HitWallEvent)] = e => OnHitWall.Publish((HitWallEvent)e);
        _eventHandlers[typeof(BulletFiredEvent)] = e => OnBulletFired.Publish((BulletFiredEvent)e);
        _eventHandlers[typeof(HitByBulletEvent)] = e => OnHitByBullet.Publish((HitByBulletEvent)e);
        _eventHandlers[typeof(BulletHitBotEvent)] = e => OnBulletHit.Publish((BulletHitBotEvent)e);
        _eventHandlers[typeof(BulletHitBulletEvent)] = e => OnBulletHitBullet.Publish((BulletHitBulletEvent)e);
        _eventHandlers[typeof(BulletHitWallEvent)] = e => OnBulletHitWall.Publish((BulletHitWallEvent)e);
        _eventHandlers[typeof(ScannedBotEvent)] = e => OnScannedBot.Publish((ScannedBotEvent)e);
        _eventHandlers[typeof(WonRoundEvent)] = e => OnWonRound.Publish((WonRoundEvent)e);
        _eventHandlers[typeof(CustomEvent)] = e => OnCustomEvent.Publish((CustomEvent)e);
        _eventHandlers[typeof(TeamMessageEvent)] = e => OnTeamMessage.Publish((TeamMessageEvent)e);
    }

    private void SubscribeToEventHandlers(IBaseBot baseBot)
    {
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
        OnTeamMessage.Subscribe(baseBot.OnTeamMessage);
    }

    internal void FireEvent(BotEvent botEvent)
    {
        if (_eventHandlers.TryGetValue(botEvent.GetType(), out var action))
        {
            action.Invoke(botEvent); // calls Publish(botEvent)
        }
        else
        {
            throw new InvalidOperationException("Unhandled event: " + botEvent);
        }
    }

    // Virtual (fake) event:
    internal sealed class GameAbortedEvent : IEvent
    {
    }
}