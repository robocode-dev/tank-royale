using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi
{
  public partial class BaseBot
  {
    internal class BaseBotInternals
    {
      private const string NotConnectedToServerMsg =
        "Not connected to game server yes. Make sure OnConnected() event handler has been called first";

      private const string GameNotRunningMsg =
        "Game is not running. Make sure OnGameStarted() event handler has been called first";

      private const string TickNotAvailableMsg =
        "Game is not running or tick has not occurred yet. Make sure OnTick() event handler has been called first";

      private IBaseBot parent;

      private BotInfo botInfo;

      private BotIntent botIntent = new BotIntent();

      // Server connection
      private WebSocketClient socket;
      private ServerHandshake serverHandshake = null;

      internal EventWaitHandle exitEvent = new ManualResetEvent(false);

      // Current game states:
      private int? myId = null;
      private GameSetup gameSetup = null;
      private TickEvent currentTurn = null;
      private long? ticksStart = DateTime.Now.Ticks;

      // Adjustment of turn rates
      internal bool isAdjustGunForBodyTurn;
      internal bool isAdjustRadarForGunTurn;

      // Event Managers
      internal EventManager<ConnectedEvent> onConnectedManager = new EventManager<ConnectedEvent>();
      internal EventManager<DisconnectedEvent> onDisconnectedManager = new EventManager<DisconnectedEvent>();
      internal EventManager<ConnectionErrorEvent> onConnectionErrorManager = new EventManager<ConnectionErrorEvent>();
      internal EventManager<GameStartedEvent> onGameStartedManager = new EventManager<GameStartedEvent>();
      internal EventManager<GameEndedEvent> onGameEndedManager = new EventManager<GameEndedEvent>();
      internal EventManager<TickEvent> onTickManager = new EventManager<TickEvent>();
      internal EventManager<SkippedTurnEvent> onSkippedTurnManager = new EventManager<SkippedTurnEvent>();
      internal EventManager<BotDeathEvent> onDeathManager = new EventManager<BotDeathEvent>();
      internal EventManager<BotDeathEvent> onBotDeathManager = new EventManager<BotDeathEvent>();
      internal EventManager<BotHitBotEvent> onHitBotManager = new EventManager<BotHitBotEvent>();
      internal EventManager<BotHitWallEvent> onHitWallManager = new EventManager<BotHitWallEvent>();
      internal EventManager<BulletFiredEvent> onBulletFiredManager = new EventManager<BulletFiredEvent>();
      internal EventManager<BulletHitBotEvent> onHitByBulletManager = new EventManager<BulletHitBotEvent>();
      internal EventManager<BulletHitBotEvent> onBulletHitManager = new EventManager<BulletHitBotEvent>();
      internal EventManager<BulletHitBulletEvent> onBulletHitBulletManager = new EventManager<BulletHitBulletEvent>();
      internal EventManager<BulletHitWallEvent> onBulletHitWallManager = new EventManager<BulletHitWallEvent>();
      internal EventManager<ScannedBotEvent> onScannedBotManager = new EventManager<ScannedBotEvent>();
      internal EventManager<WonRoundEvent> onWonRoundManager = new EventManager<WonRoundEvent>();

      // Events
      internal event EventManager<ConnectedEvent>.EventHandler OnConnected;
      internal event EventManager<DisconnectedEvent>.EventHandler OnDisconnected;
      internal event EventManager<ConnectionErrorEvent>.EventHandler OnConnectionError;
      internal event EventManager<GameStartedEvent>.EventHandler OnGameStarted;
      internal event EventManager<GameEndedEvent>.EventHandler OnGameEnded;
      internal event EventManager<TickEvent>.EventHandler OnTick;
      internal event EventManager<SkippedTurnEvent>.EventHandler OnSkippedTurn;
      internal event EventManager<BotDeathEvent>.EventHandler OnDeath;
      internal event EventManager<BotDeathEvent>.EventHandler OnBotDeath;
      internal event EventManager<BotHitBotEvent>.EventHandler OnHitBot;
      internal event EventManager<BotHitWallEvent>.EventHandler OnHitWall;
      internal event EventManager<BulletFiredEvent>.EventHandler OnBulletFired;
      internal event EventManager<BulletHitBotEvent>.EventHandler OnHitByBullet;
      internal event EventManager<BulletHitBotEvent>.EventHandler OnBulletHit;
      internal event EventManager<BulletHitBulletEvent>.EventHandler OnBulletHitBullet;
      internal event EventManager<BulletHitWallEvent>.EventHandler OnBulletHitWall;
      internal event EventManager<ScannedBotEvent>.EventHandler OnScannedBot;
      internal event EventManager<WonRoundEvent>.EventHandler OnWonRound;

      internal BaseBotInternals(IBaseBot parent, BotInfo botInfo, Uri serverUri)
      {
        this.parent = parent;
        this.botInfo = (botInfo == null) ? EnvVars.GetBotInfo() : botInfo;

        Init(serverUri);
      }

      private void Init(Uri serverUri)
      {
        socket = new WebSocketClient((serverUri == null) ? ServerUriFromSetting : serverUri);

        botIntent.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotIntent); // must be set

        onConnectedManager.Add(parent.OnConnected);
        OnConnected += onConnectedManager.InvokeAll;

        onDisconnectedManager.Add(parent.OnDisconnected);
        OnDisconnected += onDisconnectedManager.InvokeAll;

        onConnectionErrorManager.Add(parent.OnConnectionError);
        OnConnectionError += onConnectionErrorManager.InvokeAll;

        onGameStartedManager.Add(parent.OnGameStarted);
        OnGameStarted += onGameStartedManager.InvokeAll;

        onGameEndedManager.Add(parent.OnGameEnded);
        OnGameEnded += onGameEndedManager.InvokeAll;

        onTickManager.Add(parent.OnTick);
        OnTick += onTickManager.InvokeAll;

        onSkippedTurnManager.Add(parent.OnSkippedTurn);
        OnSkippedTurn += onSkippedTurnManager.InvokeAll;

        onDeathManager.Add(parent.OnDeath);
        OnDeath += onDeathManager.InvokeAll;

        onBotDeathManager.Add(parent.OnBotDeath);
        OnBotDeath += onBotDeathManager.InvokeAll;

        onHitBotManager.Add(parent.OnHitBot);
        OnHitBot += onHitBotManager.InvokeAll;

        onHitWallManager.Add(parent.OnHitWall);
        OnHitWall += onHitWallManager.InvokeAll;

        onBulletFiredManager.Add(parent.OnBulletFired);
        OnBulletFired += onBulletFiredManager.InvokeAll;

        onHitByBulletManager.Add(parent.OnHitByBullet);
        OnHitByBullet += onHitByBulletManager.InvokeAll;

        onBulletHitManager.Add(parent.OnBulletHit);
        OnBulletHit += onBulletHitManager.InvokeAll;

        onBulletHitBulletManager.Add(parent.OnBulletHitBullet);
        OnBulletHitBullet += onBulletHitBulletManager.InvokeAll;

        onBulletHitWallManager.Add(parent.OnBulletHitWall);
        OnBulletHitWall += onBulletHitWallManager.InvokeAll;

        onScannedBotManager.Add(parent.OnScannedBot);
        OnScannedBot += onScannedBotManager.InvokeAll;

        onWonRoundManager.Add(parent.OnWonRound);
        OnWonRound += onWonRoundManager.InvokeAll;
      }

      internal void Connect()
      {
        try
        {
          socket.OnConnected += new WebSocketClient.OnConnectedHandler(HandleConnected);
          socket.OnDisconnected += new WebSocketClient.OnDisconnectedHandler(HandleDisconnected);
          socket.OnError += new WebSocketClient.OnErrorHandler(HandleConnectionError);
          socket.OnTextMessage += new WebSocketClient.OnTextMessageHandler(HandleTextMessage);
          socket.Connect();
        }
        catch (Exception ex)
        {
          throw new BotException("Could not connect to web socket", ex);
        }
      }

      internal void Disconnect()
      {
        try
        {
          socket.Disconnect();
        }
        catch (Exception ex)
        {
          throw new BotException("Could not disconnect from web socket", ex);
        }
      }

      private void ClearCurrentGameState()
      {
        // Clear setting that are only available during a running game
        currentTurn = null;
        gameSetup = null;
        myId = null;
      }

      internal void SendIntent()
      {
        try
        {
          socket.SendTextMessage(JsonConvert.SerializeObject(botIntent));
        }
        catch (Exception ex)
        {
          throw new BotException("Could not send message", ex);
        }
      }

      private Uri ServerUriFromSetting
      {
        get
        {
          var uri = EnvVars.GetServerUrl();
          if (uri == null)
          {
            uri = "ws://localhost";
          }
          if (!Uri.IsWellFormedUriString(uri, UriKind.Absolute))
          {
            throw new BotException("Incorrect syntax for server uri: " + uri);
          }
          return new Uri(uri);
        }
      }

      internal ServerHandshake ServerHandshake
      {
        get
        {
          if (serverHandshake == null)
          {
            throw new BotException(NotConnectedToServerMsg);
          }
          return serverHandshake;
        }
      }

      internal int MyId
      {
        get
        {
          if (myId == null)
          {
            throw new BotException(GameNotRunningMsg);
          }
          return (int)myId;
        }
      }

      internal GameSetup GameSetup
      {
        get
        {
          if (gameSetup == null)
          {
            throw new BotException(GameNotRunningMsg);
          }
          return gameSetup;
        }
      }

      internal BotIntent BotIntent
      {
        get
        {
          if (botIntent == null)
          {
            throw new BotException(GameNotRunningMsg);
          }
          return botIntent;
        }
      }

      internal TickEvent CurrentTurn
      {
        get
        {
          if (currentTurn == null)
          {
            throw new BotException(TickNotAvailableMsg);
          }
          return currentTurn;
        }
      }

      internal long TicksStart
      {
        get
        {
          if (ticksStart == null)
          {
            throw new BotException(TickNotAvailableMsg);
          }
          return (long)ticksStart;
        }
      }

      private void HandleConnected()
      {
        OnConnected(new ConnectedEvent(socket.ServerUri));
      }

      private void HandleDisconnected(bool remote)
      {
        OnDisconnected(new DisconnectedEvent(socket.ServerUri, remote));
      }

      private void HandleConnectionError(Exception cause)
      {
        OnConnectionError(new ConnectionErrorEvent(socket.ServerUri, cause));
      }

      private void HandleTextMessage(string json)
      {
        var jsonMsg = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
        try
        {
          var type = (string)jsonMsg["$type"];

          if (!string.IsNullOrWhiteSpace(type))
          {
            var msgType = (MessageType)Enum.Parse(typeof(MessageType), type);
            switch (msgType)
            {
              case MessageType.TickEventForBot:
                HandleTickEvent(json);
                break;
              case MessageType.ServerHandshake:
                HandleServerHandshake(json);
                break;
              case MessageType.GameStartedEventForBot:
                HandleGameStartedEvent(json);
                break;
              case MessageType.GameEndedEventForBot:
                HandleGameEndedEvent(json);
                break;
              case MessageType.SkippedTurnEvent:
                HandleSkippedTurnEvent(json);
                break;
              default:
                throw new BotException("Unsupported WebSocket message type: " + type);
            }
          }
        }
        catch (KeyNotFoundException)
        {
          throw new BotException($"$type is missing on the JSON message: {string.Join(Environment.NewLine, jsonMsg)}");
        }
      }

      private void HandleServerHandshake(string json)
      {
        var serverHandshake = JsonConvert.DeserializeObject<ServerHandshake>(json);

        // Reply by sending bot handshake
        var botHandshake = BotHandshakeFactory.Create(botInfo);
        botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotHandshake);
        var text = JsonConvert.SerializeObject(botHandshake);

        socket.SendTextMessage(text);
      }

      private void HandleGameStartedEvent(string json)
      {
        var gameStartedEventForBot = JsonConvert.DeserializeObject<GameStartedEventForBot>(json);

        myId = gameStartedEventForBot.MyId;
        gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

        // Send ready signal
        BotReady ready = new BotReady();
        ready.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotReady);

        var text = JsonConvert.SerializeObject(ready);
        socket.SendTextMessage(text);

        var gameStartedEvent = new GameStartedEvent((int)myId, gameSetup);
        OnGameStarted(gameStartedEvent);
      }

      private void HandleGameEndedEvent(string json)
      {
        // Clear current game state
        ClearCurrentGameState();

        // Send the game ended event
        var gameEndedEventForBot = JsonConvert.DeserializeObject<GameEndedEventForBot>(json);
        var results = ResultsMapper.Map(gameEndedEventForBot.Results);

        var gameEndedEvent = new GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results);
        OnGameEnded(gameEndedEvent);
      }

      public void HandleSkippedTurnEvent(string json)
      {
        var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.SkippedTurnEvent>(json);
        OnSkippedTurn(EventMapper.Map(skippedTurnEvent));
      }

      private void HandleTickEvent(string json)
      {
        var tickEventForBot = JsonConvert.DeserializeObject<Schema.TickEventForBot>(json);
        currentTurn = EventMapper.Map(json);
        DispatchEvents(currentTurn.Events);

        OnTick(currentTurn);
      }

      private void DispatchEvents(IEnumerable<Event> events)
      {
        foreach (var evt in events)
        {
          switch (evt)
          {
            case BotDeathEvent botDeathEvent:
              if (botDeathEvent.VictimId == MyId)
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
              botIntent.Firepower = 0d;
              OnBulletFired(bulletFiredEvent);
              break;

            case BulletHitBotEvent bulletHitBotEvent:
              if (bulletHitBotEvent.VictimId == myId)
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
              Console.WriteLine(evt);
              break;
          }
        }
      }
    }
  }

  public class KnownTypesBinder : ISerializationBinder
  {
    public IList<Type> KnownTypes { get; set; }

    public Type BindToType(string assemblyName, string typeName)
    {
      return KnownTypes.SingleOrDefault(t => t.Name == typeName);
    }

    public void BindToName(Type serializedType, out string assemblyName, out string typeName)
    {
      assemblyName = null;
      typeName = serializedType.Name;
    }
  }
}