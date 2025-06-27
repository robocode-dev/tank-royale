using System;
using System.Collections.Generic;
using System.Collections.Immutable;
using System.Linq;
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

    private readonly double _absDeceleration;

    private readonly HashSet<Events.Condition> _conditions = new();

    private int _eventHandlingDisabledTurn;

    private RecordingTextWriter _recordingStdOut;
    private RecordingTextWriter _recordingStdErr;

    private ICollection<int> _teammateIds;

    private int _lastExecuteTurnNumber;

    private readonly GraphicsState _graphicsState = new();

    internal BaseBotInternals(IBaseBot baseBot, BotInfo botInfo, Uri serverUrl, string serverSecret)
    {
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

        _absDeceleration = Math.Abs(Constants.Deceleration);

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
        _recordingStdOut = new RecordingTextWriter(Console.Out);
        _recordingStdErr = new RecordingTextWriter(Console.Error);

        Console.SetOut(_recordingStdOut);
        Console.SetError(_recordingStdErr);
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
        _thread = new Thread(() => CreateRunnable(bot));
        _thread.Start();
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

    private BotEventHandlers BotEventHandlers { get; }

    internal InternalEventHandlers InternalEventHandlers { get; }

    internal IList<E.BotEvent> Events => _eventQueue.Events(CurrentTickOrThrow.TurnNumber);

    internal void ClearEvents() => _eventQueue.ClearEvents();

    internal void SetInterruptible(bool interruptible) => _eventQueue.SetCurrentEventInterruptible(interruptible);

    private void OnRoundStarted(E.RoundStartedEvent e)
    {
        ResetMovement();
        _eventQueue.Clear();
        IsStopped = false;
        _eventHandlingDisabledTurn = 0;
        _lastExecuteTurnNumber = -1;
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

    internal void Execute()
    {
        if (!IsRunning)
            return;

        var turnNumber = CurrentTickOrThrow.TurnNumber;
        if (turnNumber != _lastExecuteTurnNumber)
        {
            _lastExecuteTurnNumber = turnNumber;

            SendIntent();
        }

        WaitForNextTurn(turnNumber);
    }

    private void SendIntent()
    {
        RenderGraphicsToBotIntent();
        TransferStdOutToBotIntent();
        _socket.SendTextMessage(JsonConverter.ToJson(BotIntent));
        BotIntent.TeamMessages.Clear();
    }

    private void TransferStdOutToBotIntent()
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
        if (CurrentTickOrThrow.BotState.IsDebuggingEnabled)
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
            Thread.CurrentThread.Interrupt();
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

    internal int MyId { get; private set; }

    internal GameSetup GameSetup => _gameSetup ?? throw new BotException(GameNotRunningMsg);

    internal S.BotIntent BotIntent { get; } = NewBotIntent();

    internal E.TickEvent CurrentTickOrThrow => _tickEvent ?? throw new BotException(TickNotAvailableMsg);

    internal E.TickEvent CurrentTickOrNull => _tickEvent;

    private long TicksStart
    {
        get
        {
            if (_ticksStart == null) throw new BotException(TickNotAvailableMsg);
            return (long)_ticksStart;
        }
    }

    internal int TimeLeft
    {
        get
        {
            var passesMicroSeconds = (DateTime.Now.Ticks - TicksStart) / 10;
            return (int)(_gameSetup.TurnTimeout - passesMicroSeconds);
        }
    }

    internal bool SetFire(double firepower)
    {
        if (IsNaN(firepower)) throw new ArgumentException("'firepower' cannot be NaN");

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
            if (IsNaN(value))
            {
                throw new ArgumentException("'TurnRate' cannot be NaN");
            }

            BotIntent.TurnRate = Math.Clamp(value, -_maxTurnRate, _maxTurnRate);
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
            if (IsNaN(value))
            {
                throw new ArgumentException("'GunTurnRate' cannot be NaN");
            }

            BotIntent.GunTurnRate = Math.Clamp(value, -_maxGunTurnRate, _maxGunTurnRate);
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
            if (IsNaN(value))
            {
                throw new ArgumentException("'RadarTurnRate' cannot be NaN");
            }

            BotIntent.RadarTurnRate = Math.Clamp(value, -_maxRadarTurnRate, _maxRadarTurnRate);
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

            BotIntent.TargetSpeed = Math.Clamp(value, -_maxSpeed, _maxSpeed);
        }
    }

    internal double MaxTurnRate
    {
        get => _maxTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("'MaxTurnRate' cannot be NaN");
            }

            _maxTurnRate = Math.Clamp(value, 0, Constants.MaxTurnRate);
        }
    }

    internal double MaxGunTurnRate
    {
        get => _maxGunTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("'MaxGunTurnRate' cannot be NaN");
            }

            _maxGunTurnRate = Math.Clamp(value, 0, Constants.MaxGunTurnRate);
        }
    }

    internal double MaxRadarTurnRate
    {
        get => _maxRadarTurnRate;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("'MaxRadarTurnRate' cannot be NaN");
            }

            _maxRadarTurnRate = Math.Clamp(value, 0, Constants.MaxRadarTurnRate);
        }
    }

    internal double MaxSpeed
    {
        get => _maxSpeed;
        set
        {
            if (IsNaN(value))
            {
                throw new ArgumentException("'MaxSpeed' cannot be NaN");
            }

            _maxSpeed = Math.Clamp(value, 0, Constants.MaxSpeed);
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

        var targetSpeed = IsPositiveInfinity(distance) ? _maxSpeed : Math.Min(GetMaxSpeed(distance), _maxSpeed);

        return speed >= 0
            ? Math.Clamp(targetSpeed, speed - _absDeceleration, speed + Constants.Acceleration)
            : Math.Clamp(targetSpeed, speed - Constants.Acceleration, speed + GetMaxDeceleration(-speed));
    }

    private double GetMaxSpeed(double distance)
    {
        var decelerationTime =
            Math.Max(1, Math.Ceiling((Math.Sqrt((4 * 2 / _absDeceleration) * distance + 1) - 1) / 2));
        if (IsPositiveInfinity(decelerationTime))
            return Constants.MaxSpeed;

        var decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * _absDeceleration;
        return ((decelerationTime - 1) * _absDeceleration) + ((distance - decelerationDistance) / decelerationTime);
    }

    private double GetMaxDeceleration(double speed)
    {
        var decelerationTime = speed / _absDeceleration;
        var accelerationTime = 1 - decelerationTime;

        return Math.Min(1, decelerationTime) * _absDeceleration +
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
        if (teammateId != null && !TeammateIds.Contains((int)teammateId))
        {
            throw new ArgumentException("No teammate was found with the specified 'teammateId': " + teammateId);
        }

        if (BotIntent.TeamMessages is { Count: IBaseBot.MaxNumberOfTeamMessagesPerTurn })
            throw new InvalidOperationException(
                "The maximum number team massages has already been reached: " +
                IBaseBot.MaxNumberOfTeamMessagesPerTurn);

        var json = JsonConverter.ToJson(message);
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

    internal Color? BodyColor
    {
        get => _tickEvent?.BotState.BodyColor;
        set => BotIntent.BodyColor = ToIntentColor(value);
    }

    internal Color? TurretColor
    {
        get => _tickEvent?.BotState.TurretColor;
        set => BotIntent.TurretColor = ToIntentColor(value);
    }

    internal Color? RadarColor
    {
        get => _tickEvent?.BotState.RadarColor;
        set => BotIntent.RadarColor = ToIntentColor(value);
    }

    internal Color? BulletColor
    {
        get => _tickEvent?.BotState.BulletColor;
        set => BotIntent.BulletColor = ToIntentColor(value);
    }

    internal Color? ScanColor
    {
        get => _tickEvent?.BotState.ScanColor;
        set => BotIntent.ScanColor = ToIntentColor(value);
    }

    internal Color? TracksColor
    {
        get => _tickEvent?.BotState.TracksColor;
        set => BotIntent.TracksColor = ToIntentColor(value);
    }

    internal Color? GunColor
    {
        get => _tickEvent?.BotState.GunColor;
        set => BotIntent.GunColor = ToIntentColor(value);
    }

    internal IGraphics Graphics => _graphicsState.Graphics;

    private static string ToIntentColor(Color? color) => color == null ? null : "#" + ColorUtil.ToHex(color);

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

        _closedEvent.Set();
    }

    private void HandleConnectionError(Exception cause)
    {
        BotEventHandlers.OnConnectionError.Publish(new E.ConnectionErrorEvent(_socket.ServerUri,
            new Exception(cause.Message)));

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
        catch (KeyNotFoundException)
        {
            Console.Error.WriteLine(jsonMsg);

            throw new BotException($"'type' is missing on the JSON message: {json}");
        }
    }

    private void HandleTick(string json)
    {
        if (IsEventHandlingDisabled()) return;

        _ticksStart = DateTime.Now.Ticks;

        var mappedTickEvent = EventMapper.Map(json, _baseBot);
        _eventQueue.AddEventsFromTick(mappedTickEvent);

        if (BotIntent.Rescan == true)
            BotIntent.Rescan = false;

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

        BotEventHandlers.OnRoundStarted.Publish(mappedRoundStartedEvent);
        InternalEventHandlers.OnRoundStarted.Publish(mappedRoundStartedEvent);
    }

    private void HandleRoundEnded(string json)
    {
        var roundEndedEventForBot = JsonConverter.FromJson<S.RoundEndedEventForBot>(json);
        VerifyNotNull(roundEndedEventForBot, typeof(S.RoundEndedEventForBot));

        var botResults = ResultsMapper.Map(roundEndedEventForBot.Results);

        var mappedRoundEndedEvent = new E.RoundEndedEvent(roundEndedEventForBot.RoundNumber,
            roundEndedEventForBot.TurnNumber, botResults);

        BotEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
        InternalEventHandlers.OnRoundEnded.Publish(mappedRoundEndedEvent);
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

        // Send ready signal
        var ready = new S.BotReady
        {
            Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotReady)
        };

        var msg = JsonConverter.ToJson(ready);
        _socket.SendTextMessage(msg);

        BotEventHandlers.OnGameStarted.Publish(new E.GameStartedEvent(MyId, _initialPosition, _gameSetup));
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
        if (IsEventHandlingDisabled()) return;

        var skippedTurnEvent = JsonConverter.FromJson<Schema.SkippedTurnEvent>(json);

        BotEventHandlers.OnSkippedTurn.Publish(EventMapper.Map(skippedTurnEvent));
    }

    private void HandleServerHandshake(string json)
    {
        _serverHandshake = JsonConverter.FromJson<S.ServerHandshake>(json);

        // Reply by sending bot handshake
        var isDroid = _baseBot is Droid;
        var botHandshake = BotHandshakeFactory.Create(_serverHandshake?.SessionId, _botInfo, isDroid, _serverSecret);
        botHandshake.Type = EnumUtil.GetEnumMemberAttrValue(S.MessageType.BotHandshake);
        var text = JsonConverter.ToJson(botHandshake);

        _socket.SendTextMessage(text);
    }

    private static void VerifyNotNull(Object iEvent, Type eventType)
    {
        if (iEvent == null)
            throw new BotException(nameof(eventType) + " is missing in JSON message from server");
    }
}