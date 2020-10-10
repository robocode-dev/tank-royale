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

      private readonly BotInfo botInfo;
      internal readonly BotEvents botEvents;

      internal BotIntent botIntent = new BotIntent();

      // Server connection
      private WebSocketClient socket;
      private ServerHandshake serverHandshake = null;

      internal EventWaitHandle exitEvent = new ManualResetEvent(false);

      // Current game states:
      private int? myId = null;
      private GameSetup gameSetup = null;
      private TickEvent currentTick = null;
      private long? ticksStart;

      private readonly bool doDispatchEvents;

      internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUri)
      {
        this.botEvents = new BotEvents(baseBot);
        this.botInfo = (botInfo == null) ? EnvVars.GetBotInfo() : botInfo;
        this.doDispatchEvents = !(baseBot is IBot);

        Init(serverUri);
      }

      private void Init(Uri serverUri)
      {
        socket = new WebSocketClient((serverUri == null) ? ServerUriFromSetting : serverUri);

        botIntent.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotIntent); // must be set

        botEvents.onBulletFiredManager.Subscribe(HandleBulletFired);
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
          throw new BotException("Could not connect to web socket: "
              + socket.ServerUri
              + ". Setup "
              + EnvVars.SERVER_URL
              + " to point to a server that is up and running.",
            ex);
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
        currentTick = null;
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

      internal TickEvent CurrentTick
      {
        get
        {
          if (currentTick == null)
          {
            throw new BotException(TickNotAvailableMsg);
          }
          return currentTick;
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
        botEvents.FireConnectedEvent(new ConnectedEvent(socket.ServerUri));
      }

      private void HandleDisconnected(bool remote)
      {
        botEvents.FireDisconnectedEvent(new DisconnectedEvent(socket.ServerUri, remote));
      }

      private void HandleConnectionError(Exception cause)
      {
        botEvents.FireConnectionErrorEvent(new ConnectionErrorEvent(socket.ServerUri, cause));
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

        botEvents.FireGameStartedEvent(new GameStartedEvent((int)myId, gameSetup));
      }

      private void HandleGameEndedEvent(string json)
      {
        // Clear current game state
        ClearCurrentGameState();

        // Send the game ended event
        var gameEndedEventForBot = JsonConvert.DeserializeObject<GameEndedEventForBot>(json);
        var results = ResultsMapper.Map(gameEndedEventForBot.Results);

        botEvents.FireGameEndedEvent(new GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results));
      }

      public void HandleSkippedTurnEvent(string json)
      {
        var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.SkippedTurnEvent>(json);
        botEvents.FireSkippedTurnEvent(EventMapper.Map(skippedTurnEvent));
      }

      private void HandleTickEvent(string json)
      {
        var tickEventForBot = JsonConvert.DeserializeObject<Schema.TickEventForBot>(json);
        currentTick = EventMapper.Map(json);

        ticksStart = DateTime.Now.Ticks;
        botEvents.FireTickEvent(currentTick);

        if (doDispatchEvents)
        {
          botEvents.FireEvents(currentTick);
        }
      }

      private void HandleBulletFired(BulletFiredEvent bulletFiredEvent)
      {
        botIntent.Firepower = 0; // Reset firepower so the bot stops firing continuously
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