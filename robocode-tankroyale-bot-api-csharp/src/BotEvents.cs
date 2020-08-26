using System;
using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi
{
  class BotEvents
  {
    readonly IBaseBot baseBot;

    // Event Managers
    public EventManager<ConnectedEvent> onConnectedManager = new EventManager<ConnectedEvent>();
    public EventManager<DisconnectedEvent> onDisconnectedManager = new EventManager<DisconnectedEvent>();
    public EventManager<ConnectionErrorEvent> onConnectionErrorManager = new EventManager<ConnectionErrorEvent>();
    public EventManager<GameStartedEvent> onGameStartedManager = new EventManager<GameStartedEvent>();
    public EventManager<GameEndedEvent> onGameEndedManager = new EventManager<GameEndedEvent>();
    public EventManager<TickEvent> onTickManager = new EventManager<TickEvent>();
    public EventManager<SkippedTurnEvent> onSkippedTurnManager = new EventManager<SkippedTurnEvent>();
    public EventManager<BotDeathEvent> onDeathManager = new EventManager<BotDeathEvent>();
    public EventManager<BotDeathEvent> onBotDeathManager = new EventManager<BotDeathEvent>();
    public EventManager<BotHitBotEvent> onHitBotManager = new EventManager<BotHitBotEvent>();
    public EventManager<BotHitWallEvent> onHitWallManager = new EventManager<BotHitWallEvent>();
    public EventManager<BulletFiredEvent> onBulletFiredManager = new EventManager<BulletFiredEvent>();
    public EventManager<BulletHitBotEvent> onHitByBulletManager = new EventManager<BulletHitBotEvent>();
    public EventManager<BulletHitBotEvent> onBulletHitManager = new EventManager<BulletHitBotEvent>();
    public EventManager<BulletHitBulletEvent> onBulletHitBulletManager = new EventManager<BulletHitBulletEvent>();
    public EventManager<BulletHitWallEvent> onBulletHitWallManager = new EventManager<BulletHitWallEvent>();
    public EventManager<ScannedBotEvent> onScannedBotManager = new EventManager<ScannedBotEvent>();
    public EventManager<WonRoundEvent> onWonRoundManager = new EventManager<WonRoundEvent>();
    public EventManager<Condition> onConditionManager = new EventManager<Condition>();

    // Events
    private event EventManager<ConnectedEvent>.Subscriber OnConnected;
    private event EventManager<DisconnectedEvent>.Subscriber OnDisconnected;
    private event EventManager<ConnectionErrorEvent>.Subscriber OnConnectionError;
    private event EventManager<GameStartedEvent>.Subscriber OnGameStarted;
    private event EventManager<GameEndedEvent>.Subscriber OnGameEnded;
    private event EventManager<TickEvent>.Subscriber OnTick;
    private event EventManager<SkippedTurnEvent>.Subscriber OnSkippedTurn;
    private event EventManager<BotDeathEvent>.Subscriber OnDeath;
    private event EventManager<BotDeathEvent>.Subscriber OnBotDeath;
    private event EventManager<BotHitBotEvent>.Subscriber OnHitBot;
    private event EventManager<BotHitWallEvent>.Subscriber OnHitWall;
    private event EventManager<BulletFiredEvent>.Subscriber OnBulletFired;
    private event EventManager<BulletHitBotEvent>.Subscriber OnHitByBullet;
    private event EventManager<BulletHitBotEvent>.Subscriber OnBulletHit;
    private event EventManager<BulletHitBulletEvent>.Subscriber OnBulletHitBullet;
    private event EventManager<BulletHitWallEvent>.Subscriber OnBulletHitWall;
    private event EventManager<ScannedBotEvent>.Subscriber OnScannedBot;
    private event EventManager<WonRoundEvent>.Subscriber OnWonRound;
    private event EventManager<Condition>.Subscriber OnCondition;

    internal BotEvents(IBaseBot baseBot)
    {
      this.baseBot = baseBot;
      init();
    }

    private void init()
    {
      onConnectedManager.Subscribe(baseBot.OnConnected);
      OnConnected += onConnectedManager.Publish;

      onDisconnectedManager.Subscribe(baseBot.OnDisconnected);
      OnDisconnected += onDisconnectedManager.Publish;

      onConnectionErrorManager.Subscribe(baseBot.OnConnectionError);
      OnConnectionError += onConnectionErrorManager.Publish;

      onGameStartedManager.Subscribe(baseBot.OnGameStarted);
      OnGameStarted += onGameStartedManager.Publish;

      onGameEndedManager.Subscribe(baseBot.OnGameEnded);
      OnGameEnded += onGameEndedManager.Publish;

      onTickManager.Subscribe(baseBot.OnTick);
      OnTick += onTickManager.Publish;

      onSkippedTurnManager.Subscribe(baseBot.OnSkippedTurn);
      OnSkippedTurn += onSkippedTurnManager.Publish;

      onDeathManager.Subscribe(baseBot.OnDeath);
      OnDeath += onDeathManager.Publish;

      onBotDeathManager.Subscribe(baseBot.OnBotDeath);
      OnBotDeath += onBotDeathManager.Publish;

      onHitBotManager.Subscribe(baseBot.OnHitBot);
      OnHitBot += onHitBotManager.Publish;

      onHitWallManager.Subscribe(baseBot.OnHitWall);
      OnHitWall += onHitWallManager.Publish;

      onBulletFiredManager.Subscribe(baseBot.OnBulletFired);
      OnBulletFired += onBulletFiredManager.Publish;

      onHitByBulletManager.Subscribe(baseBot.OnHitByBullet);
      OnHitByBullet += onHitByBulletManager.Publish;

      onBulletHitManager.Subscribe(baseBot.OnBulletHit);
      OnBulletHit += onBulletHitManager.Publish;

      onBulletHitBulletManager.Subscribe(baseBot.OnBulletHitBullet);
      OnBulletHitBullet += onBulletHitBulletManager.Publish;

      onBulletHitWallManager.Subscribe(baseBot.OnBulletHitWall);
      OnBulletHitWall += onBulletHitWallManager.Publish;

      onScannedBotManager.Subscribe(baseBot.OnScannedBot);
      OnScannedBot += onScannedBotManager.Publish;

      onWonRoundManager.Subscribe(baseBot.OnWonRound);
      OnWonRound += onWonRoundManager.Publish;

      onConditionManager.Subscribe(baseBot.OnCondition);
      OnCondition += onConditionManager.Publish;
    }

    public void FireConnectedEvent(ConnectedEvent evt)
    {
      OnConnected(evt);
    }

    public void FireDisconnectedEvent(DisconnectedEvent evt)
    {
      OnDisconnected(evt);
    }

    public void FireConnectionErrorEvent(ConnectionErrorEvent evt)
    {
      OnConnectionError(evt);
    }

    public void FireGameStartedEvent(GameStartedEvent evt)
    {
      OnGameStarted(evt);
    }

    public void FireGameEndedEvent(GameEndedEvent evt)
    {
      OnGameEnded(evt);
    }

    public void FireSkippedTurnEvent(SkippedTurnEvent evt)
    {
      OnSkippedTurn(evt);
    }

    public void FireTickEvent(TickEvent evt)
    {
      OnTick(evt);
    }

    public void FireEvents(TickEvent tickEvent)
    {
      foreach (var evt in tickEvent.Events)
      {
        switch (evt)
        {
          case BotDeathEvent botDeathEvent:
            if (botDeathEvent.VictimId == baseBot.MyId)
              OnDeath(botDeathEvent);
            else
              OnBotDeath(botDeathEvent);
            break;

          case BotHitBotEvent botHitBotEvent:
            OnHitBot(botHitBotEvent);
            break;

          case BotHitWallEvent botHitWallEvent:
            OnHitWall(botHitWallEvent);
            break;

          case BulletFiredEvent bulletFiredEvent:
            // Stop firing, when bullet has fired
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

    public void FireConditionMet(Condition condition)
    {
      OnCondition(condition);
    }
  }
}