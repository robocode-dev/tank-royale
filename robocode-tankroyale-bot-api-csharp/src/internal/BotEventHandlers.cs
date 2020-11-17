using System;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal class BotEventHandlers
  {
    readonly IBaseBot baseBot;

    // Regular bot event handlers
    internal EventHandler<ConnectedEvent> onConnectedHandler = new EventHandler<ConnectedEvent>();
    internal EventHandler<DisconnectedEvent> onDisconnectedHandler = new EventHandler<DisconnectedEvent>();
    internal EventHandler<ConnectionErrorEvent> onConnectionErrorHandler = new EventHandler<ConnectionErrorEvent>();
    internal EventHandler<GameStartedEvent> onGameStartedHandler = new EventHandler<GameStartedEvent>();
    internal EventHandler<GameEndedEvent> onGameEndedHandler = new EventHandler<GameEndedEvent>();
    internal EventHandler<TickEvent> onTickHandler = new EventHandler<TickEvent>();
    internal EventHandler<SkippedTurnEvent> onSkippedTurnHandler = new EventHandler<SkippedTurnEvent>();
    internal EventHandler<DeathEvent> onDeathHandler = new EventHandler<DeathEvent>();
    internal EventHandler<DeathEvent> onBotDeathHandler = new EventHandler<DeathEvent>();
    internal EventHandler<HitBotEvent> onHitBotHandler = new EventHandler<HitBotEvent>();
    internal EventHandler<HitWallEvent> onHitWallHandler = new EventHandler<HitWallEvent>();
    internal EventHandler<BulletFiredEvent> onBulletFiredHandler = new EventHandler<BulletFiredEvent>();
    internal EventHandler<BulletHitBotEvent> onHitByBulletHandler = new EventHandler<BulletHitBotEvent>();
    internal EventHandler<BulletHitBotEvent> onBulletHitHandler = new EventHandler<BulletHitBotEvent>();
    internal EventHandler<BulletHitBulletEvent> onBulletHitBulletHandler = new EventHandler<BulletHitBulletEvent>();
    internal EventHandler<BulletHitWallEvent> onBulletHitWallHandler = new EventHandler<BulletHitWallEvent>();
    internal EventHandler<ScannedBotEvent> onScannedBotHandler = new EventHandler<ScannedBotEvent>();
    internal EventHandler<WonRoundEvent> onWonRoundHandler = new EventHandler<WonRoundEvent>();
    internal EventHandler<CustomEvent> onCustomEventHandler = new EventHandler<CustomEvent>();

    // Convenient event handlers
    internal EventHandler<TickEvent> onProcessTurnHandler = new EventHandler<TickEvent>();
    internal EventHandler<TickEvent> onNewRoundHandler = new EventHandler<TickEvent>();

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

      onConnectedHandler.Subscribe(baseBot.OnConnected);
      OnConnected += onConnectedHandler.Publish;

      onDisconnectedHandler.Subscribe(baseBot.OnDisconnected);
      OnDisconnected += onDisconnectedHandler.Publish;

      onConnectionErrorHandler.Subscribe(baseBot.OnConnectionError);
      OnConnectionError += onConnectionErrorHandler.Publish;

      onGameStartedHandler.Subscribe(baseBot.OnGameStarted);
      OnGameStarted += onGameStartedHandler.Publish;

      onGameEndedHandler.Subscribe(baseBot.OnGameEnded);
      OnGameEnded += onGameEndedHandler.Publish;

      onTickHandler.Subscribe(baseBot.OnTick);
      OnTick += onTickHandler.Publish;

      onSkippedTurnHandler.Subscribe(baseBot.OnSkippedTurn);
      OnSkippedTurn += onSkippedTurnHandler.Publish;

      onDeathHandler.Subscribe(baseBot.OnDeath);
      OnDeath += onDeathHandler.Publish;

      onBotDeathHandler.Subscribe(baseBot.OnBotDeath);
      OnBotDeath += onBotDeathHandler.Publish;

      onHitBotHandler.Subscribe(baseBot.OnHitBot);
      OnHitBot += onHitBotHandler.Publish;

      onHitWallHandler.Subscribe(baseBot.OnHitWall);
      OnHitWall += onHitWallHandler.Publish;

      onBulletFiredHandler.Subscribe(baseBot.OnBulletFired);
      OnBulletFired += onBulletFiredHandler.Publish;

      onHitByBulletHandler.Subscribe(baseBot.OnHitByBullet);
      OnHitByBullet += onHitByBulletHandler.Publish;

      onBulletHitHandler.Subscribe(baseBot.OnBulletHit);
      OnBulletHit += onBulletHitHandler.Publish;

      onBulletHitBulletHandler.Subscribe(baseBot.OnBulletHitBullet);
      OnBulletHitBullet += onBulletHitBulletHandler.Publish;

      onBulletHitWallHandler.Subscribe(baseBot.OnBulletHitWall);
      OnBulletHitWall += onBulletHitWallHandler.Publish;

      onScannedBotHandler.Subscribe(baseBot.OnScannedBot);
      OnScannedBot += onScannedBotHandler.Publish;

      onWonRoundHandler.Subscribe(baseBot.OnWonRound);
      OnWonRound += onWonRoundHandler.Publish;

      onCustomEventHandler.Subscribe(baseBot.OnCustomEvent);
      OnCustomEvent += onCustomEventHandler.Publish;
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