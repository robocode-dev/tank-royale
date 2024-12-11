using System;
using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal;

/// <summary>
/// Class used for instant event handling only used for updating the state of the API internals.
/// </summary>
internal sealed class InstantEventHandlers
{
    internal readonly EventHandler<DisconnectedEvent> OnDisconnected = new();

    internal readonly EventHandler<GameEndedEvent> OnGameEnded = new();
    internal readonly EventHandler<GameAbortedEvent> OnGameAborted = new();
    internal readonly EventHandler<RoundStartedEvent> OnRoundStarted = new();
    internal readonly EventHandler<RoundEndedEvent> OnRoundEnded = new();

    internal readonly EventHandler<DeathEvent> OnDeath = new();
    internal readonly EventHandler<HitBotEvent> OnHitBot = new();
    internal readonly EventHandler<HitWallEvent> OnHitWall = new();
    internal readonly EventHandler<BulletFiredEvent> OnBulletFired = new();

    // Virtual (fake) event handler
    internal readonly EventHandler<TickEvent> OnNextTurn = new();

    private readonly Dictionary<Type, Action<IEvent>> _eventHandlers = new();

    internal InstantEventHandlers()
    {
        InitializeEventHandlers();
    }

    private void InitializeEventHandlers()
    {
        _eventHandlers[typeof(DisconnectedEvent)] = e => OnDisconnected.Publish((DisconnectedEvent)e);

        _eventHandlers[typeof(GameEndedEvent)] = e => OnGameEnded.Publish((GameEndedEvent)e);
        _eventHandlers[typeof(GameAbortedEvent)] = e => OnGameAborted.Publish((GameAbortedEvent)e);
        _eventHandlers[typeof(RoundStartedEvent)] = e => OnRoundStarted.Publish((RoundStartedEvent)e);
        _eventHandlers[typeof(RoundEndedEvent)] = e => OnRoundEnded.Publish((RoundEndedEvent)e);
        
        _eventHandlers[typeof(DeathEvent)] = e => OnDeath.Publish((DeathEvent)e);
        _eventHandlers[typeof(HitBotEvent)] = e => OnHitBot.Publish((HitBotEvent)e);
        _eventHandlers[typeof(HitWallEvent)] = e => OnHitWall.Publish((HitWallEvent)e);
        _eventHandlers[typeof(BulletFiredEvent)] = e => OnBulletFired.Publish((BulletFiredEvent)e);

        _eventHandlers[typeof(NextTurnEvent)] = e => OnNextTurn.Publish((TickEvent)e);
    }

    internal void FireEvent(BotEvent botEvent) {
        if (_eventHandlers.TryGetValue(botEvent.GetType(), out var action))
        {
            action.Invoke(botEvent); // calls Publish(botEvent)
        }
        // ignore, if there is no registered event handler
    }
    
    // Virtual (fake) events:

    internal sealed class GameAbortedEvent : IEvent
    {
    }
    
    private sealed class NextTurnEvent : IEvent
    {
    }
}