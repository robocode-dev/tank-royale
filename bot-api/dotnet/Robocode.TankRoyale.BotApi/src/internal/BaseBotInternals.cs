using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Drawing;
using System.Linq;
using System.Threading;
using Newtonsoft.Json;
using S = Robocode.TankRoyale.Schema.Game;
using E = Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using SvgNet.Interfaces;
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

    private InitialPosition initialPosition;

    private E.TickEvent tickEvent;
    private long? ticksStart;

    private readonly EventQueue eventQueue;

    private readonly object nextTurnMonitor = new();

    private Thread thread;

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

    private int eventHandlingDisabledTurn;

    private RecordingTextWriter recordingStdOut;
    private RecordingTextWriter recordingStdErr;

    private readonly IDictionary<Type, int> eventPriorities = new Dictionary<Type, int>();

    private ICollection<int> teammateIds;

    private int lastExecuteTurnNumber;

    private readonly GraphicsState graphicsState = new();
    
    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUrl, string serverSecret)
    {
        this.baseBot = baseBot;
        this.botInfo = botInfo ?? EnvVars.GetBotInfo();

        BotEventHandlers = new BotEventHandlers(baseBot);
        InstantEventHandlers = new InstantEventHandlers();
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
        recordingStdOut = new RecordingTextWriter(Console.Out);
        recordingStdErr = new RecordingTextWriter(Console.Error);

        Console.SetOut(recordingStdOut);
        Console.SetError(recordingStdErr);
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
        InstantEventHandlers.OnRoundStarted.Subscribe(OnRoundStarted, 100);
        InstantEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 100);
        InstantEventHandlers.OnBulletFired.Subscribe(OnBulletFired, 100);
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

    internal void StartThread(IBot bot)
    {
        thread = new Thread(() => CreateRunnable(bot));;
        thread.Start();
    }

    private void CreateRunnable(IBot bot)
    {
        IsRunning = true;
        try
        {
            EnableEventHandling(true); // prevent event queue max limit to be reached

            try
            {
                bot.Run();
            }
            catch (ThreadInterruptedException)
            {
                return;
            }

            // Skip every turn after the run method has exited
            while (IsRunning)
            {
                try
                {
                    bot.Go();
                }
                catch (ThreadInterruptedException)
                {
                    return;
                }
            }
        }
        finally
        {
            EnableEventHandling(false); // prevent event queue max limit to be reached
        }
    }

    internal void StopThread()
    {
        if (!IsRunning)
            return;

        IsRunning = false;

        if (thread != null)
        {
            thread.Interrupt();
            thread = null;
        }
    }


    public void EnableEventHandling(bool enable)
    {
        eventHandlingDisabledTurn = enable ? 0 : CurrentTickOrThrow.TurnNumber;
    }

    private bool IsEventHandlingDisabled()
    {
        // Important! Allow an additional turn so events like RoundStarted can be handled
        return eventHandlingDisabledTurn != 0 && eventHandlingDisabledTurn < (CurrentTickOrThrow.TurnNumber - 1);
    }

    public void SetStopResumeHandler(IStopResumeListener listener) => stopResumeListener = listener;

    private static S.BotIntent NewBotIntent() => new()
    {
        Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotIntent), // must be set
        TeamMessages = new List<S.TeamMessage>() // initialize list
    };

    private void ResetMovement()
    {
        BotIntent.TurnRate = null;
        BotIntent.GunTurnRate = null;
        BotIntent.RadarTurnRate = null;
        BotIntent.TargetSpeed = null;
        BotIntent.Firepower = null;
    }

    internal BotEventHandlers BotEventHandlers { get; }

    internal InstantEventHandlers InstantEventHandlers { get; }

    internal IList<E.BotEvent> Events => eventQueue.Events(CurrentTickOrThrow.TurnNumber);

    internal void ClearEvents() => eventQueue.ClearEvents();

    internal void SetInterruptible(bool interruptible) => eventQueue.SetInterruptible(interruptible);

    internal void SetScannedBotEventInterruptible() =>
        eventQueue.SetInterruptible(typeof(E.ScannedBotEvent), true);

    private HashSet<Events.Condition> conditions = new();

    private void OnRoundStarted(E.RoundStartedEvent e)
    {
        ResetMovement();
        eventQueue.Clear();
        IsStopped = false;
        eventHandlingDisabledTurn = 0;
        lastExecuteTurnNumber = -1;
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
        Connect();
        closedEvent.WaitOne();
    }

    private void Connect()
    {
        var serverUri = socket.ServerUri;
        SanitizeUrl(serverUri);
        try
        {
            socket.Connect();
        }
        catch (Exception)
        {
            throw new BotException($"Could not connect to web socket for URL: {serverUri}");
        }
    }

    private static void SanitizeUrl(Uri uri)
    {
        var scheme = uri.Scheme;
        if (!new List<string> { "ws", "wss" }.Any(s => s.Contains(scheme)))
        {
            throw new BotException($"Wrong scheme used with server URL: {uri}");
        }
    }

    internal void Execute()
    {
        if (!IsRunning)
            return;

        var turnNumber = CurrentTickOrThrow.TurnNumber;
        if (turnNumber == lastExecuteTurnNumber)
        {
            return; // skip this execute, as we have already run this method within the same turn
        }

        lastExecuteTurnNumber = turnNumber;

        DispatchEvents(turnNumber);
        SendIntent();
        WaitForNextTurn(turnNumber);
    }

    private void SendIntent()
    {
        RenderGraphicsToBotIntent();
        TransferStdOutToBotIntent();
        socket.SendTextMessage(JsonConvert.SerializeObject(BotIntent));
        BotIntent.TeamMessages.Clear();
    }

    private void TransferStdOutToBotIntent()
    {
        if (recordingStdOut != null)
        {
            var output = recordingStdOut.ReadNext();
            BotIntent.StdOut = output.Length > 0 ? output : null;
        }

        if (recordingStdErr != null)
        {
            var error = recordingStdErr.ReadNext();
            BotIntent.StdErr = error.Length > 0 ? error : null;
        }
    }

    private void RenderGraphicsToBotIntent()
    {
        if (CurrentTickOrThrow.BotState.IsDebuggingEnabled) {
            BotIntent.DebugGraphics = graphicsState.GetSvgOutput();
            graphicsState.Clear();
        }
    }
    
    private void WaitForNextTurn(int turnNumber)
    {
        // Most bot methods will call waitForNextTurn(), and hence this is a central place to stop a rogue thread that
        // cannot be killed any other way.
        StopRogueThread();

        lock (nextTurnMonitor)
        {
            while (
                IsRunning &&
                turnNumber == CurrentTickOrThrow.TurnNumber &&
                Thread.CurrentThread == thread
            )
            {
                Monitor.Wait(nextTurnMonitor);
            }
        }
    }

    private void StopRogueThread()
    {
        if (Thread.CurrentThread != thread)
        {
            Thread.CurrentThread.Interrupt();
        }
    }

    private void DispatchEvents(int turnNumber)
    {
        try
        {
            eventQueue.DispatchEvents(turnNumber);
        }
        catch (Exception e)
        {
            if (e is ThreadInterruptedException)
            {
                return;
            }

            Console.Error.WriteLine(e);
        }
    }

    internal string Variant => ServerHandshake.Variant;

    internal string Version => ServerHandshake.Version;

    internal int MyId { get; private set; }

    internal GameSetup GameSetup => gameSetup ?? throw new BotException(GameNotRunningMsg);

    internal S.BotIntent BotIntent { get; } = NewBotIntent();

    internal E.TickEvent CurrentTickOrThrow => tickEvent ?? throw new BotException(TickNotAvailableMsg);

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
        if (IsNaN(firepower)) throw new ArgumentException("'firepower' cannot be NaN");

        if (baseBot.Energy < firepower || CurrentTickOrThrow.BotState.GunHeat > 0)
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
                throw new ArgumentException("'TurnRate' cannot be NaN");
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
                throw new ArgumentException("'GunTurnRate' cannot be NaN");
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
                throw new ArgumentException("'RadarTurnRate' cannot be NaN");
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
                throw new ArgumentException("'TargetSpeed' cannot be NaN");
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
                throw new ArgumentException("'MaxTurnRate' cannot be NaN");
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
                throw new ArgumentException("'MaxGunTurnRate' cannot be NaN");
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
                throw new ArgumentException("'MaxRadarTurnRate' cannot be NaN");
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
                throw new ArgumentException("'MaxSpeed' cannot be NaN");
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

    internal void ClearConditions()
    {
        lock (conditions)
        {
            conditions.Clear();
        }
    }

    internal bool AddCondition(Events.Condition condition)
    {
        lock (conditions)
        {
            return conditions.Add(condition);
        }
    }

    internal bool RemoveCondition(Events.Condition condition)
    {
        return conditions.Remove(condition);
    }

    internal ImmutableHashSet<Events.Condition> Conditions => conditions.ToImmutableHashSet();

    internal void SetStop(bool overwrite)
    {
        if (!IsStopped || overwrite)
        {
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
    }

    internal void SetResume()
    {
        if (IsStopped)
        {
            BotIntent.TargetSpeed = savedTargetSpeed;
            BotIntent.TurnRate = savedTurnRate;
            BotIntent.GunTurnRate = savedGunTurnRate;
            BotIntent.RadarTurnRate = savedRadarTurnRate;

            stopResumeListener?.OnResume();
            IsStopped = false; // must be last step
        }
    }

    internal bool IsStopped { get; private set; }

    internal ICollection<int> TeammateIds => teammateIds ?? throw new BotException(GameNotRunningMsg);

    internal bool IsTeammate(int botId) => TeammateIds.Contains(botId);

    internal void BroadcastTeamMessage(object message) => SendTeamMessage(null, message);

    internal void SendTeamMessage(int? teammateId, object message)
    {
        if (teammateId != null && !TeammateIds.Contains((int)teammateId))
        {
            throw new ArgumentException("No teammate was found with the specified 'teammateId': " + teammateId);
        }

        if (BotIntent.TeamMessages is { Count: IBaseBot.MaxNumberOfTeamMessagesPerTurn })
            throw new InvalidOperationException(
                "The maximum number team massages has already been reached: " +
                IBaseBot.MaxNumberOfTeamMessagesPerTurn);

        var json = JsonConvert.SerializeObject(message);
        var bytes = System.Text.Encoding.UTF8.GetBytes(json);
        if (bytes.Length > IBaseBot.TeamMessageMaxSize)
            throw new ArgumentException(
                $"The team message is larger than the limit of {IBaseBot.TeamMessageMaxSize} bytes");

        BotIntent.TeamMessages.Add(new S.TeamMessage
        {
            MessageType = message.GetType().ToString(),
            Message = Convert.ToBase64String(bytes),
            ReceiverId = teammateId,
        });
    }

    internal int GetPriority(Type eventType)
    {
        if (!eventPriorities.TryGetValue(eventType, out var priority))
        {
            throw new InvalidOperationException($"Could not get event priority for the type: {eventType.Name}");
        }

        return priority;
    }

    internal void SetPriority(Type eventType, int priority) => eventPriorities[eventType] = priority;

    internal Color? BodyColor
    {
        get => tickEvent?.BotState.BodyColor;
        set => BotIntent.BodyColor = ToIntentColor(value);
    }

    internal Color? TurretColor
    {
        get => tickEvent?.BotState.TurretColor;
        set => BotIntent.TurretColor = ToIntentColor(value);
    }

    internal Color? RadarColor
    {
        get => tickEvent?.BotState.RadarColor;
        set => BotIntent.RadarColor = ToIntentColor(value);
    }

    internal Color? BulletColor
    {
        get => tickEvent?.BotState.BulletColor;
        set => BotIntent.BulletColor = ToIntentColor(value);
    }

    internal Color? ScanColor
    {
        get => tickEvent?.BotState.ScanColor;
        set => BotIntent.ScanColor = ToIntentColor(value);
    }

    internal Color? TracksColor
    {
        get => tickEvent?.BotState.TracksColor;
        set => BotIntent.TracksColor = ToIntentColor(value);
    }

    internal Color? GunColor
    {
        get => tickEvent?.BotState.GunColor;
        set => BotIntent.GunColor = ToIntentColor(value);
    }

    internal IGraphics Graphics => graphicsState.Graphics;
    
    private static string ToIntentColor(Color? color) => color == null ? null : "#" + ColorUtil.ToHex(color);

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
        BotEventHandlers.OnConnected.Publish(new E.ConnectedEvent(socket.ServerUri));

    private void HandleDisconnected(bool remote, int? statusCode, string reason)
    {
        var disconnectedEvent = new E.DisconnectedEvent(socket.ServerUri, remote, statusCode, reason);
        
        BotEventHandlers.OnDisconnected.Publish(disconnectedEvent);
        InstantEventHandlers.OnDisconnected.Publish(disconnectedEvent);

        closedEvent.Set();
    }

    private void HandleConnectionError(Exception cause)
    {
        BotEventHandlers.OnConnectionError.Publish(new E.ConnectionErrorEvent(socket.ServerUri,
            new Exception(cause.Message)));

        closedEvent.Set();
    }

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
        if (IsEventHandlingDisabled()) return;

        ticksStart = DateTime.Now.Ticks;

        var mappedTickEvent = EventMapper.Map(json, baseBot);
        eventQueue.AddEventsFromTick(mappedTickEvent);

        if (BotIntent.Rescan == true)
            BotIntent.Rescan = false;

        tickEvent = mappedTickEvent;
        
        foreach (var botEvent in tickEvent.Events)
        {
            InstantEventHandlers.FireEvent(botEvent);
        }

        // Trigger next turn (not tick-event!)
        InstantEventHandlers.OnNextTurn.Publish(tickEvent);
    }

    private void HandleRoundStarted(string json)
    {
        var roundStartedEvent = JsonConvert.DeserializeObject<S.RoundStartedEvent>(json);
        VerifyNotNull(roundStartedEvent, typeof(S.RoundStartedEvent));

        var mappedRoundStartedEvent = new E.RoundStartedEvent(roundStartedEvent.RoundNumber);
        
        BotEventHandlers.OnRoundStarted.Publish(mappedRoundStartedEvent);
        InstantEventHandlers.OnRoundStarted.Publish(mappedRoundStartedEvent);
    }

    private void HandleRoundEnded(string json)
    {
        var roundEndedEventForBot = JsonConvert.DeserializeObject<S.RoundEndedEventForBot>(json);
        VerifyNotNull(roundEndedEventForBot, typeof(S.RoundEndedEventForBot));

        var botResults = ResultsMapper.Map(roundEndedEventForBot.Results);

        var mappedRoundEndedEvent = new E.RoundEndedEvent(roundEndedEventForBot.RoundNumber,
            roundEndedEventForBot.TurnNumber, botResults);
        
        BotEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
        InstantEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
    }

    private void HandleGameStarted(string json)
    {
        var gameStartedEventForBot = JsonConvert.DeserializeObject<S.GameStartedEventForBot>(json);
        VerifyNotNull(gameStartedEventForBot, typeof(S.GameStartedEventForBot));

        MyId = gameStartedEventForBot.MyId;
        teammateIds = gameStartedEventForBot.TeammateIds;
        gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

        initialPosition = new InitialPosition(
            gameStartedEventForBot.StartX,
            gameStartedEventForBot.StartY,
            gameStartedEventForBot.StartDirection);

        // Send ready signal
        var ready = new S.BotReady
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotReady)
        };

        var msg = JsonConvert.SerializeObject(ready);
        socket.SendTextMessage(msg);

        BotEventHandlers.OnGameStarted.Publish(new E.GameStartedEvent(MyId, initialPosition, gameSetup));
    }

    private void HandleGameEnded(string json)
    {
        // Send the game ended event
        var gameEndedEventForBot = JsonConvert.DeserializeObject<S.GameEndedEventForBot>(json);
        VerifyNotNull(gameEndedEventForBot, typeof(S.GameEndedEventForBot));

        var results = ResultsMapper.Map(gameEndedEventForBot.Results);
        var mappedGameEnded = new E.GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results);
        
        BotEventHandlers.OnGameEnded.Publish(mappedGameEnded);
        InstantEventHandlers.OnGameEnded.Publish(mappedGameEnded);
    }

    private void HandleGameAborted()
    {
        BotEventHandlers.OnGameAborted.Publish(null);
        InstantEventHandlers.OnGameAborted.Publish(null);
    }

    private void HandleSkippedTurn(string json)
    {
        if (IsEventHandlingDisabled()) return;

        var skippedTurnEvent = JsonConvert.DeserializeObject<Schema.Game.SkippedTurnEvent>(json);

        BotEventHandlers.OnSkippedTurn.Publish(EventMapper.Map(skippedTurnEvent));
    }

    private void HandleServerHandshake(string json)
    {
        serverHandshake = JsonConvert.DeserializeObject<S.ServerHandshake>(json);

        // Reply by sending bot handshake
        var isDroid = baseBot is Droid;
        var botHandshake = BotHandshakeFactory.Create(serverHandshake?.SessionId, botInfo, isDroid, serverSecret);
        botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotHandshake);
        var text = JsonConvert.SerializeObject(botHandshake);

        socket.SendTextMessage(text);
    }

    private static void VerifyNotNull(Object iEvent, Type eventType)
    {
        if (iEvent == null)
            throw new BotException(nameof(eventType) + " is missing in JSON message from server");
    }
}