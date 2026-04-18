using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Threading;
using S = Robocode.TankRoyale.Schema;
using E = Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.BotApi.Graphics;
using Robocode.TankRoyale.BotApi.Internal.Json;
using static System.Double;

namespace Robocode.TankRoyale.BotApi.Internal;

sealed class BaseBotInternals
{
    [DllImport("winmm.dll", EntryPoint = "timeBeginPeriod")]
    private static extern uint TimeBeginPeriod(uint uPeriod);

    private const string DefaultServerUrl = "ws://localhost:7654";

    private const string NotConnectedToServerMsg =
        "Not connected to a game server. Make sure OnConnected() event handler has been called first";

    private const string GameNotRunningMsg =
        "Game is not running. Make sure OnGameStarted() event handler has been called first";

    private const string TickNotAvailableMsg =
        "Game is not running or tick has not occurred yet. Make sure OnTick() event handler has been called first";

    private readonly string _serverSecret;
    private WebSocketClient _socket;
    private S.ServerHandshake _serverHandshake;
    private readonly EventWaitHandle _closedEvent = new ManualResetEvent(false);

    private readonly IBaseBot _baseBot;
    private readonly BotInfo _botInfo;

    private GameSetup _gameSetup;

    private InitialPosition _initialPosition;

    private E.TickEvent _tickEvent;
    private long? _ticksStart;

    private readonly EventQueue _eventQueue;

    private readonly object _nextTurnMonitor = new();

    private Thread _thread;

    private bool _isRunning;
    private readonly object _isRunningLock = new();

    private IStopResumeListener _stopResumeListener;

    private double _maxSpeed;
    private double _maxTurnRate;
    private double _maxGunTurnRate;
    private double _maxRadarTurnRate;

    private double? _savedTargetSpeed;
    private double? _savedTurnRate;
    private double? _savedGunTurnRate;
    private double? _savedRadarTurnRate;

    private readonly HashSet<Events.Condition> _conditions = new();

    private int _eventHandlingDisabledTurn;

    private TextWriter _originalStdOut;
    private TextWriter _originalStdErr;
    private RecordingTextWriter _recordingStdOut;
    private RecordingTextWriter _recordingStdErr;

    private ICollection<int> _teammateIds;

    private int _lastExecuteTurnNumber;

