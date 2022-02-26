using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using S = Robocode.TankRoyale.Schema;
using E = Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Internal
{
  public sealed class BaseBotInternals
  {
    private const string NotConnectedToServerMsg =
      "Not connected to a game server. Make sure OnConnected() event handler has been called first";

    private const string GameNotRunningMsg =
      "Game is not running. Make sure OnGameStarted() event handler has been called first";

    private const string TickNotAvailableMsg =
      "Game is not running or tick has not occurred yet. Make sure OnTick() event handler has been called first";

    private readonly string serverSecret;
    private WebSocketClient socket;
    private S.ServerHandshake serverHandshake;
    private readonly EventWaitHandle closedEvent = new ManualResetEvent(false);

    private readonly IBaseBot baseBot;
    private readonly BotInfo botInfo;
    private S.BotIntent botIntent = newBotIntent();

    private int? myId;
    private GameSetup gameSetup;

    private E.TickEvent tickEvent;
    private long? ticksStart;

    private readonly BotEventHandlers botEventHandlers;
    private readonly EventQueue eventQueue;
    private readonly ISet<Events.Condition> conditions = new HashSet<Events.Condition>();

    private readonly Object nextTurnMonitor = new Object();

    private bool isStopped;
    private IStopResumeListener stopResumeListener;

    private double maxSpeed;
    private double maxTurnRate;
    private double maxGunTurnRate;
    private double maxRadarTurnRate;

    private double? savedTargetSpeed;
    private double? savedTurnRate;
    private double? savedGunTurnRate;
    private double? savedRadarTurnRate;

    private readonly double absDeceleration;


    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUrl, String serverSecret)
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

      serverUrl = serverUrl == null ? ServerUrlFromSetting : serverUrl;
      this.serverSecret = serverSecret == null ? ServerSecretFromSetting : serverSecret;

      Init(serverUrl);
    }

    private void Init(Uri serverUrl)
    {
      socket = new WebSocketClient(serverUrl);
      socket.OnConnected += new WebSocketClient.OnConnectedHandler(HandleConnected);
      socket.OnDisconnected += new WebSocketClient.OnDisconnectedHandler(HandleDisconnected);
      socket.OnError += new WebSocketClient.OnErrorHandler(HandleConnectionError);
      socket.OnTextMessage += new WebSocketClient.OnTextMessageHandler(HandleTextMessage);

      botEventHandlers.onRoundStarted.Subscribe(OnRoundStarted, 100);
      botEventHandlers.onNextTurn.Subscribe(OnNextTurn, 100);
      botEventHandlers.onBulletFired.Subscribe(OnBulletFired, 100);
    }

    public void SetStopResumeHandler(IStopResumeListener listener)
    {
      stopResumeListener = listener;
    }

    private static S.BotIntent newBotIntent()
    {
      var botIntent = new S.BotIntent();
      botIntent.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotIntent); // must be set
      return botIntent;
    }

    internal BotEventHandlers BotEventHandlers { get => botEventHandlers; }

    internal ISet<Events.Condition> Conditions { get => conditions; }

    private void OnRoundStarted(E.RoundStartedEvent e)
    {
      botIntent = newBotIntent();
      eventQueue.Clear();
      isStopped = false;
    }

    private void OnNextTurn(E.TickEvent e)
    {
      lock (nextTurnMonitor)
      {
        // Unblock methods waiting for the next turn
        Monitor.PulseAll(nextTurnMonitor);
      }
    }

    private void OnBulletFired(E.BulletFiredEvent e)
    {
      botIntent.Firepower = 0; // Reset firepower so the bot stops firing continuously
    }

    internal void Start()
    {
      Connect();

      closedEvent.WaitOne();
    }

    private void Connect()
    {
      try
      {
        socket.Connect();
      }
      catch (Exception ex)
      {
        throw new BotException($"Could not connect to web socket for URL: {socket.ServerUri}", ex);
      }
    }

    internal void Execute()
    {
      SendIntent();
      WaitForNextTurn();
      DispatchEvents();
    }

    internal void SendIntent()
    {
      LimitTargetSpeedAndTurnRates();
      socket.SendTextMessage(JsonConvert.SerializeObject(botIntent));
    }

    private void WaitForNextTurn()
    {
      int turnNumber = CurrentTick.TurnNumber;

      lock (nextTurnMonitor)
      {
        while (turnNumber >= CurrentTick.TurnNumber)
          Monitor.Wait(nextTurnMonitor);
      }
    }

    private void DispatchEvents()
    {
      try
      {
        eventQueue.DispatchEvents(CurrentTick.TurnNumber);
      }
      catch (RescanException)
      {
        // Do nothing (event handler was stopped by this exception)
      }
      catch (Exception e)
      {
        Console.Error.WriteLine(e);
      }
    }

    private void LimitTargetSpeedAndTurnRates()
    {
      var targetSpeed = botIntent.TargetSpeed;
      if (targetSpeed != null)
        botIntent.TargetSpeed = Math.Clamp((double)targetSpeed, -maxSpeed, maxSpeed);

      var turnRate = botIntent.TurnRate;
      if (turnRate != null)
        botIntent.TurnRate = Math.Clamp((double)turnRate, -maxTurnRate, maxTurnRate);

      var gunTurnRate = botIntent.GunTurnRate;
      if (gunTurnRate != null)
        botIntent.GunTurnRate = Math.Clamp((double)gunTurnRate, -maxGunTurnRate, maxGunTurnRate);

      var radarTurnRate = botIntent.RadarTurnRate;
      if (radarTurnRate != null)
        botIntent.RadarTurnRate = Math.Clamp((double)radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate);
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
          throw new BotException(GameNotRunningMsg);

        return (int)myId;
      }
    }

    internal GameSetup GameSetup
    {
      get
      {
        if (gameSetup == null)
          throw new BotException(GameNotRunningMsg);

        return gameSetup;
      }
    }

    internal S.BotIntent BotIntent
    {
      get
      {
        if (botIntent == null)
          throw new BotException(GameNotRunningMsg);

        return botIntent;
      }
    }

    internal E.TickEvent CurrentTick
    {
      get
      {
        if (tickEvent == null)
          throw new BotException(TickNotAvailableMsg);

        return tickEvent;
      }
    }

    internal long TicksStart
    {
      get
      {
        if (ticksStart == null)
          throw new BotException(TickNotAvailableMsg);

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
        throw new ArgumentException("firepower cannot be NaN");

      if (baseBot.Energy < firepower || CurrentTick.BotState.GunHeat > 0)
        return false; // cannot fire yet
      botIntent.Firepower = firepower;
      return true;
    }

    internal void SetMaxSpeed(double maxSpeed)
    {
      this.maxSpeed = Math.Clamp(maxSpeed, 0, baseBot.MaxSpeed);
    }

    internal void SetMaxTurnRate(double maxTurnRate)
    {
      this.maxTurnRate = Math.Clamp(maxTurnRate, 0, baseBot.MaxTurnRate);
    }

    internal void SetMaxGunTurnRate(double maxGunTurnRate)
    {
      this.maxGunTurnRate = Math.Clamp(maxGunTurnRate, 0, baseBot.MaxGunTurnRate);
    }

    internal void SetMaxRadarTurnRate(double maxRadarTurnRate)
    {
      this.maxRadarTurnRate = Math.Clamp(maxRadarTurnRate, 0, baseBot.MaxRadarTurnRate);
    }

    /// <summary>
    /// Returns the new speed based on the current speed and distance to move.
    ///
    /// <param name="speed">Is the current speed</param>
    /// <param name="distance">Is the distance to move</param>
    /// <return>The new speed</return>
    /// </summary>

    // Credits for this algorithm goes to Patrick Cupka (aka Voidious),
    // Julian Kent (aka Skilgannon), and Positive:
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
        targetSpeed = maxSpeed;
      else
        targetSpeed = Math.Min(GetMaxSpeed(distance), maxSpeed);
      if (speed >= 0)
        return Math.Clamp(targetSpeed, speed - absDeceleration, speed + baseBot.Acceleration);
      return Math.Clamp(targetSpeed, speed - baseBot.Acceleration, speed + absDeceleration);
    }

    private double GetMaxSpeed(double distance)
    {
      var decelTime = Math.Max(1, Math.Ceiling((Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
      if (decelTime == Double.PositiveInfinity)
        return baseBot.MaxSpeed;

      var decelDist = (decelTime / 2) * (decelTime - 1) * absDeceleration;
      return ((decelTime - 1) * absDeceleration) + ((distance - decelDist) / decelTime);
    }

    private double GetMaxDeceleration(double speed)
    {
      var decelTime = speed / absDeceleration;
      var accelTime = 1 - decelTime;

      return Math.Min(1, decelTime) * absDeceleration + Math.Max(0, accelTime) * baseBot.Acceleration;
    }

    internal double GetDistanceTraveledUntilStop(double speed)
    {
      speed = Math.Abs(speed);
      double distance = 0;
      while (speed > 0)
        distance += (speed = GetNewSpeed(speed, 0));

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

    internal void SetScan(bool doScan)
    {
      botIntent.Scan = doScan;
    }

    internal void SetStop()
    {
      if (!isStopped)
      {
        isStopped = true;

        savedTargetSpeed = botIntent.TargetSpeed;
        savedTurnRate = botIntent.TurnRate;
        savedGunTurnRate = botIntent.GunTurnRate;
        savedRadarTurnRate = botIntent.RadarTurnRate;

        botIntent.TargetSpeed = 0;
        botIntent.TurnRate = 0;
        botIntent.GunTurnRate = 0;
        botIntent.RadarTurnRate = 0;

        stopResumeListener?.OnStop();
      }
    }

    internal void SetResume()
    {
      if (isStopped)
      {
        botIntent.TargetSpeed = savedTargetSpeed;
        botIntent.TurnRate = savedTurnRate;
        botIntent.GunTurnRate = savedGunTurnRate;
        botIntent.RadarTurnRate = savedRadarTurnRate;

        stopResumeListener?.OnResume();
        isStopped = false; // must be last step
      }
    }

    internal bool IsStopped
    {
      get { return isStopped; }
    }

    internal S.ServerHandshake ServerHandshake
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

    private Uri ServerUrlFromSetting
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

    private string ServerSecretFromSetting
    {
      get
      {
        return EnvVars.GetServerSecret();
      }
    }

    private void HandleConnected()
    {
      botEventHandlers.FireConnectedEvent(new E.ConnectedEvent(socket.ServerUri));
    }

    private void HandleDisconnected(bool remote)
    {
      botEventHandlers.FireDisconnectedEvent(new E.DisconnectedEvent(socket.ServerUri, remote));
    }

    private void HandleConnectionError(Exception cause)
    {
      botEventHandlers.FireConnectionErrorEvent(new E.ConnectionErrorEvent(socket.ServerUri, cause));
    }

    private void HandleTextMessage(string json)
    {
      if (json == "{\"$type\":\"GameAbortedEvent\"}")
        return; // Work-around: Cannot be parsed due to $type for GameAbortedEvent?!

      var jsonMsg = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
      try
      {
        var type = (string)jsonMsg["$type"];

        if (!string.IsNullOrWhiteSpace(type))
        {
          var msgType = (S.MessageType)Enum.Parse(typeof(S.MessageType), type);
          switch (msgType)
          {
            case S.MessageType.TickEventForBot:
              HandleTickEvent(json);
              break;
            case S.MessageType.ServerHandshake:
              HandleServerHandshake(json);
              break;
            case S.MessageType.RoundStartedEvent:
              HandleRoundStartedEvent(json);
              break;
            case S.MessageType.RoundEndedEvent:
              HandleRoundEndedEvent(json);
              break;
            case S.MessageType.GameStartedEventForBot:
              HandleGameStartedEvent(json);
              break;
            case S.MessageType.GameEndedEventForBot:
              HandleGameEndedEvent(json);
              break;
            case S.MessageType.SkippedTurnEvent:
              HandleSkippedTurnEvent(json);
              break;
            default:
              throw new BotException($"Unsupported WebSocket message type: {type}");
          }
        }
      }
      catch (KeyNotFoundException)
      {
        Console.Error.WriteLine(jsonMsg);

        throw new BotException($"$type is missing on the JSON message: {json}");
      }
    }


    private void HandleTickEvent(string json)
    {
      var tickEventForBot = JsonConvert.DeserializeObject<Schema.TickEventForBot>(json);
      tickEvent = EventMapper.Map(json);

      ticksStart = DateTime.Now.Ticks;

      if (botIntent?.Scan == true)
        SetScan(false);

      eventQueue.AddEventsFromTick(tickEvent, baseBot);

      // Trigger next turn (not tick-event!)
      botEventHandlers.onNextTurn.Publish(tickEvent);
    }

    private void HandleRoundStartedEvent(string json)
    {
      var roundStartedEvent = JsonConvert.DeserializeObject<S.RoundStartedEvent>(json);
      botEventHandlers.FireRoundStartedEvent(new E.RoundStartedEvent(roundStartedEvent.RoundNumber));
    }

    private void HandleRoundEndedEvent(string json)
    {
      var roundEndedEvent = JsonConvert.DeserializeObject<S.RoundEndedEvent>(json);
      botEventHandlers.FireRoundEndedEvent(new E.RoundEndedEvent(roundEndedEvent.RoundNumber, roundEndedEvent.TurnNumber));
    }

    private void HandleGameStartedEvent(string json)
    {
      var gameStartedEventForBot = JsonConvert.DeserializeObject<S.GameStartedEventForBot>(json);

      myId = gameStartedEventForBot.MyId;
      gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

      // Send ready signal
      S.BotReady ready = new S.BotReady();
      ready.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotReady);

      var msg = JsonConvert.SerializeObject(ready);
      socket.SendTextMessage(msg);

      botEventHandlers.FireGameStartedEvent(new E.GameStartedEvent((int)myId, gameSetup));
    }

    private void HandleGameEndedEvent(string json)
    {
      // Send the game ended event
      var gameEndedEventForBot = JsonConvert.DeserializeObject<S.GameEndedEventForBot>(json);
      var results = ResultsMapper.Map(gameEndedEventForBot.Results);

      botEventHandlers.FireGameEndedEvent(new E.GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results));
    }

    private void HandleServerHandshake(string json)
    {
      serverHandshake = JsonConvert.DeserializeObject<S.ServerHandshake>(json);

      // Reply by sending bot handshake
      var botHandshake = BotHandshakeFactory.Create(botInfo, serverSecret);
      botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotHandshake);
      var text = JsonConvert.SerializeObject(botHandshake);

      socket.SendTextMessage(text);
    }

    public void HandleSkippedTurnEvent(string json)
    {
      var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.SkippedTurnEvent>(json);
      botEventHandlers.FireSkippedTurnEvent(EventMapper.Map(skippedTurnEvent));
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