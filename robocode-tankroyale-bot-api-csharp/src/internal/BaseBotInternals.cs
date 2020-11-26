using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using Robocode.TankRoyale.Schema;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Internal
{
  public sealed class BaseBotInternals
  {
    private const string NotConnectedToServerMsg =
      "Not connected to game server yes. Make sure OnConnected() event handler has been called first";

    private const string GameNotRunningMsg =
      "Game is not running. Make sure OnGameStarted() event handler has been called first";

    private const string TickNotAvailableMsg =
      "Game is not running or tick has not occurred yet. Make sure OnTick() event handler has been called first";

    private readonly double absDeceleration;

    private readonly IBaseBot baseBot;
    private readonly BotInfo botInfo;
    private readonly BotEventHandlers botEventHandlers;
    private readonly EventQueue eventQueue;
    private readonly ISet<Events.Condition> conditions = new HashSet<Events.Condition>();

    private BotIntent botIntent = newBotIntent();

    // Server connection
    private WebSocketClient socket;
    private ServerHandshake serverHandshake;

    private EventWaitHandle exitEvent = new ManualResetEvent(false);

    // Current game states:
    private int? myId;
    private GameSetup gameSetup;
    private TickEvent tickEvent;
    private long? ticksStart;

    // Maximum speed and turn rates
    private double maxSpeed;
    private double maxTurnRate;
    private double maxGunTurnRate;
    private double maxRadarTurnRate;

    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUri)
    {
      this.baseBot = baseBot;
      this.botInfo = (botInfo == null) ? EnvVars.GetBotInfo() : botInfo;

      this.botEventHandlers = new BotEventHandlers(baseBot);
      this.eventQueue = new EventQueue(this, botEventHandlers);

      this.absDeceleration = Math.Abs(baseBot.Deceleration);

      this.maxSpeed = baseBot.MaxSpeed;
      this.maxTurnRate = baseBot.MaxTurnRate;
      this.maxGunTurnRate = baseBot.MaxGunTurnRate;
      this.maxRadarTurnRate = baseBot.MaxRadarTurnRate;

      Init(serverUri == null ? ServerUriFromSetting : serverUri);
    }

    private void Init(Uri serverUri)
    {
      socket = new WebSocketClient(serverUri);
      socket.OnConnected += new WebSocketClient.OnConnectedHandler(HandleConnected);
      socket.OnDisconnected += new WebSocketClient.OnDisconnectedHandler(HandleDisconnected);
      socket.OnError += new WebSocketClient.OnErrorHandler(HandleConnectionError);
      socket.OnTextMessage += new WebSocketClient.OnTextMessageHandler(HandleTextMessage);

      botEventHandlers.onNewRound.Subscribe(HandleNewRound, 100);
      botEventHandlers.onBulletFired.Subscribe(HandleBulletFired, 100);
    }

    private static BotIntent newBotIntent()
    {
      BotIntent botIntent = new BotIntent();
      botIntent.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotIntent); // must be set
      return botIntent;
    }

    internal BotEventHandlers BotEventHandlers { get => botEventHandlers; }

    internal ISet<Events.Condition> Conditions { get => conditions; }

    internal void Start()
    {
      Connect();
      exitEvent.WaitOne();
    }

    private void Connect()
    {
      try
      {
        socket.Connect();
      }
      catch (Exception ex)
      {
        throw new BotException($"Could not connect to web socket: {socket.ServerUri}. " +
            $"Setup {EnvVars.ServerUrl} to point to a server that is up and running.", ex);
      }
    }

    internal void Execute()
    {
      // Send the bot intent to the server
      SendIntent();

      // Clear rescanning
      botIntent.Scan = false;

      // Dispatch all bot events
      new Thread(new ThreadStart(DispatchEvents)).Start();
    }

    private void DispatchEvents()
    {
      eventQueue.DispatchEvents(CurrentTick.TurnNumber);
    }

    internal void SendIntent()
    {
      if (botIntent?.TargetSpeed > maxSpeed)
      {
        botIntent.TargetSpeed = maxSpeed;
      }
      if (botIntent?.TurnRate > maxTurnRate)
      {
        botIntent.TurnRate = maxTurnRate;
      }
      if (botIntent?.GunTurnRate > maxGunTurnRate)
      {
        botIntent.GunTurnRate = maxGunTurnRate;
      }
      if (botIntent?.RadarTurnRate > maxRadarTurnRate)
      {
        botIntent.RadarTurnRate = maxRadarTurnRate;
      }
      socket.SendTextMessage(JsonConvert.SerializeObject(botIntent));
    }

    internal string Variant
    {
      get => ServerHandshake.Variant;
    }

    internal string Version
    {
      get => ServerHandshake.Version;
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
        if (tickEvent == null)
        {
          throw new BotException(TickNotAvailableMsg);
        }
        return tickEvent;
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

    internal int TimeLeft
    {
      get
      {
        long passesMicroSeconds = (DateTime.Now.Ticks - TicksStart) / 10;
        return (int)(gameSetup.TurnTimeout - passesMicroSeconds);
      }
    }

    internal bool SetFire(double firepower)
    {
      if (Double.IsNaN(firepower))
      {
        throw new ArgumentException("firepower cannot be NaN");
      }
      if (baseBot.Energy < firepower || CurrentTick.BotState.GunHeat > 0)
      {
        return false; // cannot fire yet
      }
      botIntent.Firepower = firepower;
      return true;
    }

    internal void SetMaxSpeed(double maxSpeed)
    {
      if (maxSpeed < 0)
      {
        maxSpeed = 0;
      }
      else if (maxSpeed > baseBot.MaxSpeed)
      {
        maxSpeed = baseBot.MaxSpeed;
      }
      this.maxSpeed = maxSpeed;
    }

    internal void SetMaxTurnRate(double maxTurnRate)
    {
      if (maxTurnRate < 0)
      {
        maxTurnRate = 0;
      }
      else if (maxTurnRate > baseBot.MaxTurnRate)
      {
        maxTurnRate = baseBot.MaxTurnRate;
      }
      this.maxTurnRate = maxTurnRate;
    }

    internal void SetMaxGunTurnRate(double maxGunTurnRate)
    {
      if (maxGunTurnRate < 0)
      {
        maxGunTurnRate = 0;
      }
      else if (maxGunTurnRate > baseBot.MaxGunTurnRate)
      {
        maxGunTurnRate = baseBot.MaxGunTurnRate;
      }
      this.maxGunTurnRate = maxGunTurnRate;
    }

    internal void SetMaxRadarTurnRate(double maxRadarTurnRate)
    {
      if (maxRadarTurnRate < 0)
      {
        maxRadarTurnRate = 0;
      }
      else if (maxRadarTurnRate > baseBot.MaxRadarTurnRate)
      {
        maxRadarTurnRate = baseBot.MaxRadarTurnRate;
      }
      this.maxRadarTurnRate = maxRadarTurnRate;
    }

    /// <summary>
    /// Returns the new speed based on the current speed and distance to move.
    ///
    /// <param name="speed">Is the current speed</param>
    /// <param name="distance">Is the distance to move</param>
    /// <return>The new speed</return>
    //
    // Credits for this algorithm goes to Patrick Cupka (aka Voidious), Julian Kent (aka
    // Skilgannon), and Positive:
    // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
    internal double GetNewSpeed(double speed, double distance)
    {
      if (distance < 0)
      {
        // If the distance is negative, then change it to be positive and change the sign of the
        // input velocity and the result
        return -GetNewSpeed(-speed, -distance);
      }

      double targetSpeed;
      if (distance == Double.PositiveInfinity)
      {
        targetSpeed = maxSpeed;
      }
      else
      {
        targetSpeed = Math.Min(GetMaxSpeed(distance), maxSpeed);
      }

      if (speed >= 0)
      {
        return Math.Max(speed - absDeceleration, Math.Min(targetSpeed, speed + baseBot.Acceleration));
      } // else
      return Math.Max(speed - baseBot.Acceleration, Math.Min(targetSpeed, speed + GetMaxDeceleration(-speed)));
    }

    private double GetMaxSpeed(double distance)
    {
      var decelTime =
        Math.Max(
          1,
          Math.Ceiling( // sum of 0... decelTime, solving for decelTime using quadratic formula
            (Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));

      if (decelTime == Double.PositiveInfinity)
      {
        return baseBot.MaxSpeed;
      }

      var decelDist =
        (decelTime / 2) *
        (decelTime - 1) // sum of 0..(decelTime-1)
        *
        absDeceleration;

      return ((decelTime - 1) * absDeceleration) + ((distance - decelDist) / decelTime);
    }

    private double GetMaxDeceleration(double speed)
    {
      var decelTime = speed / absDeceleration;
      var accelTime = (1 - decelTime);

      return Math.Min(1, decelTime) * absDeceleration + Math.Max(0, accelTime) * baseBot.Acceleration;
    }

    internal double GetDistanceTraveledUntilStop(double speed)
    {
      speed = Math.Abs(speed);
      double distance = 0;
      while (speed > 0)
      {
        distance += (speed = GetNewSpeed(speed, 0));
      }
      return distance;
    }

    internal void AddCondition(Events.Condition condition)
    {
      conditions.Add(condition);
    }

    internal void RemoveCondition(Events.Condition condition)
    {
      conditions.Remove(condition);
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

    private void HandleConnected()
    {
      botEventHandlers.FireConnectedEvent(new ConnectedEvent(socket.ServerUri));
    }

    private void HandleDisconnected(bool remote)
    {
      botEventHandlers.FireDisconnectedEvent(new DisconnectedEvent(socket.ServerUri, remote));
    }

    private void HandleConnectionError(Exception cause)
    {
      botEventHandlers.FireConnectionErrorEvent(new ConnectionErrorEvent(socket.ServerUri, cause));
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
              throw new BotException($"Unsupported WebSocket message type: {type}");
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
      serverHandshake = JsonConvert.DeserializeObject<ServerHandshake>(json);

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

      var msg = JsonConvert.SerializeObject(ready);
      socket.SendTextMessage(msg);

      botEventHandlers.FireGameStartedEvent(new GameStartedEvent((int)myId, gameSetup));
    }

    private void HandleGameEndedEvent(string json)
    {
      // Clear current game state
      ClearCurrentGameState();

      // Send the game ended event
      var gameEndedEventForBot = JsonConvert.DeserializeObject<GameEndedEventForBot>(json);
      var results = ResultsMapper.Map(gameEndedEventForBot.Results);

      botEventHandlers.FireGameEndedEvent(new GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results));
    }

    public void HandleSkippedTurnEvent(string json)
    {
      var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.SkippedTurnEvent>(json);
      botEventHandlers.FireSkippedTurnEvent(EventMapper.Map(skippedTurnEvent));
    }

    private void HandleTickEvent(string json)
    {
      var tickEventForBot = JsonConvert.DeserializeObject<Schema.TickEventForBot>(json);
      tickEvent = EventMapper.Map(json);

      ticksStart = DateTime.Now.Ticks;

      eventQueue.AddEventsFromTick(baseBot, tickEvent);

      // Trigger new round
      if (tickEvent.TurnNumber == 1)
      {
        botEventHandlers.FireNewRound(tickEvent);
      }

      // Trigger processing turn
      botEventHandlers.FireProcessTurn(tickEvent);
    }

    private void HandleNewRound(TickEvent evt)
    {
      tickEvent = evt; // use new bot coordinate, rates and directions etc.
      botIntent = newBotIntent();
      eventQueue.Clear();
    }

    private void HandleBulletFired(Robocode.TankRoyale.BotApi.Events.BulletFiredEvent bulletFiredEvent)
    {
      botIntent.Firepower = 0; // Reset firepower so the bot stops firing continuously
    }

    private void ClearCurrentGameState()
    {
      // Clear setting that are only available during a running game
      tickEvent = null;
      gameSetup = null;
      myId = null;
    }
  }
}

public class KnownTypesBinder : ISerializationBinder
{
  private IList<Type> KnownTypes { get; set; }

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