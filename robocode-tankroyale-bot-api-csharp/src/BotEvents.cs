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

    // Events
    private event EventManager<ConnectedEvent>.EventHandler OnConnected;
    private event EventManager<DisconnectedEvent>.EventHandler OnDisconnected;
    private event EventManager<ConnectionErrorEvent>.EventHandler OnConnectionError;
    private event EventManager<GameStartedEvent>.EventHandler OnGameStarted;
    private event EventManager<GameEndedEvent>.EventHandler OnGameEnded;
    private event EventManager<TickEvent>.EventHandler OnTick;
    private event EventManager<SkippedTurnEvent>.EventHandler OnSkippedTurn;
    private event EventManager<BotDeathEvent>.EventHandler OnDeath;
    private event EventManager<BotDeathEvent>.EventHandler OnBotDeath;
    private event EventManager<BotHitBotEvent>.EventHandler OnHitBot;
    private event EventManager<BotHitWallEvent>.EventHandler OnHitWall;
    private event EventManager<BulletFiredEvent>.EventHandler OnBulletFired;
    private event EventManager<BulletHitBotEvent>.EventHandler OnHitByBullet;
    private event EventManager<BulletHitBotEvent>.EventHandler OnBulletHit;
    private event EventManager<BulletHitBulletEvent>.EventHandler OnBulletHitBullet;
    private event EventManager<BulletHitWallEvent>.EventHandler OnBulletHitWall;
    private event EventManager<ScannedBotEvent>.EventHandler OnScannedBot;
    private event EventManager<WonRoundEvent>.EventHandler OnWonRound;

    internal BotEvents(IBaseBot baseBot)
    {
      this.baseBot = baseBot;

      init();
    }

    private void init()
    {
      onConnectedManager.Add(baseBot.OnConnected);
      OnConnected += onConnectedManager.InvokeAll;

      onDisconnectedManager.Add(baseBot.OnDisconnected);
      OnDisconnected += onDisconnectedManager.InvokeAll;

      onConnectionErrorManager.Add(baseBot.OnConnectionError);
      OnConnectionError += onConnectionErrorManager.InvokeAll;

      onGameStartedManager.Add(baseBot.OnGameStarted);
      OnGameStarted += onGameStartedManager.InvokeAll;

      onGameEndedManager.Add(baseBot.OnGameEnded);
      OnGameEnded += onGameEndedManager.InvokeAll;

      onTickManager.Add(baseBot.OnTick);
      OnTick += onTickManager.InvokeAll;

      onSkippedTurnManager.Add(baseBot.OnSkippedTurn);
      OnSkippedTurn += onSkippedTurnManager.InvokeAll;

      onDeathManager.Add(baseBot.OnDeath);
      OnDeath += onDeathManager.InvokeAll;

      onBotDeathManager.Add(baseBot.OnBotDeath);
      OnBotDeath += onBotDeathManager.InvokeAll;

      onHitBotManager.Add(baseBot.OnHitBot);
      OnHitBot += onHitBotManager.InvokeAll;

      onHitWallManager.Add(baseBot.OnHitWall);
      OnHitWall += onHitWallManager.InvokeAll;

      onBulletFiredManager.Add(baseBot.OnBulletFired);
      OnBulletFired += onBulletFiredManager.InvokeAll;

      onHitByBulletManager.Add(baseBot.OnHitByBullet);
      OnHitByBullet += onHitByBulletManager.InvokeAll;

      onBulletHitManager.Add(baseBot.OnBulletHit);
      OnBulletHit += onBulletHitManager.InvokeAll;

      onBulletHitBulletManager.Add(baseBot.OnBulletHitBullet);
      OnBulletHitBullet += onBulletHitBulletManager.InvokeAll;

      onBulletHitWallManager.Add(baseBot.OnBulletHitWall);
      OnBulletHitWall += onBulletHitWallManager.InvokeAll;

      onScannedBotManager.Add(baseBot.OnScannedBot);
      OnScannedBot += onScannedBotManager.InvokeAll;

      onWonRoundManager.Add(baseBot.OnWonRound);
      OnWonRound += onWonRoundManager.InvokeAll;
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

    public void DispatchEvents(TickEvent tickEvent)
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
            baseBot.Firepower = 0d;
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
}