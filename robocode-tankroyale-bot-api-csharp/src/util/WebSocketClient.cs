using System;
using System.IO;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Robocode.TankRoyale.BotApi
{
  /// <summary>
  /// Client WebSocket class based on ClientWebSocket from System.Net.WebSockets which provides these delegate methods:
  /// OnMessage(string message) and OnError(Exception ex). No need to call ReceiveAsync() in order to receive data.
  /// </summary>
  public class WebSocketClient
  {
    /// <summary>Event called when the web socket got connected.</summary>
    public event OnConnectedHandler OnConnected;

    /// <summary>Event called when the web socket got disconnected.</summary>
    public event OnDisconnectedHandler OnDisconnected;

    /// <summary>Event called when an error occured.</summary>
    public event OnErrorHandler OnError;

    /// <summary>Event called when a text message has been recieved.</summary>
    public event OnTextMessageHandler OnTextMessage;

    /// <summary>Event handler for OnConnected events</summary>
    public delegate void OnConnectedHandler();

    /// <summary>Event handler for OnDisconnected events</summary>
    /// <param name="remove">true if the web socket was disconnected remotely by the server; false otherwise</param>
    public delegate void OnDisconnectedHandler(bool remote);

    /// <summary>Event handler for OnError events</summary>
    /// <param name="error">Is the error/exception that occurred</param>
    public delegate void OnErrorHandler(Exception error);

    /// <summary>Event handler for OnTextMessage events</summary>
    /// <param name="text">Is the recieved text message</param>
    public delegate void OnTextMessageHandler(string text);

    private ClientWebSocket socket = new ClientWebSocket();

    public Uri ServerUri { get; }

    private CancellationTokenSource cancelSource = new CancellationTokenSource();

    /// <summary>Constructor.</summary>
    /// <param name="uri">Is the server URI</param>
    public WebSocketClient(Uri serverUri) => ServerUri = serverUri;

    /// <summary>Connect to the server.</summary>
    public void Connect()
    {
      socket.Options.KeepAliveInterval = TimeSpan.FromSeconds(1);
      socket.ConnectAsync(ServerUri, CancellationToken.None).GetAwaiter().GetResult();
      Task.Factory.StartNew(HandleIncomingMessages);
      OnConnected();
    }

    /// <summary>Disconnect from the server.</summary>
    public void Disconnect()
    {
      cancelSource.Cancel(); // signal that ReceiveAsync() should cancel
      socket.CloseOutputAsync(WebSocketCloseStatus.Empty, null /* when empty */ , CancellationToken.None);
      OnDisconnected(false /* not remote */ );
    }

    /// <summary>Sends a text message to the server.</summary>
    /// <param name="text">Is the text to send.</param>
    public void SendTextMessage(string text)
    {
      socket.SendAsync(Encoding.UTF8.GetBytes(text), WebSocketMessageType.Text, true, CancellationToken.None);
    }

    private void HandleIncomingMessages()
    {
      try
      {
        ArraySegment<Byte> buffer = new ArraySegment<byte>(new Byte[8192]);
        WebSocketReceiveResult result = null;

        while (!cancelSource.IsCancellationRequested)
        {
          using (var ms = new MemoryStream())
          {
            do
            {
              result = socket.ReceiveAsync(buffer, cancelSource.Token).GetAwaiter().GetResult();
              ms.Write(buffer.Array, buffer.Offset, result.Count);
            }
            while (!result.EndOfMessage);

            ms.Seek(0, SeekOrigin.Begin);

            using (var reader = new StreamReader(ms, Encoding.UTF8))
            {
              string text = reader.ReadToEnd();
              OnTextMessage(text);
            }
          }
        }
      }
      catch (Exception ex)
      {
        OnError(ex);
      }
    }
  }
}