using System;
using System.Collections.Generic;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Text.Json;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale
{
  public abstract class BaseBot : IBaseBot
  {
    readonly __Internals __internals;

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
      __internals = new __Internals(null, null);
    }

    /// <summary>
    /// Constructor used when server URI is provided through the environment variable
    /// ROBOCODE_SERVER_URI.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    public BaseBot(BotInfo botInfo)
    {
      __internals = new __Internals(botInfo, null);
    }

    /// <summary>
    /// Constructor used providing both the bot information and server URI for your bot.
    /// </summary>
    /// <param name="botInfo">Is the bot info containing information about your bot.</param>
    /// <param name="serverUri">Is the server URI</param>
    public BaseBot(BotInfo botInfo, Uri serverUri)
    {
      __internals = new __Internals(botInfo, serverUri);
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
      get
      {
        return __internals.ServerHandshake.Variant;
      }
    }

    public String Version
    {
      get
      {
        return __internals.ServerHandshake.Version;
      }
    }

    public int MyId
    {
      get
      {
        return __internals.MyId;
      }
    }

    public String GameType
    {
      get
      {
        return __internals.GameSetup.GameType;
      }
    }

    public int ArenaWidth
    {
      get
      {
        return __internals.GameSetup.ArenaWidth;
      }
    }

    public int ArenaHeight
    {
      get
      {
        return __internals.GameSetup.ArenaHeight;
      }
    }

    public int NumberOfRounds
    {
      get
      {
        return __internals.GameSetup.NumberOfRounds;
      }
    }

    public double GunCoolingRate
    {
      get
      {
        return __internals.GameSetup.GunCoolingRate;
      }
    }

    public int getMaxInactivityTurns
    {
      get
      {
        return __internals.GameSetup.MaxInactivityTurns;
      }
    }

    public int getTurnTimeout
    {
      get
      {
        return __internals.GameSetup.TurnTimeout;
      }
    }

    public int TimeLeft
    {
      get
      {
        long passesMicroSeconds = (DateTime.Now.Ticks - __internals.tickStart) / 10;
        return (int)(__internals.GameSetup.TurnTimeout - passesMicroSeconds);
      }
    }

    public int RoundNumber
    {
      get
      {
        return __internals.CurrentTurn.RoundNumber;
      }
    }

    public int TurnNumber
    {
      get
      {
        return __internals.CurrentTurn.TurnNumber;
      }
    }

    public double Energy
    {
      get
      {
        return __internals.CurrentTurn.BotState.Energy;
      }
    }

    public bool IsDisabled
    {
      get
      {
        return Energy == 0;
      }
    }

    public double X
    {
      get
      {
        return __internals.CurrentTurn.BotState.X;
      }
    }

    public double Y
    {
      get
      {
        return __internals.CurrentTurn.BotState.Y;
      }
    }

    public double Direction
    {
      get
      {
        return __internals.CurrentTurn.BotState.Direction;
      }
    }

    public double GunDirection
    {
      get
      {
        return __internals.CurrentTurn.BotState.GunDirection;
      }
    }

    public double RadarDirection
    {
      get
      {
        return __internals.CurrentTurn.BotState.RadarDirection;
      }
    }

    public double getSpeed
    {
      get
      {
        return __internals.CurrentTurn.BotState.Speed;
      }
    }

    public double getGunHeat
    {
      get
      {
        return __internals.CurrentTurn.BotState.GunHeat;
      }
    }

    public ICollection<BulletState> BulletStates
    {
      get
      {
        return __internals.CurrentTurn.BulletStates;
      }
    }

    public ICollection<Event> Events
    {
      get
      {
        return __internals.CurrentTurn.Events;
      }
    }


    internal class __Internals
    {
      private const string NotConnectedToServerMsg =
          "Not connected to game server yes. Make sure onConnected() event handler has been called first";

      private const string GameNotRunningMsg =
          "Game is not running. Make sure onGameStarted() event handler has been called first";

      private const string TickNotAvailableMsg =
          "Game is not running or tick has not occurred yet. Make sure onTick() event handler has been called first";

      private BotInfo botInfo;

      private BotIntent botIntent = new BotIntent();

      // Server connection
      private ClientWebSocket socket;
      private Uri serverUri;
      private ServerHandshake serverHandshake;

      // Current game states:
      private int? myId;
      private GameSetup gameSetup;
      private TickEvent currentTurn;
      private long tickStart = DateTime.Now.Ticks;

      internal __Internals(BotInfo botInfo, Uri serverUri)
      {
        this.botInfo = (botInfo == null) ? EnvVars.GetBotInfo() : botInfo;
        this.serverUri = (serverUri == null) ? ServerUriFromSetting : serverUri;
        socket = new ClientWebSocket();
      }

      internal void Connect()
      {
        if (socket.State != WebSocketState.Open)
        {
          try
          {
            socket.ConnectAsync(serverUri, CancellationToken.None);
          }
          catch (Exception ex)
          {
            throw new BotException("Could not connect to web socket", ex);
          }
        }
      }

      internal void SendBotIntent()
      {
        socket.SendAsync(Encoding.UTF8.GetBytes(JsonSerializer.Serialize(botIntent)), WebSocketMessageType.Text, true, CancellationToken.None);
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
    }
  }
}