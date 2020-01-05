using System;
using System.IO;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Net.WebSockets;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Client WebSocket class based on ClientWebSocket from System.Net.WebSockets which provides these delegate methods:
  /// OnMessage(string message) and OnError(Exception ex). No need to call ReceiveAsync() in order to receive data.
  /// </summary>
  public class WebSocketClient
  {
    public delegate void OnErrorHandler(Exception ex);
    public delegate void OnMessageHandler(string message);

    public event OnErrorHandler OnError;
    public event OnMessageHandler OnMessage;

    private ClientWebSocket socket = new ClientWebSocket();
    private Uri uri;

    private CancellationTokenSource receiveCancelSource = new CancellationTokenSource();

    public WebSocketClient(Uri uri)
    {
      this.uri = uri;
    }

    public void Connect()
    {
      socket.ConnectAsync(uri, CancellationToken.None).GetAwaiter().GetResult();
      Task.Factory.StartNew(HandleIncomingMessages);
    }

    public void Disconnect()
    {
      receiveCancelSource.Cancel(); // signal that ReceiveAsync() should cancel
      socket.CloseOutputAsync(WebSocketCloseStatus.Empty, "Disconnected", CancellationToken.None);
    }

    public void SendMessage(string message)
    {
      socket.SendAsync(Encoding.UTF8.GetBytes(message), WebSocketMessageType.Text, true, CancellationToken.None);
    }

    private async void HandleIncomingMessages()
    {
      try
      {
        ArraySegment<Byte> buffer = new ArraySegment<byte>(new Byte[8192]);
        WebSocketReceiveResult result = null;

        while (!receiveCancelSource.IsCancellationRequested)
        {
          using (var ms = new MemoryStream())
          {
            do
            {
              result = await socket.ReceiveAsync(buffer, receiveCancelSource.Token);
              ms.Write(buffer.Array, buffer.Offset, result.Count);
            }
            while (!result.EndOfMessage);

            ms.Seek(0, SeekOrigin.Begin);

            using (var reader = new StreamReader(ms, Encoding.UTF8))
            {
              string message = reader.ReadToEnd();
              OnMessage(message);
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