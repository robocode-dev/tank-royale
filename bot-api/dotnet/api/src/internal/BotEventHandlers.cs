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

    private readonly EventHandler<TickEvent> _onTick = new();
    internal readonly EventHandler<SkippedTurnEvent> OnSkippedTurn = new();
    private readonly EventHandler<DeathEvent> _onDeath = new();
    private readonly EventHandler<BotDeathEvent> _onBotDeath = new();
    private readonly EventHandler<HitBotEvent> _onHitBot = new();
    private readonly EventHandler<HitWallEvent> _onHitWall = new();
    private readonly EventHandler<BulletFiredEvent> _onBulletFired = new();
    private readonly EventHandler<HitByBulletEvent> _onHitByBullet = new();
    private readonly EventHandler<BulletHitBotEvent> _onBulletHit = new();
    private readonly EventHandler<BulletHitBulletEvent> _onBulletHitBullet = new();
    private readonly EventHandler<BulletHitWallEvent> _onBulletHitWall = new();
    private readonly EventHandler<ScannedBotEvent> _onScannedBot = new();
    private readonly EventHandler<WonRoundEvent> _onWonRound = new();
    private readonly EventHandler<CustomEvent> _onCustomEvent = new();
    private readonly EventHandler<TeamMessageEvent> _onTeamMessage = new();

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

        _eventHandlers[typeof(TickEvent)] = e => _onTick.Publish((TickEvent)e);
        _eventHandlers[typeof(SkippedTurnEvent)] = e => OnSkippedTurn.Publish((SkippedTurnEvent)e);
        _eventHandlers[typeof(DeathEvent)] = e => _onDeath.Publish((DeathEvent)e);
        _eventHandlers[typeof(BotDeathEvent)] = e => _onBotDeath.Publish((BotDeathEvent)e);
        _eventHandlers[typeof(HitBotEvent)] = e => _onHitBot.Publish((HitBotEvent)e);
        _eventHandlers[typeof(HitWallEvent)] = e => _onHitWall.Publish((HitWallEvent)e);
        _eventHandlers[typeof(BulletFiredEvent)] = e => _onBulletFired.Publish((BulletFiredEvent)e);
        _eventHandlers[typeof(HitByBulletEvent)] = e => _onHitByBullet.Publish((HitByBulletEvent)e);
        _eventHandlers[typeof(BulletHitBotEvent)] = e => _onBulletHit.Publish((BulletHitBotEvent)e);
        _eventHandlers[typeof(BulletHitBulletEvent)] = e => _onBulletHitBullet.Publish((BulletHitBulletEvent)e);
        _eventHandlers[typeof(BulletHitWallEvent)] = e => _onBulletHitWall.Publish((BulletHitWallEvent)e);
        _eventHandlers[typeof(ScannedBotEvent)] = e => _onScannedBot.Publish((ScannedBotEvent)e);
        _eventHandlers[typeof(WonRoundEvent)] = e => _onWonRound.Publish((WonRoundEvent)e);
        _eventHandlers[typeof(CustomEvent)] = e => _onCustomEvent.Publish((CustomEvent)e);
        _eventHandlers[typeof(TeamMessageEvent)] = e => _onTeamMessage.Publish((TeamMessageEvent)e);
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

        _onTick.Subscribe(baseBot.OnTick);
        OnSkippedTurn.Subscribe(baseBot.OnSkippedTurn);
        _onDeath.Subscribe(baseBot.OnDeath);
        _onBotDeath.Subscribe(baseBot.OnBotDeath);
        _onHitBot.Subscribe(baseBot.OnHitBot);
        _onHitWall.Subscribe(baseBot.OnHitWall);
        _onBulletFired.Subscribe(baseBot.OnBulletFired);
        _onHitByBullet.Subscribe(baseBot.OnHitByBullet);
        _onBulletHit.Subscribe(baseBot.OnBulletHit);
        _onBulletHitBullet.Subscribe(baseBot.OnBulletHitBullet);
        _onBulletHitWall.Subscribe(baseBot.OnBulletHitWall);
        _onScannedBot.Subscribe(baseBot.OnScannedBot);
        _onWonRound.Subscribe(baseBot.OnWonRound);
        _onCustomEvent.Subscribe(baseBot.OnCustomEvent);
        _onTeamMessage.Subscribe(baseBot.OnTeamMessage);
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