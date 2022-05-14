using System;
using System.Collections.Generic;
using System.Threading;
using Newtonsoft.Json;
using S = Robocode.TankRoyale.Schema;
using E = Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

public sealed class BaseBotInternals
{
    private const string DefaultServerUrl = "ws://localhost:7654";

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
    private S.BotIntent botIntent = NewBotIntent();

    private int? myId;
    private GameSetup gameSetup;

    private E.TickEvent tickEvent;
    private long? ticksStart;

    private readonly EventQueue eventQueue;

    private readonly object nextTurnMonitor = new();

    private bool isRunning;
    private readonly object isRunningLock = new();

    private IStopResumeListener stopResumeListener;

    private readonly double maxSpeed;
    private readonly double maxTurnRate;
    private readonly double maxGunTurnRate;
    private readonly double maxRadarTurnRate;

    private double? savedTargetSpeed;
    private double? savedTurnRate;
    private double? savedGunTurnRate;
    private double? savedRadarTurnRate;

    private readonly double absDeceleration;


    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUrl, string serverSecret)
    {
        this.baseBot = baseBot;
        this.botInfo = botInfo ?? EnvVars.GetBotInfo();

        BotEventHandlers = new BotEventHandlers(baseBot);
        eventQueue = new EventQueue(this, BotEventHandlers);

        absDeceleration = Math.Abs(Constants.Deceleration);

        maxSpeed = Constants.MaxSpeed;
        maxTurnRate = Constants.MaxTurnRate;
        maxGunTurnRate = Constants.MaxGunTurnRate;
        maxRadarTurnRate = Constants.MaxRadarTurnRate;

        this.serverSecret = serverSecret ?? ServerSecretFromSetting;

        Init(serverUrl ?? ServerUrlFromSetting);
    }

    private void Init(Uri serverUrl)
    {
        socket = new WebSocketClient(serverUrl);
        socket.OnConnected += HandleConnected;
        socket.OnDisconnected += HandleDisconnected;
        socket.OnError += HandleConnectionError;
        socket.OnTextMessage += HandleTextMessage;

        BotEventHandlers.onRoundStarted.Subscribe(OnRoundStarted, 100);
        BotEventHandlers.onNextTurn.Subscribe(OnNextTurn, 100);
        BotEventHandlers.onBulletFired.Subscribe(OnBulletFired, 100);
    }

    public bool IsRunning
    {
        get
        {
            lock (isRunningLock)
            {
                return isRunning;
            }
        }
        set
        {
            lock (isRunningLock)
            {
                isRunning = value;
            }
        }
    }

    public void SetStopResumeHandler(IStopResumeListener listener)
    {
        stopResumeListener = listener;
    }

    private static S.BotIntent NewBotIntent()
    {
        var botIntent = new S.BotIntent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotIntent) // must be set
        };
        return botIntent;
    }

    internal BotEventHandlers BotEventHandlers { get; }

    internal void DisableEventQueue()
    {
        eventQueue.Disable();
    }

    internal void SetInterruptible(bool interruptible)
    {
        eventQueue.SetInterruptible(interruptible);
    }

    internal void SetScannedBotEventInterruptible()
    {
        eventQueue.SetInterruptible(typeof(E.ScannedBotEvent), true);
    }

    internal ISet<Events.Condition> Conditions { get; } = new HashSet<Events.Condition>();

    private void OnRoundStarted(E.RoundStartedEvent e)
    {
        botIntent = NewBotIntent();
        eventQueue.Clear();
        IsStopped = false;
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
        catch (Exception)
        {
            throw new BotException($"Could not connect to web socket for URL: {socket.ServerUri}");
        }
    }

    internal void Execute()
    {
        if (!IsRunning)
            return;

        SendIntent();
        WaitForNextTurn();
        DispatchEvents();
    }

    private void SendIntent()
    {
        LimitTargetSpeedAndTurnRates();
        socket.SendTextMessage(JsonConvert.SerializeObject(botIntent));
    }

    private void WaitForNextTurn()
    {
        var turnNumber = CurrentTick.TurnNumber;

        lock (nextTurnMonitor)
        {
            while (IsRunning && turnNumber >= CurrentTick.TurnNumber)
                Monitor.Wait(nextTurnMonitor);
        }
    }

    private void DispatchEvents()
    {
        try
        {
            eventQueue.DispatchEvents(CurrentTick.TurnNumber);
        }
        catch (InterruptEventHandlerException)
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

    internal string Variant => ServerHandshake.Variant;

    internal string Version => ServerHandshake.Version;

    internal int MyId
    {
        get
        {
            if (myId == null) throw new BotException(GameNotRunningMsg);
            return (int)myId;
        }
    }

    internal GameSetup GameSetup
    {
        get
        {
            if (gameSetup == null) throw new BotException(GameNotRunningMsg);
            return gameSetup;
        }
    }

    internal S.BotIntent BotIntent
    {
        get
        {
            if (botIntent == null) throw new BotException(GameNotRunningMsg);
            return botIntent;
        }
    }

    internal E.TickEvent CurrentTick
    {
        get
        {
            if (tickEvent == null) throw new BotException(TickNotAvailableMsg);
            return tickEvent;
        }
    }

    private long TicksStart
    {
        get
        {
            if (ticksStart == null) throw new BotException(TickNotAvailableMsg);
            return (long)ticksStart;
        }
    }

    internal int TimeLeft
    {
        get
        {
            var passesMicroSeconds = (DateTime.Now.Ticks - TicksStart) / 10;
            return (int)(gameSetup.TurnTimeout - passesMicroSeconds);
        }
    }

    internal bool SetFire(double firepower)
    {
        if (IsNaN(firepower)) throw new ArgumentException("firepower cannot be NaN");

        if (baseBot.Energy < firepower || CurrentTick.BotState.GunHeat > 0)
            return false; // cannot fire yet
        botIntent.Firepower = firepower;
        return true;
    }

    internal double MaxSpeed
    {
        get => maxSpeed;
        set => Math.Clamp(value, 0, Constants.MaxSpeed);
    }

    internal double MaxTurnRate
    {
        get => maxTurnRate;
        set => Math.Clamp(value, 0, Constants.MaxTurnRate);
    }

    internal double MaxGunTurnRate
    {
        get => maxGunTurnRate;
        set => Math.Clamp(value, 0, Constants.MaxGunTurnRate);
    }

    internal double MaxRadarTurnRate
    {
        get => maxRadarTurnRate;
        set => Math.Clamp(value, 0, Constants.MaxRadarTurnRate);
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

        var targetSpeed = IsPositiveInfinity(distance) ? maxSpeed : Math.Min(GetMaxSpeed(distance), maxSpeed);

        return speed >= 0
            ? Math.Clamp(targetSpeed, speed - absDeceleration, speed + Constants.Acceleration)
            : Math.Clamp(targetSpeed, speed - Constants.Acceleration, speed + GetMaxDeceleration(-speed));
    }

    private double GetMaxSpeed(double distance)
    {
        var decelerationTime =
            Math.Max(1, Math.Ceiling((Math.Sqrt((4 * 2 / absDeceleration) * distance + 1) - 1) / 2));
        if (IsPositiveInfinity(decelerationTime))
            return Constants.MaxSpeed;

        var decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * absDeceleration;
        return ((decelerationTime - 1) * absDeceleration) + ((distance - decelerationDistance) / decelerationTime);
    }

    private double GetMaxDeceleration(double speed)
    {
        var decelerationTime = speed / absDeceleration;
        var accelerationTime = 1 - decelerationTime;

        return Math.Min(1, decelerationTime) * absDeceleration +
               Math.Max(0, accelerationTime) * Constants.Acceleration;
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
        Conditions.Add(condition);
    }

    internal void RemoveCondition(Events.Condition condition)
    {
        Conditions.Remove(condition);
    }

    internal void SetStop()
    {
        if (IsStopped) return;

        IsStopped = true;

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

    internal void SetResume()
    {
        if (!IsStopped) return;

        botIntent.TargetSpeed = savedTargetSpeed;
        botIntent.TurnRate = savedTurnRate;
        botIntent.GunTurnRate = savedGunTurnRate;
        botIntent.RadarTurnRate = savedRadarTurnRate;

        stopResumeListener?.OnResume();
        IsStopped = false; // must be last step
    }

    internal bool IsStopped { get; private set; }

    private S.ServerHandshake ServerHandshake
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

    private static Uri ServerUrlFromSetting
    {
        get
        {
            var uri = EnvVars.GetServerUrl() ?? DefaultServerUrl;
            if (!Uri.IsWellFormedUriString(uri, UriKind.Absolute))
            {
                throw new BotException("Incorrect syntax for server uri: " + uri + ". Default is: " +
                                       DefaultServerUrl);
            }

            return new Uri(uri);
        }
    }

    private static string ServerSecretFromSetting => EnvVars.GetServerSecret();

    private void HandleConnected()
    {
        BotEventHandlers.FireConnectedEvent(new E.ConnectedEvent(socket.ServerUri));
    }

    private void HandleDisconnected(bool remote, int? statusCode, string reason)
    {
        BotEventHandlers.FireDisconnectedEvent(
            new E.DisconnectedEvent(socket.ServerUri, remote, statusCode, reason));
    }

    private void HandleConnectionError(Exception cause)
    {
        BotEventHandlers.FireConnectionErrorEvent(new E.ConnectionErrorEvent(socket.ServerUri,
            new Exception(cause.Message)));

        // Terminate
        Console.WriteLine("Exiting");
        Environment.Exit(1);
    }

    private void HandleTextMessage(string json)
    {
        if (json == "{\"$type\":\"GameAbortedEvent\"}")
            return; // Work-around: Cannot be parsed due to $type for GameAbortedEvent?!

        var jsonMsg = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
        try
        {
            var type = (string)jsonMsg?["$type"];
            if (string.IsNullOrWhiteSpace(type)) return;

            var msgType = (S.MessageType)Enum.Parse(typeof(S.MessageType), type);
            switch (msgType)
            {
                case S.MessageType.TickEventForBot:
                    HandleTick(json);
                    break;
                case S.MessageType.RoundStartedEvent:
                    HandleRoundStarted(json);
                    break;
                case S.MessageType.RoundEndedEvent:
                    HandleRoundEnded(json);
                    break;
                case S.MessageType.GameStartedEventForBot:
                    HandleGameStarted(json);
                    break;
                case S.MessageType.GameEndedEventForBot:
                    HandleGameEnded(json);
                    break;
                case S.MessageType.SkippedTurnEvent:
                    HandleSkippedTurn(json);
                    break;
                case S.MessageType.ServerHandshake:
                    HandleServerHandshake(json);
                    break;
                case S.MessageType.GameAbortedEvent:
                    HandleGameAborted();
                    break;
                default:
                    throw new BotException($"Unsupported WebSocket message type: {type}");
            }
        }
        catch (KeyNotFoundException)
        {
            Console.Error.WriteLine(jsonMsg);

            throw new BotException($"$type is missing on the JSON message: {json}");
        }
    }

    private void HandleTick(string json)
    {
        tickEvent = EventMapper.Map(json);

        ticksStart = DateTime.Now.Ticks;

        if (botIntent.Rescan == true)
            botIntent.Rescan = false;

        eventQueue.AddEventsFromTick(tickEvent);

        // Trigger next turn (not tick-event!)
        BotEventHandlers.FireNextTurn(tickEvent);
    }

    private void HandleRoundStarted(string json)
    {
        var roundStartedEvent = JsonConvert.DeserializeObject<S.RoundStartedEvent>(json);
        if (roundStartedEvent == null)
            throw new BotException("RoundStartedEvent is missing in JSON message from server");

        BotEventHandlers.FireRoundStartedEvent(new E.RoundStartedEvent(roundStartedEvent.RoundNumber));
    }

    private void HandleRoundEnded(string json)
    {
        var roundEndedEvent = JsonConvert.DeserializeObject<S.RoundEndedEvent>(json);
        if (roundEndedEvent == null)
            throw new BotException("RoundEndedEvent is missing in JSON message from server");

        BotEventHandlers.FireRoundEndedEvent(new E.RoundEndedEvent(roundEndedEvent.RoundNumber,
            roundEndedEvent.TurnNumber));
    }

    private void HandleGameStarted(string json)
    {
        var gameStartedEventForBot = JsonConvert.DeserializeObject<S.GameStartedEventForBot>(json);
        if (gameStartedEventForBot == null)
            throw new BotException("GameStartedEventForBot is missing in JSON message from server");

        myId = gameStartedEventForBot.MyId;
        gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

        // Send ready signal
        var ready = new S.BotReady
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotReady)
        };

        var msg = JsonConvert.SerializeObject(ready);
        socket.SendTextMessage(msg);

        BotEventHandlers.FireGameStartedEvent(new E.GameStartedEvent((int)myId, gameSetup));
    }

    private void HandleGameEnded(string json)
    {
        // Send the game ended event
        var gameEndedEventForBot = JsonConvert.DeserializeObject<S.GameEndedEventForBot>(json);
        if (gameEndedEventForBot == null)
            throw new BotException("GameEndedEventForBot is missing in JSON message from server");

        var results = ResultsMapper.Map(gameEndedEventForBot.Results);
        BotEventHandlers.FireGameEndedEvent(new E.GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results));
    }

    private void HandleGameAborted()
    {
        BotEventHandlers.FireGameAbortedEvent();
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

    private void HandleSkippedTurn(string json)
    {
        var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.SkippedTurnEvent>(json);
        BotEventHandlers.FireSkippedTurnEvent(EventMapper.Map(skippedTurnEvent));
    }
}