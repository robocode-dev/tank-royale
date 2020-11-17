using System;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal sealed class BotEventHandlers
  {
    readonly IBaseBot baseBot;

    // Regular bot event handlers
    internal readonly EventHandler<ConnectedEvent> onConnected = new EventHandler<ConnectedEvent>();
    internal readonly EventHandler<DisconnectedEvent> onDisconnected = new EventHandler<DisconnectedEvent>();
    internal readonly EventHandler<ConnectionErrorEvent> onConnectionError = new EventHandler<ConnectionErrorEvent>();
    internal readonly EventHandler<GameStartedEvent> onGameStarted = new EventHandler<GameStartedEvent>();
    internal readonly EventHandler<GameEndedEvent> onGameEnded = new EventHandler<GameEndedEvent>();
    internal readonly EventHandler<TickEvent> onTick = new EventHandler<TickEvent>();
    internal readonly EventHandler<SkippedTurnEvent> onSkippedTurn = new EventHandler<SkippedTurnEvent>();
    internal readonly EventHandler<DeathEvent> onDeath = new EventHandler<DeathEvent>();
    internal readonly EventHandler<DeathEvent> onBotDeath = new EventHandler<DeathEvent>();
    internal readonly EventHandler<HitBotEvent> onHitBot = new EventHandler<HitBotEvent>();
    internal readonly EventHandler<HitWallEvent> onHitWall = new EventHandler<HitWallEvent>();
    internal readonly EventHandler<BulletFiredEvent> onBulletFired = new EventHandler<BulletFiredEvent>();
    internal readonly EventHandler<BulletHitBotEvent> onHitByBullet = new EventHandler<BulletHitBotEvent>();
    internal readonly EventHandler<BulletHitBotEvent> onBulletHit = new EventHandler<BulletHitBotEvent>();
    internal readonly EventHandler<BulletHitBulletEvent> onBulletHitBullet = new EventHandler<BulletHitBulletEvent>();
    internal readonly EventHandler<BulletHitWallEvent> onBulletHitWall = new EventHandler<BulletHitWallEvent>();
    internal readonly EventHandler<ScannedBotEvent> onScannedBot = new EventHandler<ScannedBotEvent>();
    internal readonly EventHandler<WonRoundEvent> onWonRound = new EventHandler<WonRoundEvent>();
    internal readonly EventHandler<CustomEvent> onCustomEvent = new EventHandler<CustomEvent>();

    // Convenient event handlers
    internal readonly EventHandler<TickEvent> onProcessTurn = new EventHandler<TickEvent>();
    internal readonly EventHandler<TickEvent> onNewRound = new EventHandler<TickEvent>();

    // Events
    private event EventHandler<ConnectedEvent>.Subscriber OnConnected;
    private event EventHandler<DisconnectedEvent>.Subscriber OnDisconnected;
    private event EventHandler<ConnectionErrorEvent>.Subscriber OnConnectionError;
    private event EventHandler<GameStartedEvent>.Subscriber OnGameStarted;
    private event EventHandler<GameEndedEvent>.Subscriber OnGameEnded;
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

    private event EventHandler<TickEvent>.Subscriber OnProcessTurn;
    private event EventHandler<TickEvent>.Subscriber OnNewRound;

    internal BotEventHandlers(IBaseBot baseBot)
    {
      this.baseBot = baseBot;

      onConnected.Subscribe(baseBot.OnConnected);
      OnConnected += onConnected.Publish;

      onDisconnected.Subscribe(baseBot.OnDisconnected);
      OnDisconnected += onDisconnected.Publish;

      onConnectionError.Subscribe(baseBot.OnConnectionError);
      OnConnectionError += onConnectionError.Publish;

      onGameStarted.Subscribe(baseBot.OnGameStarted);
      OnGameStarted += onGameStarted.Publish;

      onGameEnded.Subscribe(baseBot.OnGameEnded);
      OnGameEnded += onGameEnded.Publish;

      onTick.Subscribe(baseBot.OnTick);
      OnTick += onTick.Publish;

      onSkippedTurn.Subscribe(baseBot.OnSkippedTurn);
      OnSkippedTurn += onSkippedTurn.Publish;

      onDeath.Subscribe(baseBot.OnDeath);
      OnDeath += onDeath.Publish;

      onBotDeath.Subscribe(baseBot.OnBotDeath);
      OnBotDeath += onBotDeath.Publish;

      onHitBot.Subscribe(baseBot.OnHitBot);
      OnHitBot += onHitBot.Publish;

      onHitWall.Subscribe(baseBot.OnHitWall);
      OnHitWall += onHitWall.Publish;

      onBulletFired.Subscribe(baseBot.OnBulletFired);
      OnBulletFired += onBulletFired.Publish;

      onHitByBullet.Subscribe(baseBot.OnHitByBullet);
      OnHitByBullet += onHitByBullet.Publish;

      onBulletHit.Subscribe(baseBot.OnBulletHit);
      OnBulletHit += onBulletHit.Publish;

      onBulletHitBullet.Subscribe(baseBot.OnBulletHitBullet);
      OnBulletHitBullet += onBulletHitBullet.Publish;

      onBulletHitWall.Subscribe(baseBot.OnBulletHitWall);
      OnBulletHitWall += onBulletHitWall.Publish;

      onScannedBot.Subscribe(baseBot.OnScannedBot);
      OnScannedBot += onScannedBot.Publish;

      onWonRound.Subscribe(baseBot.OnWonRound);
      OnWonRound += onWonRound.Publish;

      onCustomEvent.Subscribe(baseBot.OnCustomEvent);
      OnCustomEvent += onCustomEvent.Publish;
    }

    internal void FireConnectedEvent(ConnectedEvent evt)
    {
      OnConnected(evt);
    }

    internal void FireDisconnectedEvent(DisconnectedEvent evt)
    {
      OnDisconnected(evt);
    }

    internal void FireConnectionErrorEvent(ConnectionErrorEvent evt)
    {
      OnConnectionError(evt);
    }

    internal void FireGameStartedEvent(GameStartedEvent evt)
    {
      OnGameStarted(evt);
    }

    internal void FireGameEndedEvent(GameEndedEvent evt)
    {
      OnGameEnded(evt);
    }

    internal void FireSkippedTurnEvent(SkippedTurnEvent evt)
    {
      OnSkippedTurn(evt);
    }

    internal void FireTickEvent(TickEvent evt)
    {
      OnTick(evt);
    }

    internal void FireProcessTurn(TickEvent evt)
    {
      OnProcessTurn(evt);
    }

    internal void FireNewRound(TickEvent evt)
    {
      OnNewRound(evt);
    }

    internal void Fire(BotEvent evt)
    {
      switch (evt)
      {
        case DeathEvent botDeathEvent:
          if (botDeathEvent.VictimId == baseBot.MyId)
            OnDeath(botDeathEvent);
          else
            OnBotDeath(botDeathEvent);
          break;

        case HitBotEvent botHitBotEvent:
          OnHitBot(botHitBotEvent);
          break;

        case HitWallEvent botHitWallEvent:
          OnHitWall(botHitWallEvent);
          break;

        case BulletFiredEvent bulletFiredEvent:
          OnBulletFired(bulletFiredEvent);
          break;

        case BulletHitBotEvent bulletHitBotEvent:
          if (bulletHitBotEvent.VictimId == baseBot.MyId)
            OnHitByBullet(bulletHitBotEvent);
          else
            OnBulletHit(bulletHitBotEvent);
          break;

        case BulletHitBulletEvent bulletHitBulletEvent:
          OnBulletHitBullet(bulletHitBulletEvent);
          break;

        case BulletHitWallEvent bulletHitWallEvent:
          OnBulletHitWall(bulletHitWallEvent);
          break;

        case ScannedBotEvent scannedBotEvent:
          OnScannedBot(scannedBotEvent);
          break;

        case SkippedTurnEvent skippedTurnEvent:
          OnSkippedTurn(skippedTurnEvent);
          break;

        case WonRoundEvent wonRoundEvent:
          OnWonRound(wonRoundEvent);
          break;

        default:
          Console.Error.WriteLine("Unhandled event: " + evt);
          break;
      }
    }
  }
}