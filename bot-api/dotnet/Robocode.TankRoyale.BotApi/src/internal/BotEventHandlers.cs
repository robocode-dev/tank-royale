using System;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
    internal sealed class BotEventHandlers
    {
        private readonly IBaseBot baseBot;

        // Regular bot event handlers
        internal readonly EventHandler<ConnectedEvent> onConnected = new EventHandler<ConnectedEvent>();
        internal readonly EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<DisconnectedEvent>();

        internal readonly EventHandler<ConnectionErrorEvent> onConnectionError =
            new EventHandler<ConnectionErrorEvent>();

        internal readonly EventHandler<GameStartedEvent> onGameStarted = new EventHandler<GameStartedEvent>();
        internal readonly EventHandler<GameEndedEvent> onGameEnded = new EventHandler<GameEndedEvent>();
        internal readonly EventHandler<object> onGameAborted = new EventHandler<object>();
        internal readonly EventHandler<RoundStartedEvent> onRoundStarted = new EventHandler<RoundStartedEvent>();
        internal readonly EventHandler<RoundEndedEvent> onRoundEnded = new EventHandler<RoundEndedEvent>();
        internal readonly EventHandler<TickEvent> onTick = new EventHandler<TickEvent>();
        internal readonly EventHandler<SkippedTurnEvent> onSkippedTurn = new EventHandler<SkippedTurnEvent>();
        internal readonly EventHandler<DeathEvent> onDeath = new EventHandler<DeathEvent>();
        internal readonly EventHandler<DeathEvent> onBotDeath = new EventHandler<DeathEvent>();
        internal readonly EventHandler<HitBotEvent> onHitBot = new EventHandler<HitBotEvent>();
        internal readonly EventHandler<HitWallEvent> onHitWall = new EventHandler<HitWallEvent>();
        internal readonly EventHandler<BulletFiredEvent> onBulletFired = new EventHandler<BulletFiredEvent>();
        internal readonly EventHandler<BulletHitBotEvent> onHitByBullet = new EventHandler<BulletHitBotEvent>();
        internal readonly EventHandler<BulletHitBotEvent> onBulletHit = new EventHandler<BulletHitBotEvent>();

        internal readonly EventHandler<BulletHitBulletEvent> onBulletHitBullet =
            new EventHandler<BulletHitBulletEvent>();

        internal readonly EventHandler<BulletHitWallEvent> onBulletHitWall = new EventHandler<BulletHitWallEvent>();
        internal readonly EventHandler<ScannedBotEvent> onScannedBot = new EventHandler<ScannedBotEvent>();
        internal readonly EventHandler<WonRoundEvent> onWonRound = new EventHandler<WonRoundEvent>();
        internal readonly EventHandler<CustomEvent> onCustomEvent = new EventHandler<CustomEvent>();

        internal readonly EventHandler<TickEvent> onNextTurn = new EventHandler<TickEvent>();

        // Events
        private event EventHandler<ConnectedEvent>.Subscriber OnConnected;
        private event EventHandler<DisconnectedEvent>.Subscriber OnDisconnected;
        private event EventHandler<ConnectionErrorEvent>.Subscriber OnConnectionError;
        private event EventHandler<GameStartedEvent>.Subscriber OnGameStarted;
        private event EventHandler<GameEndedEvent>.Subscriber OnGameEnded;
        private event EventHandler<object>.Subscriber OnGameAborted;
        private event EventHandler<RoundStartedEvent>.Subscriber OnRoundStarted;
        private event EventHandler<RoundEndedEvent>.Subscriber OnRoundEnded;
        private event EventHandler<TickEvent>.Subscriber OnTick;
        private event EventHandler<SkippedTurnEvent>.Subscriber OnSkippedTurn;
        private event EventHandler<DeathEvent>.Subscriber OnDeath;
        private event EventHandler<DeathEvent>.Subscriber OnBotDeath;
        private event EventHandler<HitBotEvent>.Subscriber OnHitBot;
        private event EventHandler<HitWallEvent>.Subscriber OnHitWall;
        private event EventHandler<BulletFiredEvent>.Subscriber OnBulletFired;
        private event EventHandler<BulletHitBotEvent>.Subscriber OnHitByBullet;
        private event EventHandler<BulletHitBotEvent>.Subscriber OnBulletHit;
        private event EventHandler<BulletHitBulletEvent>.Subscriber OnBulletHitBullet;
        private event EventHandler<BulletHitWallEvent>.Subscriber OnBulletHitWall;
        private event EventHandler<ScannedBotEvent>.Subscriber OnScannedBot;
        private event EventHandler<WonRoundEvent>.Subscriber OnWonRound;
        private event EventHandler<CustomEvent>.Subscriber OnCustomEvent;

        private event EventHandler<TickEvent>.Subscriber OnNextTurn;

        internal BotEventHandlers(IBaseBot baseBot)
        {
            this.baseBot = baseBot;

            // Regular handlers
            OnConnected += onConnected.Publish;
            OnDisconnected += onDisconnected.Publish;
            OnConnectionError += onConnectionError.Publish;
            OnGameStarted += onGameStarted.Publish;
            OnGameEnded += onGameEnded.Publish;
            OnGameAborted += onGameAborted.Publish;
            OnRoundStarted += onRoundStarted.Publish;
            OnRoundEnded += onRoundEnded.Publish;
            OnTick += onTick.Publish;
            OnSkippedTurn += onSkippedTurn.Publish;
            OnDeath += onDeath.Publish;
            OnBotDeath += onBotDeath.Publish;
            OnHitBot += onHitBot.Publish;
            OnHitWall += onHitWall.Publish;
            OnBulletFired += onBulletFired.Publish;
            OnHitByBullet += onHitByBullet.Publish;
            OnBulletHit += onBulletHit.Publish;
            OnBulletHitBullet += onBulletHitBullet.Publish;
            OnBulletHitWall += onBulletHitWall.Publish;
            OnScannedBot += onScannedBot.Publish;
            OnWonRound += onWonRound.Publish;
            OnCustomEvent += onCustomEvent.Publish;

            OnNextTurn += onNextTurn.Publish;

            // Subscribe to bot events
            onConnected.Subscribe(baseBot.OnConnected);
            onDisconnected.Subscribe(baseBot.OnDisconnected);
            onConnectionError.Subscribe(baseBot.OnConnectionError);
            onGameStarted.Subscribe(baseBot.OnGameStarted);
            onGameEnded.Subscribe(baseBot.OnGameEnded);
            onRoundStarted.Subscribe(baseBot.OnRoundStarted);
            onRoundEnded.Subscribe(baseBot.OnRoundEnded);
            onTick.Subscribe(baseBot.OnTick);
            onSkippedTurn.Subscribe(baseBot.OnSkippedTurn);
            onDeath.Subscribe(baseBot.OnDeath);
            onBotDeath.Subscribe(baseBot.OnBotDeath);
            onHitBot.Subscribe(baseBot.OnHitBot);
            onHitWall.Subscribe(baseBot.OnHitWall);
            onBulletFired.Subscribe(baseBot.OnBulletFired);
            onHitByBullet.Subscribe(baseBot.OnHitByBullet);
            onBulletHit.Subscribe(baseBot.OnBulletHit);
            onBulletHitBullet.Subscribe(baseBot.OnBulletHitBullet);
            onBulletHitWall.Subscribe(baseBot.OnBulletHitWall);
            onScannedBot.Subscribe(baseBot.OnScannedBot);
            onWonRound.Subscribe(baseBot.OnWonRound);
            onCustomEvent.Subscribe(baseBot.OnCustomEvent);
        }

        internal void FireConnectedEvent(ConnectedEvent evt)
        {
            OnConnected?.Invoke(evt);
        }

        internal void FireDisconnectedEvent(DisconnectedEvent evt)
        {
            OnDisconnected?.Invoke(evt);
        }

        internal void FireConnectionErrorEvent(ConnectionErrorEvent evt)
        {
            OnConnectionError?.Invoke(evt);
        }

        internal void FireGameStartedEvent(GameStartedEvent evt)
        {
            OnGameStarted?.Invoke(evt);
        }

        internal void FireGameEndedEvent(GameEndedEvent evt)
        {
            OnGameEnded?.Invoke(evt);
        }

        internal void FireGameAbortedEvent()
        {
            OnGameAborted?.Invoke(null);
        }

        internal void FireRoundStartedEvent(RoundStartedEvent evt)
        {
            OnRoundStarted?.Invoke(evt);
        }

        internal void FireRoundEndedEvent(RoundEndedEvent evt)
        {
            OnRoundEnded?.Invoke(evt);
        }

        internal void FireSkippedTurnEvent(SkippedTurnEvent evt)
        {
            OnSkippedTurn?.Invoke(evt);
        }

        internal void FireNextTurn(TickEvent evt)
        {
            OnNextTurn?.Invoke(evt);
        }


        internal void Fire(BotEvent evt)
        {
            switch (evt)
            {
                case TickEvent tickEvent:
                    OnTick?.Invoke(tickEvent);
                    break;
                case ScannedBotEvent scannedBotEvent:
                    OnScannedBot?.Invoke(scannedBotEvent);
                    break;
                case SkippedTurnEvent skippedTurnEvent:
                    OnSkippedTurn?.Invoke(skippedTurnEvent);
                    break;
                case HitBotEvent botHitBotEvent:
                    OnHitBot?.Invoke(botHitBotEvent);
                    break;
                case HitWallEvent botHitWallEvent:
                    OnHitWall?.Invoke(botHitWallEvent);
                    break;
                case BulletFiredEvent bulletFiredEvent:
                    OnBulletFired?.Invoke(bulletFiredEvent);
                    break;
                case BulletHitWallEvent bulletHitWallEvent:
                    OnBulletHitWall?.Invoke(bulletHitWallEvent);
                    break;
                case BulletHitBotEvent bulletHitBotEvent:
                    if (bulletHitBotEvent.VictimId == baseBot.MyId)
                        OnHitByBullet?.Invoke(bulletHitBotEvent);
                    else
                        OnBulletHit?.Invoke(bulletHitBotEvent);
                    break;
                case DeathEvent botDeathEvent:
                    if (botDeathEvent.VictimId == baseBot.MyId)
                        OnDeath?.Invoke(botDeathEvent);
                    else
                        OnBotDeath?.Invoke(botDeathEvent);
                    break;
                case BulletHitBulletEvent bulletHitBulletEvent:
                    OnBulletHitBullet?.Invoke(bulletHitBulletEvent);
                    break;
                case WonRoundEvent wonRoundEvent:
                    OnWonRound?.Invoke(wonRoundEvent);
                    break;
                case CustomEvent customEvent:
                    OnCustomEvent?.Invoke(customEvent);
                    break;
                default:
                    throw new Exception("Unhandled event: " + evt);
            }
        }
    }
}