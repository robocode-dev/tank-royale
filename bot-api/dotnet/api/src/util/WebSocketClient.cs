using System;
using System.IO;
using System.Net.WebSockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Robocode.TankRoyale.BotApi.Util;

/// <summary>
/// Client WebSocket class based on ClientWebSocket from System.Net.WebSockets which provides these delegate methods:
/// OnMessage(string message) and OnError(Exception ex). No need to call ReceiveAsync() in order to receive data.
/// </summary>
public class WebSocketClient
{
    /// <summary>Event called when the web socket got connected.</summary>
    public event HandleConnected OnConnected;

    /// <summary>Event called when the web socket got disconnected.</summary>
    public event HandleDisconnected OnDisconnected;

    /// <summary>Event called when an error occurred.</summary>
    public event HandleError OnError;

    /// <summary>Event called when a text message has been received.</summary>
    public event HandleTextMessage OnTextMessage;

    /// <summary>Event handler for OnConnected events</summary>
    public delegate void HandleConnected();

    /// <summary>Event handler for OnDisconnected events</summary>
    /// <param name="remote">true if the web socket was disconnected remotely by the server; false otherwise</param>
    /// <param name="statusCode">Is a status code that indicates the reason for closing the connection.</param>
    /// <param name="reason">Is a message with the reason for closing the connection.</param>
    public delegate void HandleDisconnected(bool remote, int? statusCode, string reason);

    /// <summary>Event handler for OnError events</summary>
    /// <param name="error">Is the error/exception that occurred</param>
    public delegate void HandleError(Exception error);

    /// <summary>Event handler for OnTextMessage events</summary>
    /// <param name="text">Is the received text message</param>
    public delegate void HandleTextMessage(string text);

    private readonly ClientWebSocket socket = new();

    public Uri ServerUri { get; }

    private readonly CancellationTokenSource cancelSource = new();

    /// <summary>Constructor.</summary>
    /// <param name="serverUri">Is the server URI</param>
    public WebSocketClient(Uri serverUri) => ServerUri = serverUri;

    /// <summary>Connect to the server.</summary>
    public void Connect()
    {
        socket.ConnectAsync(ServerUri, CancellationToken.None).GetAwaiter().GetResult();
        Task.Factory.StartNew(HandleIncomingMessages);
        OnConnected?.Invoke();
    }

    /// <summary>Disconnect from the server.</summary>
    public void Disconnect()
    {
        cancelSource.Cancel(); // signal that ReceiveAsync() should cancel
        socket.CloseOutputAsync(WebSocketCloseStatus.Empty, null /* when empty */, CancellationToken.None);
        OnDisconnected?.Invoke(false, (int)WebSocketCloseStatus.NormalClosure, "Bot disconnected");
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
            var buffer = new ArraySegment<byte>(new byte[8192]);

            while (!cancelSource.IsCancellationRequested)
            {
                using var ms = new MemoryStream();
                WebSocketReceiveResult result;
                do
                {
                    result = socket.ReceiveAsync(buffer, cancelSource.Token).GetAwaiter().GetResult();
                    if (result.MessageType == WebSocketMessageType.Close)
                    {
                        if (socket.CloseStatus != null)
                            OnDisconnected?.Invoke(true /* caused by remote */, (int)socket.CloseStatus,
                                socket.CloseStatusDescription);
                        return; // Cannot handle more messages when disconnected
                    }

                    if (buffer.Array != null) ms.Write(buffer.Array, buffer.Offset, result.Count);
                } while (!result.EndOfMessage);

                ms.Seek(0, SeekOrigin.Begin);

                using var reader = new StreamReader(ms, Encoding.UTF8);
                var text = reader.ReadToEnd();
                OnTextMessage?.Invoke(text);
            }
        }
        catch (Exception ex)
        {
            OnError?.Invoke(ex);
        }
    }
}