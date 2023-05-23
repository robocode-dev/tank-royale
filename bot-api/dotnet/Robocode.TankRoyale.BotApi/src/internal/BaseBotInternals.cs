using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.IO;
using System.Threading;
using System.Web;
using Newtonsoft.Json;
using S = Robocode.TankRoyale.Schema;
using E = Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using static Robocode.TankRoyale.BotApi.Events.DefaultEventPriority;
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

    private GameSetup gameSetup;

    private E.TickEvent tickEvent;
    private long? ticksStart;

    private readonly EventQueue eventQueue;

    private readonly object nextTurnMonitor = new();

    private bool isRunning;
    private readonly object isRunningLock = new();

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

    private bool eventHandlingDisabled;

    private readonly StringWriter stdOutStringWriter = new();
    private readonly StringWriter stdErrStringWriter = new();

    private readonly IDictionary<Type, int> eventPriorities = new Dictionary<Type, int>();

    private ICollection<int> teammateIds;
    
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
        RedirectStdOutAndStdErr();
        InitializeWebSocketClient(serverUrl);
        InitializeEventPriorities();
        SubscribeToEvents();
    }

    private void RedirectStdOutAndStdErr()
    {
        if (!EnvVars.IsBotBooted()) return;
        Console.SetOut(stdOutStringWriter);
        Console.SetError(stdErrStringWriter);
    }

    private void InitializeWebSocketClient(Uri serverUrl)
    {
        socket = new WebSocketClient(serverUrl);
        socket.OnConnected += HandleConnected;
        socket.OnDisconnected += HandleDisconnected;
        socket.OnError += HandleConnectionError;
        socket.OnTextMessage += HandleTextMessage;
    }

    private void InitializeEventPriorities()
    {
        eventPriorities[typeof(E.WonRoundEvent)] = WonRound;
        eventPriorities[typeof(E.SkippedTurnEvent)] = SkippedTurn;
        eventPriorities[typeof(E.TickEvent)] = Tick;
        eventPriorities[typeof(E.CustomEvent)] = Custom;
        eventPriorities[typeof(E.TeamMessageEvent)] = TeamMessage;
        eventPriorities[typeof(E.BotDeathEvent)] = BotDeath;
        eventPriorities[typeof(E.BulletHitWallEvent)] = BulletHitWall;
        eventPriorities[typeof(E.BulletHitBulletEvent)] = BulletHitBullet;
        eventPriorities[typeof(E.BulletHitBotEvent)] = BulletHitBot;
        eventPriorities[typeof(E.BulletFiredEvent)] = BulletFired;
        eventPriorities[typeof(E.HitByBulletEvent)] = HitByBullet;
        eventPriorities[typeof(E.HitWallEvent)] = HitWall;
        eventPriorities[typeof(E.HitBotEvent)] = HitBot;
        eventPriorities[typeof(E.ScannedBotEvent)] = ScannedBot;
        eventPriorities[typeof(E.DeathEvent)] = Death;
    }

    private void SubscribeToEvents()
    {
        BotEventHandlers.OnRoundStarted.Subscribe(OnRoundStarted, 100);
        BotEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 100);
        BotEventHandlers.OnBulletFired.Subscribe(OnBulletFired, 100);
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

    public void EnableEventHandling(bool enable) => eventHandlingDisabled = !enable;

    public void SetStopResumeHandler(IStopResumeListener listener) => stopResumeListener = listener;

    private static S.BotIntent NewBotIntent()
    {
        var botIntent = new S.BotIntent
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotIntent) // must be set
        };
        return botIntent;
    }

    private void ResetMovement()
    {
        BotIntent.TurnRate = null;
        BotIntent.GunTurnRate = null;
        BotIntent.RadarTurnRate = null;
        BotIntent.TargetSpeed = null;
        BotIntent.Firepower = null;
    }

    internal BotEventHandlers BotEventHandlers { get; }

    internal IList<E.BotEvent> Events => eventQueue.Events;

    internal void ClearEvents() => eventQueue.ClearEvents();

    internal void SetInterruptible(bool interruptible) => eventQueue.SetInterruptible(interruptible);

    internal void SetScannedBotEventInterruptible() =>
        eventQueue.SetInterruptible(typeof(E.ScannedBotEvent), true);

    internal ISet<Events.Condition> Conditions { get; } = new HashSet<Events.Condition>();

    private void OnRoundStarted(E.RoundStartedEvent e)
    {
        ResetMovement();
        eventQueue.Clear();
        IsStopped = false;
        eventHandlingDisabled = false;
    }

    private void OnNextTurn(E.TickEvent e)
    {
        lock (nextTurnMonitor)
        {
            // Unblock methods waiting for the next turn
            Monitor.PulseAll(nextTurnMonitor);
        }
    }

    private void OnBulletFired(E.BulletFiredEvent e) =>
        BotIntent.Firepower = 0; // Reset firepower so the bot stops firing continuously

    internal void Start()
    {
        IsRunning = true;
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

        var turnNumber = CurrentTick.TurnNumber;

        DispatchEvents(turnNumber);
        SendIntent();
        WaitForNextTurn(turnNumber);
    }

    private void SendIntent()
    {
        TransferStdOutToBotIntent();
        socket.SendTextMessage(JsonConvert.SerializeObject(BotIntent));
    }

    private void TransferStdOutToBotIntent()
    {
        var stdOutText = stdOutStringWriter.ToString();
        BotIntent.StdOut = stdOutText.Length > 0
            ? HttpUtility.JavaScriptStringEncode(stdOutText.Replace("\r", ""))
            : null;
        stdOutStringWriter.GetStringBuilder().Clear();

        var stdErrText = stdErrStringWriter.ToString();
        BotIntent.StdErr = stdErrText.Length > 0
            ? HttpUtility.JavaScriptStringEncode(stdErrText.Replace("\r", ""))
            : null;
        stdErrStringWriter.GetStringBuilder().Clear();
    }

    private void WaitForNextTurn(int turnNumber)
    {
        lock (nextTurnMonitor)
        {
            while (IsRunning && turnNumber >= CurrentTick.TurnNumber)
            {
                try
                {
                    Monitor.Wait(nextTurnMonitor);
                }
                catch (ThreadInterruptedException)
                {
                    return; // stop waiting, thread has been interrupted (stopped)
                }
            }
        }
    }

    private void DispatchEvents(int turnNumber)
    {
        try
        {
            eventQueue.DispatchEvents(turnNumber);
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

    internal string Variant => ServerHandshake.Variant;

    internal string Version => ServerHandshake.Version;

    internal int MyId { get; private set; }

    internal GameSetup GameSetup => gameSetup ?? throw new BotException(GameNotRunningMsg);

    internal S.BotIntent BotIntent { get; } = NewBotIntent();

    internal E.TickEvent CurrentTick => tickEvent ?? throw new BotException(TickNotAvailableMsg);

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
        BotIntent.Firepower = firepower;
        return true;
    }

    internal double GunHeat => tickEvent == null ? 0 : tickEvent.BotState.GunHeat;

    internal double Speed => tickEvent == null ? 0 : tickEvent.BotState.Speed;

    internal double TurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.TurnRate != null)
            {
                return (double)BotIntent.TurnRate;
            }

            return tickEvent == null ? 0 : tickEvent.BotState.TurnRate;
        }
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("TurnRate cannot be NaN");
            }

            BotIntent.TurnRate = Math.Clamp(value, -maxTurnRate, maxTurnRate);
        }
    }

    internal double GunTurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.GunTurnRate != null)
            {
                return (double)BotIntent.GunTurnRate;
            }

            return tickEvent == null ? 0 : tickEvent.BotState.GunTurnRate;
        }
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("GunTurnRate cannot be NaN");
            }

            BotIntent.GunTurnRate = Math.Clamp(value, -maxGunTurnRate, maxGunTurnRate);
        }
    }

    internal double RadarTurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.RadarTurnRate != null)
            {
                return (double)BotIntent.RadarTurnRate;
            }

            return tickEvent == null ? 0 : tickEvent.BotState.RadarTurnRate;
        }
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("RadarTurnRate cannot be NaN");
            }

            BotIntent.RadarTurnRate = Math.Clamp(value, -maxRadarTurnRate, maxRadarTurnRate);
        }
    }

    internal double TargetSpeed
    {
        get => BotIntent.TargetSpeed ?? 0d;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("TargetSpeed cannot be NaN");
            }

            BotIntent.TargetSpeed = Math.Clamp(value, -maxSpeed, maxSpeed);
        }
    }

    internal double MaxTurnRate
    {
        get => maxTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxTurnRate cannot be NaN");
            }

            maxTurnRate = Math.Clamp(value, 0, Constants.MaxTurnRate);
        }
    }

    internal double MaxGunTurnRate
    {
        get => maxGunTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxGunTurnRate cannot be NaN");
            }

            maxGunTurnRate = Math.Clamp(value, 0, Constants.MaxGunTurnRate);
        }
    }

    internal double MaxRadarTurnRate
    {
        get => maxRadarTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxRadarTurnRate cannot be NaN");
            }

            maxRadarTurnRate = Math.Clamp(value, 0, Constants.MaxRadarTurnRate);
        }
    }

    internal double MaxSpeed
    {
        get => maxSpeed;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("MaxSpeed cannot be NaN");
            }

            maxSpeed = Math.Clamp(value, 0, Constants.MaxSpeed);
        }
    }

    /// <summary>
    /// Returns the new speed based on the current speed and distance to move.
    ///
    /// <param name="speed">Is the current speed</param>
    /// <param name="distance">Is the distance to move</param>
    /// <return>The new speed</return>
    /// </summary>

    // Credits for this algorithm goes to Patrick Cupka (aka Voidious),
    // Julian Kent (aka Skilgannon), and Positive for the original version:
    // https://robowiki.net/wiki/User:Voidious/Optimal_Velocity#Hijack_2
    internal double GetNewTargetSpeed(double speed, double distance)
    {
        if (distance < 0)
            return -GetNewTargetSpeed(-speed, -distance);

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
            distance += (speed = GetNewTargetSpeed(speed, 0));

        return distance;
    }

    internal bool AddCondition(Events.Condition condition) => Conditions.Add(condition);

    internal bool RemoveCondition(Events.Condition condition) => Conditions.Remove(condition);

    internal void SetStop()
    {
        if (IsStopped) return;

        IsStopped = true;

        savedTargetSpeed = BotIntent.TargetSpeed;
        savedTurnRate = BotIntent.TurnRate;
        savedGunTurnRate = BotIntent.GunTurnRate;
        savedRadarTurnRate = BotIntent.RadarTurnRate;

        BotIntent.TargetSpeed = 0;
        BotIntent.TurnRate = 0;
        BotIntent.GunTurnRate = 0;
        BotIntent.RadarTurnRate = 0;

        stopResumeListener?.OnStop();
    }

    internal void SetResume()
    {
        if (!IsStopped) return;

        BotIntent.TargetSpeed = savedTargetSpeed;
        BotIntent.TurnRate = savedTurnRate;
        BotIntent.GunTurnRate = savedGunTurnRate;
        BotIntent.RadarTurnRate = savedRadarTurnRate;

        stopResumeListener?.OnResume();
        IsStopped = false; // must be last step
    }

    internal bool IsStopped { get; private set; }

    internal ICollection<int> TeammateIds => teammateIds ?? throw new BotException(GameNotRunningMsg);

    internal bool IsTeammate(int botId) => TeammateIds.Contains(botId);

    internal void BroadcastTeamMessage(object message) => SendTeamMessage(null, message);

    internal void SendTeamMessage(int? teammateId, object message)
    {
        if (teammateId != null && !TeammateIds.Contains((int)teammateId)) {
            throw new ArgumentException("No teammate was found with the specified 'teammateId': " + teammateId);
        }

        var teamMessages = BotIntent.TeamMessages; 
        if (teamMessages is { Count: IBaseBot.MaxNumberOfTeamMessagesPerTurn })
            throw new InvalidOperationException(
                "The maximum number team massages has already been reached: " +
                IBaseBot.MaxNumberOfTeamMessagesPerTurn);

        var bytes = System.Text.Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(message));
        if (bytes.Length > IBaseBot.TeamMessageMaxSize)
            throw new ArgumentException(
                $"The team message is larger than the limit of {IBaseBot.TeamMessageMaxSize} bytes");

        S.TeamMessage teamMessage = new()
        {
            ReceiverId = teammateId,
            Message = Convert.ToBase64String(bytes)
        };

        teamMessages?.Add(teamMessage);
    }

    internal int GetPriority(Type eventType)
    {
        if (!eventPriorities.ContainsKey(eventType))
        {
            throw new InvalidOperationException($"Could not get event priority for the type: {eventType.Name}");
        }

        return eventPriorities[eventType];
    }

    internal void SetPriority(Type eventType, int priority) => eventPriorities[eventType] = priority;

    internal Color BodyColor
    {
        get => tickEvent?.BotState.BodyColor;
        set => BotIntent.BodyColor = ToIntentColor(value);
    }

    internal Color TurretColor
    {
        get => tickEvent?.BotState.TurretColor;
        set => BotIntent.TurretColor = ToIntentColor(value);
    }

    internal Color RadarColor
    {
        get => tickEvent?.BotState.RadarColor;
        set => BotIntent.RadarColor = ToIntentColor(value);
    }

    internal Color BulletColor
    {
        get => tickEvent?.BotState.BulletColor;
        set => BotIntent.BulletColor = ToIntentColor(value);
    }

    internal Color ScanColor
    {
        get => tickEvent?.BotState.ScanColor;
        set => BotIntent.ScanColor = ToIntentColor(value);
    }

    internal Color TracksColor
    {
        get => tickEvent?.BotState.TracksColor;
        set => BotIntent.TracksColor = ToIntentColor(value);
    }

    internal Color GunColor
    {
        get => tickEvent?.BotState.GunColor;
        set => BotIntent.GunColor = ToIntentColor(value);
    }

    private static string ToIntentColor(Color color) => color == null ? null : "#" + color.ToHex();

    internal IEnumerable<BulletState> BulletStates => tickEvent?.BulletStates ?? ImmutableHashSet<BulletState>.Empty;

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
                throw new BotException($"Incorrect syntax for server uri: {uri}. Default is: {DefaultServerUrl}");

            return new Uri(uri);
        }
    }

    private static string ServerSecretFromSetting => EnvVars.GetServerSecret();

    private void HandleConnected() =>
        BotEventHandlers.FireConnectedEvent(new E.ConnectedEvent(socket.ServerUri));

    private void HandleDisconnected(bool remote, int? statusCode, string reason)
    {
        BotEventHandlers.FireDisconnectedEvent(
            new E.DisconnectedEvent(socket.ServerUri, remote, statusCode, reason));

        closedEvent.Set();
    }

    private void HandleConnectionError(Exception cause) =>
        BotEventHandlers.FireConnectionErrorEvent(new E.ConnectionErrorEvent(socket.ServerUri,
            new Exception(cause.Message)));

    private void HandleTextMessage(string json)
    {
        var jsonMsg = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
        try
        {
            var type = (string)jsonMsg?["type"];
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
                case S.MessageType.RoundEndedEventForBot:
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

            throw new BotException($"'type' is missing on the JSON message: {json}");
        }
    }

    private void HandleTick(string json)
    {
        if (eventHandlingDisabled) return;

        ticksStart = DateTime.Now.Ticks;

        if (BotIntent.Rescan == true)
            BotIntent.Rescan = false;

        var newTickEvent = EventMapper.Map(json, MyId);
        eventQueue.AddEventsFromTick(newTickEvent);

        tickEvent = newTickEvent;

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
        var roundEndedEventForBot = JsonConvert.DeserializeObject<S.RoundEndedEventForBot>(json);
        if (roundEndedEventForBot == null)
            throw new BotException("RoundEndedEventForBot is missing in JSON message from server");

        var botResults = ResultsMapper.Map(roundEndedEventForBot.Results);
        BotEventHandlers.FireRoundEndedEvent(new E.RoundEndedEvent(roundEndedEventForBot.RoundNumber,
            roundEndedEventForBot.TurnNumber, botResults));
    }

    private void HandleGameStarted(string json)
    {
        var gameStartedEventForBot = JsonConvert.DeserializeObject<S.GameStartedEventForBot>(json);
        if (gameStartedEventForBot == null)
            throw new BotException("GameStartedEventForBot is missing in JSON message from server");

        MyId = gameStartedEventForBot.MyId;
        teammateIds = gameStartedEventForBot.TeammateIds;
        gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

        // Send ready signal
        var ready = new S.BotReady
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotReady)
        };
        var msg = JsonConvert.SerializeObject(ready);
        socket.SendTextMessage(msg);

        BotEventHandlers.FireGameStartedEvent(new E.GameStartedEvent(MyId, gameSetup));
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

    private void HandleGameAborted() => BotEventHandlers.FireGameAbortedEvent();

    private void HandleServerHandshake(string json)
    {
        serverHandshake = JsonConvert.DeserializeObject<S.ServerHandshake>(json);

        // Reply by sending bot handshake
        var botHandshake = BotHandshakeFactory.Create(serverHandshake?.SessionId, botInfo, serverSecret);
        botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotHandshake);
        var text = JsonConvert.SerializeObject(botHandshake);

        socket.SendTextMessage(text);
    }

    private void HandleSkippedTurn(string json)
    {
        if (eventHandlingDisabled) return;

        var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.SkippedTurnEvent>(json);
        BotEventHandlers.FireSkippedTurnEvent(EventMapper.Map(skippedTurnEvent));
    }
}