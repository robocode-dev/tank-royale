using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using AutoMapper;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Abstract Bot containing convenient methods for movement, turning, and firing the gun.
  /// Most bots should inherit from this class.
  /// </summary>
  public abstract class BaseBot : IBaseBot
  {
    internal readonly __Internals __internals;

    /// <summary>
    /// Constructor used when both BotInfo and server URI are provided through environment variables.
    /// This constructor should be used when starting up the bot using a bootstrap. These environment
    /// variables must be set to provide the server URI and bot information, and are automatically set
    /// by the bootstrap tool for Robocode. ROBOCODE_SERVER_URI, BOT_NAME, BOT_VERSION, BOT_AUTHOR,
    /// BOT_DESCRIPTION, BOT_COUNTRY_CODE, BOT_GAME_TYPES, BOT_PROG_LANG.
    /// </summary>
    /// <example>
    /// ROBOCODE_SERVER_URI=ws://localhost:55000
    /// BOT_NAME=MyBot
    /// BOT_VERSION=1.0
    /// BOT_AUTHOR=fnl
    /// /// BOT_DESCRIPTION=Sample bot
    /// BOT_COUNTRY_CODE=DK
    /// BOT_GAME_TYPES=melee,1v1
    /// BOT_PROG_LANG=Java
    /// </example>
    public BaseBot()
    {
      __internals = new __Internals(this, null, null);
    }

    /// <summary>
    /// Constructor used when server URI is provided through the environment variable
    /// ROBOCODE_SERVER_URI.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    public BaseBot(BotInfo botInfo)
    {
      __internals = new __Internals(this, botInfo, null);
    }

    /// <summary>
    /// Constructor used providing both the bot information and server URI for your bot.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUri">Is the server URI</param>
    public BaseBot(BotInfo botInfo, Uri serverUri)
    {
      __internals = new __Internals(this, botInfo, serverUri);
    }

    public void Start()
    {
      __internals.Connect();
    }

    public void Go()
    {
      __internals.SendBotIntent();
    }

    public String Variant
    {
      get => __internals.ServerHandshake.Variant;
    }

    public String Version
    {
      get => __internals.ServerHandshake.Version;
    }

    public int MyId
    {
      get => __internals.MyId;
    }

    public String GameType
    {
      get => __internals.GameSetup.GameType;
    }

    public int ArenaWidth
    {
      get => __internals.GameSetup.ArenaWidth;
    }

    public int ArenaHeight
    {
      get => __internals.GameSetup.ArenaHeight;
    }

    public int NumberOfRounds
    {
      get => __internals.GameSetup.NumberOfRounds;
    }

    public double GunCoolingRate
    {
      get => __internals.GameSetup.GunCoolingRate;
    }

    public int MaxInactivityTurns
    {
      get => __internals.GameSetup.MaxInactivityTurns;
    }

    public int TurnTimeout
    {
      get => __internals.GameSetup.TurnTimeout;
    }

    public int TimeLeft
    {
      get
      {
        long passesMicroSeconds = (DateTime.Now.Ticks - __internals.TicksStart) / 10;
        return (int)(__internals.GameSetup.TurnTimeout - passesMicroSeconds);
      }
    }

    public int RoundNumber
    {
      get => __internals.CurrentTurn.RoundNumber;
    }

    public int TurnNumber
    {
      get => __internals.CurrentTurn.TurnNumber;
    }

    public double Energy
    {
      get => __internals.CurrentTurn.BotState.Energy;
    }

    public bool IsDisabled
    {
      get => Energy == 0;
    }

    public double X
    {
      get => __internals.CurrentTurn.BotState.X;
    }

    public double Y
    {
      get => __internals.CurrentTurn.BotState.Y;
    }

    public double Direction
    {
      get => __internals.CurrentTurn.BotState.Direction;
    }

    public double GunDirection
    {
      get => __internals.CurrentTurn.BotState.GunDirection;
    }

    public double RadarDirection
    {
      get => __internals.CurrentTurn.BotState.RadarDirection;
    }

    public double Speed
    {
      get => __internals.CurrentTurn.BotState.Speed;
    }

    public double GunHeat
    {
      get => __internals.CurrentTurn.BotState.GunHeat;
    }

    public ICollection<BulletState> BulletStates
    {
      get => __internals.CurrentTurn.BulletStates;
    }

    public ICollection<Event> Events
    {
      get => __internals.CurrentTurn.Events;
    }

    public double TurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("TurnRate cannot be NaN");
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxTurnRate)
        {
          value = ((IBaseBot)this).MaxTurnRate * (value > 0 ? 1 : -1);
        }
        __internals.BotIntent.TurnRate = value;
      }
      get => __internals.BotIntent.TurnRate ?? 0d;
    }

    public double GunTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("GunTurnRate cannot be NaN");
        }
        if (IsAdjustGunForBodyTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxGunTurnRate)
        {
          value = ((IBaseBot)this).MaxGunTurnRate * (value > 0 ? 1 : -1);
        }
        __internals.BotIntent.GunTurnRate = value;
      }
      get => __internals.BotIntent.GunTurnRate ?? 0d;
    }

    public double RadarTurnRate
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("RadarTurnRate cannot be NaN");
        }
        if (IsAdjustRadarForGunTurn)
        {
          value -= value;
        }
        if (Math.Abs(value) > ((IBaseBot)this).MaxRadarTurnRate)
        {
          value = ((IBaseBot)this).MaxRadarTurnRate * (value > 0 ? 1 : -1);
        }
        __internals.BotIntent.RadarTurnRate = value;
      }
      get => __internals.BotIntent.RadarTurnRate ?? 0d;
    }

    public double TargetSpeed
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("TargetSpeed cannot be NaN");
        }
        if (value > ((IBaseBot)this).MaxForwardSpeed)
        {
          value = ((IBaseBot)this).MaxForwardSpeed;
        }
        else if (value < ((IBaseBot)this).MaxBackwardSpeed)
        {
          value = ((IBaseBot)this).MaxBackwardSpeed;
        }
        __internals.BotIntent.TargetSpeed = value;
      }
      get => __internals.BotIntent.TargetSpeed ?? 0d;
    }

    public double Firepower
    {
      set
      {
        if (Double.IsNaN(value))
        {
          throw new ArgumentException("Firepower cannot be NaN");
        }
        if (value < 0)
        {
          value = 0;
        }
        else if (value > ((IBaseBot)this).MaxFirepower)
        {
          value = ((IBaseBot)this).MaxFirepower;
        }
        __internals.BotIntent.Firepower = value;
      }
      get => __internals.BotIntent.Firepower ?? 0d;
    }

    public bool IsAdjustGunForBodyTurn
    {
      set => __internals.isAdjustGunForBodyTurn = value;
      get => __internals.isAdjustGunForBodyTurn;
    }

    public bool IsAdjustRadarForGunTurn
    {
      set => __internals.isAdjustRadarForGunTurn = value;
      get => __internals.isAdjustRadarForGunTurn;
    }

    public double CalcMaxTurnRate(double speed)
    {
      return ((IBaseBot)this).MaxTurnRate - 0.75 * Math.Abs(speed);
    }

    public double CalcBulletSpeed(double firepower)
    {
      return 20 - 3 * firepower;
    }

    public double CalcGunHeat(double firepower)
    {
      return 1 + (firepower / 5);
    }

    internal class __Internals
    {
      private const string NotConnectedToServerMsg =
          "Not connected to game server yes. Make sure onConnected() event handler has been called first";

      private const string GameNotRunningMsg =
          "Game is not running. Make sure onGameStarted() event handler has been called first";

      private const string TickNotAvailableMsg =
          "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

      private IBaseBot parent;

      private BotInfo botInfo;

      private BotIntent botIntent = new BotIntent();

      // Server connection
      private WebSocketClient socket;
      private ServerHandshake serverHandshake = null;

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

      // AutoMapper configuration
      private MapperConfiguration mapperConfig = new MapperConfiguration(cfg =>
      {
        cfg.CreateMap<Robocode.TankRoyale.Schema.GameSetup, Robocode.TankRoyale.BotApi.GameSetup>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BotResultsForBot, Robocode.TankRoyale.BotApi.BotResults>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BotDeathEvent, Robocode.TankRoyale.BotApi.BotDeathEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BotHitBotEvent, Robocode.TankRoyale.BotApi.BotHitBotEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BotHitWallEvent, Robocode.TankRoyale.BotApi.BotHitWallEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BulletFiredEvent, Robocode.TankRoyale.BotApi.BulletFiredEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BulletHitBotEvent, Robocode.TankRoyale.BotApi.BulletHitBotEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BulletHitBulletEvent, Robocode.TankRoyale.BotApi.BulletHitBulletEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.BulletHitWallEvent, Robocode.TankRoyale.BotApi.BulletHitWallEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.ScannedBotEvent, Robocode.TankRoyale.BotApi.ScannedBotEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.SkippedTurnEvent, Robocode.TankRoyale.BotApi.SkippedTurnEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.TickEventForBot, Robocode.TankRoyale.BotApi.TickEvent>();
        cfg.CreateMap<Robocode.TankRoyale.Schema.WonRoundEvent, Robocode.TankRoyale.BotApi.WonRoundEvent>();
      });
      private IMapper mapper;

      internal __Internals(IBaseBot parent, BotInfo botInfo, Uri serverUri)
      {
        this.parent = parent;
        this.botInfo = (botInfo == null) ? EnvVars.GetBotInfo() : botInfo;

        Init(serverUri);
      }

      private void Init(Uri serverUri)
      {
        socket = new WebSocketClient((serverUri == null) ? ServerUriFromSetting : serverUri);

        botIntent.Type = MessageType.BotIntent; // must be set

        mapper = mapperConfig.CreateMapper();

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

        onDeathManager.Add(parent.OnBotDeath); // DeathManager uses BotDeath events
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

      internal void SendBotIntent()
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
          var uri = EnvVars.GetServerUri();
          if (uri == null)
          {
            throw new BotException(String.Format("Environment variable {0} is not defined", EnvVars.SERVER_URI));
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
        OnConnected(new ConnectedEvent());
      }

      private void HandleDisconnected(bool remote)
      {
        OnDisconnected(new DisconnectedEvent(remote));
      }

      private void HandleConnectionError(Exception ex)
      {
        OnConnectionError(new ConnectionErrorEvent(ex));
      }

      private void HandleTextMessage(string json)
      {
        var jsonMsg = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
        var type = (string)jsonMsg["type"];
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

      private void HandleServerHandshake(string json)
      {
        var serverHandshake = JsonConvert.DeserializeObject<ServerHandshake>(json);

        // Reply by sending bot handshake
        var botHandshake = BotHandshakeFactory.Create(botInfo);
        var text = JsonConvert.SerializeObject(botHandshake);

        socket.SendTextMessage(text);
      }

      private void HandleGameStartedEvent(string json)
      {
        var gameStartedEventForBot = JsonConvert.DeserializeObject<GameStartedEventForBot>(json);

        myId = gameStartedEventForBot.MyId;
        gameSetup = mapper.Map<GameSetup>(gameStartedEventForBot.GameSetup);

        // Send ready signal
        BotReady ready = new BotReady();
        ready.Type = MessageType.BotReady;

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
        var results = mapper.Map<List<BotResults>>(gameEndedEventForBot.Results);

        var gameEndedEvent = new GameEndedEvent(gameEndedEventForBot.NumberOfRounds, results);
        OnGameEnded(gameEndedEvent);
      }

      private void HandleSkippedTurnEvent(string json)
      {
        var skippedTurnEvent = JsonConvert.DeserializeObject<SkippedTurnEvent>(json);
        OnSkippedTurn(mapper.Map<SkippedTurnEvent>(skippedTurnEvent));
      }

      private void HandleTickEvent(string json)
      {
        var tickEventForBot = JsonConvert.DeserializeObject<TickEventForBot>(json);
        currentTurn = mapper.Map<TickEvent>(tickEventForBot);

        // Dispatch all on the tick event before the tick event itself
        DispatchEvents(currentTurn);

        OnTick(currentTurn);
      }

      private void DispatchEvents(TickEvent tickEvent)
      {
        foreach (var te in tickEvent.Events)
        {
          switch (te)
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
          }
        }
      }
    }
  }
}