using System;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Text.Json;

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


    internal class __Internals
    {
      private BotInfo botInfo;

      private BotIntent botIntent = new BotIntent();

      // Server connection
      private ClientWebSocket socket;
      private Uri serverUri;

      internal __Internals(BotInfo botInfo, Uri serverUri)
      {
        this.botInfo = (botInfo == null) ? EnvVars.GetBotInfo() : botInfo;
        this.serverUri = (serverUri == null) ? GetServerUriFromSetting() : serverUri;
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

      private Uri GetServerUriFromSetting()
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
  }
}