    private readonly GraphicsState _graphicsState = new();

    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUrl, string serverSecret)
    {
        if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
            TimeBeginPeriod(1); // Set 1 ms timer resolution — matches JVM and CPython behaviour on Windows

        _baseBot = baseBot;

        if (botInfo == null)
        {
            // use environment variables for configuration
            botInfo = EnvVars.GetBotInfo();
        }

        _botInfo = botInfo;

        BotEventHandlers = new BotEventHandlers(baseBot);
        InternalEventHandlers = new InternalEventHandlers();
        _eventQueue = new EventQueue(this, BotEventHandlers);

        _maxSpeed = Constants.MaxSpeed;
        _maxTurnRate = Constants.MaxTurnRate;
        _maxGunTurnRate = Constants.MaxGunTurnRate;
        _maxRadarTurnRate = Constants.MaxRadarTurnRate;

        _serverSecret = serverSecret ?? ServerSecretFromSetting;

        Init(serverUrl ?? ServerUrlFromSetting);
    }

    private void Init(Uri serverUrl)
    {
        RedirectStdOutAndStdErr();
        InitializeWebSocketClient(serverUrl);
        SubscribeToEvents();
    }

    private void RedirectStdOutAndStdErr()
    {
        _originalStdOut = Console.Out;
        _originalStdErr = Console.Error;
        _recordingStdOut = new RecordingTextWriter(_originalStdOut);
        _recordingStdErr = new RecordingTextWriter(_originalStdErr);

        Console.SetOut(_recordingStdOut);
        Console.SetError(_recordingStdErr);
    }

    private void RestoreStdOutAndStdErr()
    {
        if (_originalStdOut != null) Console.SetOut(_originalStdOut);
        if (_originalStdErr != null) Console.SetError(_originalStdErr);
    }

    private void InitializeWebSocketClient(Uri serverUrl)
    {
        _socket = new WebSocketClient(serverUrl);
        _socket.OnConnected += HandleConnected;
        _socket.OnDisconnected += HandleDisconnected;
        _socket.OnError += HandleConnectionError;
        _socket.OnTextMessage += HandleTextMessage;
    }

    private void SubscribeToEvents()
    {
        InternalEventHandlers.OnRoundStarted.Subscribe(OnRoundStarted, 100);
        InternalEventHandlers.OnNextTurn.Subscribe(OnNextTurn, 100);
        InternalEventHandlers.OnBulletFired.Subscribe(OnBulletFired, 100);
    }

    public bool IsRunning
    {
        get
        {
            lock (_isRunningLock)
            {
                return _isRunning;
            }
        }
        private set
        {
            lock (_isRunningLock)
            {
                _isRunning = value;
            }
        }
    }

    internal void StartThread(IBot bot)
    {
        EnableEventHandling(true); // reset on WebSocket thread — before new bot thread starts
        _thread = new Thread(() => CreateRunnable(bot));
        _thread.Start();
    }

    private void CreateRunnable(IBot bot)
    {
        IsRunning = true;
        try
        {
            // Block until the first tick arrives so Run() can safely access bot state
            // (e.g. RadarDirection). By the time PulseAll() fires we are guaranteed
            // that BotInternals.OnFirstTurn() (priority 110) has already called
            // ClearRemaining(), capturing the initial directions from the tick state.
            WaitUntilFirstTickArrived();
            bot.Run();
        }
        catch (ThreadInterruptedException)
        {
        }
        catch (Exception e)
        {
            Console.Error.WriteLine(e);
        }

        DispatchFinalTurnEvents();

        // Skip every turn after the run method has exited
        while (IsRunning)
        {
            try
            {
                bot.Go();
            }
            catch (ThreadInterruptedException)
            {
                break;
            }
        }

        DispatchFinalTurnEvents();
    }

    private void DispatchFinalTurnEvents()
    {
        var tick = CurrentTickOrNull;
        if (tick != null)
            DispatchEvents(tick.TurnNumber);
    }

    internal void AddEvent(E.BotEvent botEvent) => _eventQueue.AddEvent(botEvent);

    internal void StopThread()
    {
        if (!IsRunning)
            return;

        IsRunning = false;
        EnableEventHandling(false); // disable on WebSocket thread — prevents new ticks from queuing after bot stops

        if (_thread != null)
        {
            _thread.Interrupt();
            _thread = null;
        }
    }


    public void EnableEventHandling(bool enable)
    {
        _eventHandlingDisabledTurn = enable ? 0 : CurrentTickOrThrow.TurnNumber;
    }

    private bool IsEventHandlingDisabled()
    {
        // Important! Allow an additional turn so events like RoundStarted can be handled
        return _eventHandlingDisabledTurn != 0 && _eventHandlingDisabledTurn < (CurrentTickOrThrow.TurnNumber - 1);
    }

    public void SetStopResumeHandler(IStopResumeListener listener) => _stopResumeListener = listener;

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

    internal InternalEventHandlers InternalEventHandlers { get; }

    internal IList<E.BotEvent> Events => _eventQueue.Events(CurrentTickOrThrow.TurnNumber);

    internal void ClearEvents() => _eventQueue.ClearEvents();

    internal void SetInterruptible(bool interruptible) => _eventQueue.SetCurrentEventInterruptible(interruptible);

    private bool _movementResetPending;

    private void OnRoundStarted(E.RoundStartedEvent e)
    {
        _tickEvent = null;
        _eventQueue.Clear();
        IsStopped = false;
        _eventHandlingDisabledTurn = 0;
        _lastExecuteTurnNumber = -1;
        _movementResetPending = true; // defer movement reset until after first intent
    }

    private void OnNextTurn(E.TickEvent e)
    {
        lock (_nextTurnMonitor)
        {
            // Unblock methods waiting for the next turn
            Monitor.PulseAll(_nextTurnMonitor);
        }
    }

    private void OnBulletFired(E.BulletFiredEvent e) =>
        BotIntent.Firepower = 0; // Reset firepower so the bot stops firing continuously

    internal void Start()
    {
        Connect();
        _closedEvent.WaitOne();
    }

    private void Connect()
    {
        var serverUri = _socket.ServerUri;
        SanitizeUrl(serverUri);
        try
        {
            _socket.Connect();
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

    /// <param name="capturedTurnNumber">
    /// The turn number captured by Go() at the time events were dispatched, or -1 if no tick was available.
    /// </param>
    internal void Execute(int capturedTurnNumber)
    {
        // If no tick has been received yet, send current intent once so the server can proceed.
        if (capturedTurnNumber < 0)
        {
            SendIntent();
            return;
        }

        if (capturedTurnNumber != _lastExecuteTurnNumber)
        {
            _lastExecuteTurnNumber = capturedTurnNumber;
            SendIntent();

            if (_movementResetPending)
            {
                ResetMovement();
                _movementResetPending = false;
            }
        }

        WaitForNextTurn(capturedTurnNumber);
    }

    private void SendIntent()
    {
        RenderGraphicsToBotIntent();
        TransferStdOutToBotIntent();
        _socket.SendTextMessage(JsonConverter.ToJson(BotIntent));
        // Clear rescan after serialization — consumed by this intent
        if (BotIntent.Rescan == true)
            BotIntent.Rescan = false;
        BotIntent.TeamMessages.Clear();
    }

    internal void TransferStdOutToBotIntent()
    {
        if (_recordingStdOut != null)
        {
            var output = _recordingStdOut.ReadNext();
            BotIntent.StdOut = output.Length > 0 ? output : null;
        }

        if (_recordingStdErr != null)
        {
            var error = _recordingStdErr.ReadNext();
            BotIntent.StdErr = error.Length > 0 ? error : null;
        }
    }

    private void RenderGraphicsToBotIntent()
    {
        var currentTick = CurrentTickOrNull;
        if (currentTick != null && currentTick.BotState.IsDebuggingEnabled)
        {
            BotIntent.DebugGraphics = _graphicsState.GetSvgOutput();
            _graphicsState.Clear();
        }
    }

    private void WaitForNextTurn(int turnNumber)
    {
        // Most bot methods will call waitForNextTurn(), and hence this is a central place to stop a rogue thread that
        // cannot be killed any other way.
        StopRogueThread();

        lock (_nextTurnMonitor)
        {
            while (
                IsRunning &&
                turnNumber == CurrentTickOrThrow.TurnNumber &&
                Thread.CurrentThread == _thread
            )
            {
                Monitor.Wait(_nextTurnMonitor);
            }
        }
    }

    private void StopRogueThread()
    {
        if (Thread.CurrentThread != _thread)
        {
            throw new ThreadInterruptedException();
        }
    }

    // Blocks the pre-warmed bot thread until the first tick of the round arrives.
    // The thread is started at round-started (before any tick), so it must wait here
    // before Run() can safely read bot state (radar direction, etc.).
    // PulseAll() is called by OnNextTurn() (priority 100) after BotInternals.OnFirstTurn()
    // (priority 110) has already captured the initial directions via ClearRemaining().
    private void WaitUntilFirstTickArrived()
    {
        lock (_nextTurnMonitor)
        {
            while (IsRunning && CurrentTickOrNull == null)
            {
                Monitor.Wait(_nextTurnMonitor);
            }
        }
    }

    internal void DispatchEvents(int turnNumber)
    {
        try
        {
            _eventQueue.DispatchEvents(turnNumber);
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

    private int _myId;
    internal int MyId
    {
        get
        {
            if (_myId == 0)
            {
                throw new BotException(GameNotRunningMsg);
            }
            return _myId;
        }
        private set => _myId = value;
    }

    internal GameSetup GameSetup => _gameSetup ?? throw new BotException(GameNotRunningMsg);

    internal S.BotIntent BotIntent { get; } = NewBotIntent();

    internal E.TickEvent CurrentTickOrThrow => _tickEvent ?? throw new BotException(TickNotAvailableMsg);

    internal E.TickEvent CurrentTickOrNull => _tickEvent;

    internal int TimeLeft
    {
        get
        {
            if (_tickEvent == null)
                return GameSetup.TurnTimeout;

            var elapsedMicros = (Stopwatch.GetTimestamp() - _ticksStart.Value) * 1_000_000L / Stopwatch.Frequency;
            return Math.Max(0, (int)(_gameSetup.TurnTimeout - elapsedMicros));
        }
    }

    internal bool SetFire(double firepower)
    {
        IntentValidator.ValidateFirepower(firepower);

        if (_baseBot.Energy < firepower || CurrentTickOrThrow.BotState.GunHeat > 0)
            return false; // cannot fire yet
        BotIntent.Firepower = firepower;
        return true;
    }

    internal double GunHeat => _tickEvent == null ? 0 : _tickEvent.BotState.GunHeat;

    internal double Speed => _tickEvent == null ? 0 : _tickEvent.BotState.Speed;

    internal double TurnRate
    {
        get
        {
            // if the turn rate was modified during the turn
            if (BotIntent.TurnRate != null)
            {
                return (double)BotIntent.TurnRate;
            }

            return _tickEvent == null ? 0 : _tickEvent.BotState.TurnRate;
        }
        set
        {
            BotIntent.TurnRate = IntentValidator.ValidateTurnRate(value, _maxTurnRate);
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

            return _tickEvent == null ? 0 : _tickEvent.BotState.GunTurnRate;
        }
        set
        {
            BotIntent.GunTurnRate = IntentValidator.ValidateGunTurnRate(value, _maxGunTurnRate);
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

            return _tickEvent == null ? 0 : _tickEvent.BotState.RadarTurnRate;
        }
        set
        {
            BotIntent.RadarTurnRate = IntentValidator.ValidateRadarTurnRate(value, _maxRadarTurnRate);
        }
    }

    internal double TargetSpeed
    {
        get => BotIntent.TargetSpeed ?? 0d;
        set
        {
            BotIntent.TargetSpeed = IntentValidator.ValidateTargetSpeed(value, _maxSpeed);
        }
    }

    internal double MaxTurnRate
    {
        get => _maxTurnRate;
        set
        {
            _maxTurnRate = IntentValidator.ValidateMaxTurnRate(value);
        }
    }

    internal double MaxGunTurnRate
    {
        get => _maxGunTurnRate;
        set
        {
            _maxGunTurnRate = IntentValidator.ValidateMaxGunTurnRate(value);
        }
    }

    internal double MaxRadarTurnRate
    {
        get => _maxRadarTurnRate;
        set
        {
            _maxRadarTurnRate = IntentValidator.ValidateMaxRadarTurnRate(value);
        }
    }

    internal double MaxSpeed
    {
        get => _maxSpeed;
        set
        {
            _maxSpeed = IntentValidator.ValidateMaxSpeed(value);
        }
    }

    internal double GetNewTargetSpeed(double speed, double distance)
    {
        return IntentValidator.GetNewTargetSpeed(speed, distance, _maxSpeed);
    }

    internal double GetDistanceTraveledUntilStop(double speed)
    {
        return IntentValidator.GetDistanceTraveledUntilStop(speed, _maxSpeed);
    }

    internal void ClearConditions()
    {
        lock (_conditions)
        {
            _conditions.Clear();
        }
    }

    internal bool AddCondition(Events.Condition condition)
    {
        lock (_conditions)
        {
            return _conditions.Add(condition);
        }
    }

    internal bool RemoveCondition(Events.Condition condition)
    {
        return _conditions.Remove(condition);
    }

    internal ImmutableHashSet<Events.Condition> Conditions => _conditions.ToImmutableHashSet();

    internal void SetStop(bool overwrite)
    {
        if (!IsStopped || overwrite)
        {
            IsStopped = true;

            _savedTargetSpeed = BotIntent.TargetSpeed;
            _savedTurnRate = BotIntent.TurnRate;
            _savedGunTurnRate = BotIntent.GunTurnRate;
            _savedRadarTurnRate = BotIntent.RadarTurnRate;

            BotIntent.TargetSpeed = 0;
            BotIntent.TurnRate = 0;
            BotIntent.GunTurnRate = 0;
            BotIntent.RadarTurnRate = 0;

            _stopResumeListener?.OnStop();
        }
    }

    internal void SetResume()
    {
        if (IsStopped)
        {
            BotIntent.TargetSpeed = _savedTargetSpeed;
            BotIntent.TurnRate = _savedTurnRate;
            BotIntent.GunTurnRate = _savedGunTurnRate;
            BotIntent.RadarTurnRate = _savedRadarTurnRate;

            _stopResumeListener?.OnResume();
            IsStopped = false; // must be last step
        }
    }

    internal bool IsStopped { get; private set; }

    internal ICollection<int> TeammateIds => _teammateIds ?? throw new BotException(GameNotRunningMsg);

    internal bool IsTeammate(int botId) => TeammateIds.Contains(botId);

    internal void BroadcastTeamMessage(object message) => SendTeamMessage(null, message);

    internal void SendTeamMessage(int? teammateId, object message)
    {
        IntentValidator.ValidateTeammateId(teammateId, TeammateIds);
        IntentValidator.ValidateTeamMessage(message, BotIntent.TeamMessages?.Count ?? 0);

        var json = JsonConverter.ToJson(message);
        IntentValidator.ValidateTeamMessageSize(json);

        BotIntent.TeamMessages.Add(new S.TeamMessage
        {
            MessageType = message.GetType().ToString(),
            Message = json,
            ReceiverId = teammateId,
        });
    }

    internal Color? BodyColor
    {
        get => _tickEvent?.BotState.BodyColor;
        set => BotIntent.BodyColor = IntentValidator.ColorToHex(value);
    }

    internal Color? TurretColor
    {
        get => _tickEvent?.BotState.TurretColor;
        set => BotIntent.TurretColor = IntentValidator.ColorToHex(value);
    }

    internal Color? RadarColor
    {
        get => _tickEvent?.BotState.RadarColor;
        set => BotIntent.RadarColor = IntentValidator.ColorToHex(value);
    }

    internal Color? BulletColor
    {
        get => _tickEvent?.BotState.BulletColor;
        set => BotIntent.BulletColor = IntentValidator.ColorToHex(value);
    }

    internal Color? ScanColor
    {
        get => _tickEvent?.BotState.ScanColor;
        set => BotIntent.ScanColor = IntentValidator.ColorToHex(value);
    }

    internal Color? TracksColor
    {
        get => _tickEvent?.BotState.TracksColor;
        set => BotIntent.TracksColor = IntentValidator.ColorToHex(value);
    }

    internal Color? GunColor
    {
        get => _tickEvent?.BotState.GunColor;
        set => BotIntent.GunColor = IntentValidator.ColorToHex(value);
    }

    internal IGraphics Graphics => _graphicsState.Graphics;

    internal IEnumerable<BulletState> BulletStates => _tickEvent?.BulletStates ?? ImmutableHashSet<BulletState>.Empty;

    private S.ServerHandshake ServerHandshake
    {
        get
        {
            if (_serverHandshake == null)
            {
                throw new BotException(NotConnectedToServerMsg);
            }

            return _serverHandshake;
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
        BotEventHandlers.OnConnected.Publish(new E.ConnectedEvent(_socket.ServerUri));

    private void HandleDisconnected(bool remote, int? statusCode, string reason)
    {
        var disconnectedEvent = new E.DisconnectedEvent(_socket.ServerUri, remote, statusCode, reason);

        BotEventHandlers.OnDisconnected.Publish(disconnectedEvent);
        InternalEventHandlers.OnDisconnected.Publish(disconnectedEvent);

        RestoreStdOutAndStdErr();
        _closedEvent.Set();
    }

    private void HandleConnectionError(Exception cause)
    {
        BotEventHandlers.OnConnectionError.Publish(new E.ConnectionErrorEvent(_socket.ServerUri,
            new Exception(cause.Message)));

        RestoreStdOutAndStdErr();
        _closedEvent.Set();
    }

    private void HandleTextMessage(string json)
    {
        var jsonMsg = JsonConverter.FromJson<Dictionary<string, object>>(json);
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
        catch (Exception e)
        {
            HandleConnectionError(e);
        }
    }

    private void HandleTick(string json)
    {
        if (IsEventHandlingDisabled()) return;

        _ticksStart = Stopwatch.GetTimestamp();

        var mappedTickEvent = EventMapper.Map(json, _baseBot);
        _eventQueue.AddEventsFromTick(mappedTickEvent);

        _tickEvent = mappedTickEvent;

        foreach (var botEvent in _tickEvent.Events)
        {
            InternalEventHandlers.FireEvent(botEvent);
        }

        // Trigger next turn (not tick-event!)
        InternalEventHandlers.OnNextTurn.Publish(_tickEvent);
    }

    private void HandleRoundStarted(string json)
    {
        var roundStartedEvent = JsonConverter.FromJson<S.RoundStartedEvent>(json);
        VerifyNotNull(roundStartedEvent, typeof(S.RoundStartedEvent));

        var mappedRoundStartedEvent = new E.RoundStartedEvent(roundStartedEvent.RoundNumber);

        InternalEventHandlers.OnRoundStarted.Publish(mappedRoundStartedEvent);
        BotEventHandlers.OnRoundStarted.Publish(mappedRoundStartedEvent);
    }

    private void HandleRoundEnded(string json)
    {
        var roundEndedEventForBot = JsonConverter.FromJson<S.RoundEndedEventForBot>(json);
        VerifyNotNull(roundEndedEventForBot, typeof(S.RoundEndedEventForBot));

        var botResults = ResultsMapper.Map(roundEndedEventForBot.Results);

        var mappedRoundEndedEvent = new E.RoundEndedEvent(roundEndedEventForBot.RoundNumber,
            roundEndedEventForBot.TurnNumber, botResults);

        BotEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
        InternalEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent); // triggers StopThread()

        // Dispatch any queued events (e.g. WonRoundEvent from the last tick). Bot thread is now
        // stopped so there is no concurrent dispatch race. Must run before ROUND_STARTED clears
        // the event queue.
        DispatchEvents(mappedRoundEndedEvent.TurnNumber);

        // Transfer any remaining stdout/stderr from event handlers (e.g. OnWonRound) before the round ends
        TransferStdOutToBotIntent();
    }

    private void HandleGameStarted(string json)
    {
        var gameStartedEventForBot = JsonConverter.FromJson<S.GameStartedEventForBot>(json);
        VerifyNotNull(gameStartedEventForBot, typeof(S.GameStartedEventForBot));

        MyId = gameStartedEventForBot.MyId;
        _teammateIds = gameStartedEventForBot.TeammateIds;
        _gameSetup = GameSetupMapper.Map(gameStartedEventForBot.GameSetup);

        _initialPosition = new InitialPosition(
            gameStartedEventForBot.StartX,
            gameStartedEventForBot.StartY,
            gameStartedEventForBot.StartDirection);

        BotEventHandlers.OnGameStarted.Publish(new E.GameStartedEvent(MyId, _initialPosition, _gameSetup));

        // Send ready signal
        var ready = new S.BotReady
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotReady)
        };

        var msg = JsonConverter.ToJson(ready);
        _socket.SendTextMessage(msg);
    }

    private void HandleGameEnded(string json)
    {
        // Send the game ended event
        var gameEndedEventForBot = JsonConverter.FromJson<S.GameEndedEventForBot>(json);
        VerifyNotNull(gameEndedEventForBot, typeof(S.GameEndedEventForBot));

        var results = ResultsMapper.Map(gameEndedEventForBot.Results);
        var mappedGameEnded = new E.GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results);

        BotEventHandlers.OnGameEnded.Publish(mappedGameEnded);
        InternalEventHandlers.OnGameEnded.Publish(mappedGameEnded);
    }

    private void HandleGameAborted()
    {
        BotEventHandlers.OnGameAborted.Publish(null);
        InternalEventHandlers.OnGameAborted.Publish(null);
    }

    private void HandleSkippedTurn(string json)
    {
        var skippedTurnEvent = JsonConverter.FromJson<Schema.SkippedTurnEvent>(json);

        _eventQueue.AddEvent(EventMapper.Map(skippedTurnEvent));
    }

    private void HandleServerHandshake(string json)
    {
        _serverHandshake = JsonConverter.FromJson<S.ServerHandshake>(json);

        // Validate bot info before sending bot handshake
        ValidateBotInfo();

        // Reply by sending bot handshake
        var isDroid = _baseBot is Droid;
        var botHandshake = BotHandshakeFactory.Create(_serverHandshake?.SessionId, _botInfo, isDroid, _serverSecret);
        botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotHandshake);
        var text = JsonConverter.ToJson(botHandshake);

        _socket.SendTextMessage(text);
    }

    private void ValidateBotInfo()
    {
        if (string.IsNullOrWhiteSpace(_botInfo.Name))
        {
            ThrowMissingPropertyException("name");
        }
        if (string.IsNullOrWhiteSpace(_botInfo.Version))
        {
            ThrowMissingPropertyException("version");
        }
        if (_botInfo.Authors.IsNullOrEmptyOrContainsOnlyBlanks())
        {
            ThrowMissingPropertyException("authors");
        }
    }

    private void ThrowMissingPropertyException(string propertyName)
    {
        throw new BotException(
            $"Required bot property '{propertyName}' is missing. " +
            "This property is required in order for the bot to be recognized when booting it up and " +
            "when it needs to join the game. You must set this property in your bot code " +
            "or provide a .json configuration file.");
    }

    private static void VerifyNotNull(Object iEvent, Type eventType)
    {
        if (iEvent == null)
            throw new BotException(nameof(eventType) + " is missing in JSON message from server");
    }
}
