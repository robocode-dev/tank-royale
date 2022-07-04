using System;
using System.Collections.Generic;
using System.Threading;
using Fleck;
using Newtonsoft.Json;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils;

public class MockedServer
{
    public const int Port = 7913;
    
    private WebSocketServer _server;

    private readonly EventWaitHandle _openedEvent = new ManualResetEvent(false);
    private readonly EventWaitHandle _botHandshakeEvent = new ManualResetEvent(false);

    private BotHandshake _botHandshake;
    
    public void Start()
    {
        _server = new WebSocketServer($"ws://0.0.0.0:{Port}", false);
        _server.Start(socket =>
        {
            socket.OnOpen = () =>
            {
                _openedEvent.Set();

                var serverHandshake = new ServerHandshake
                {
                    Type = "ServerHandshake",
                    Name = nameof(MockedServer),
                    Version = "1.0.0",
                    Variant = "Tank Royale",
                    GameTypes = new List<string> { "melee", "classic", "1v1" }
                };

                socket.Send(JsonConvert.SerializeObject(serverHandshake));
            };
            socket.OnClose = socket.Close;

            socket.OnMessage = text =>
            {
                var message = JsonConvert.DeserializeObject<Message>(text);
                if (message != null)
                {
                    var msgType = (MessageType)Enum.Parse(typeof(MessageType), message.Type);
                    if (msgType == MessageType.BotHandshake)
                    {
                        _botHandshake = JsonConvert.DeserializeObject<BotHandshake>(text);
                        _botHandshakeEvent.Set();
                    }
                }
            };

            socket.OnError = error =>
                throw new InvalidOperationException("MockedServer error", error);
        });
    }

    public void Stop()
    {
        _server.Dispose();
    }

    public bool AwaitConnection(int milliSeconds)
    {
        return _openedEvent.WaitOne(milliSeconds);
    }

    public bool AwaitBotHandshake(int milliSeconds)
    {
        return _botHandshakeEvent.WaitOne(milliSeconds);
    }
    
    public BotHandshake GetBotHandshake() => _botHandshake;

    /*
    static void Main()
    {
        Console.WriteLine("Running MockedServer");
        
        MockedServer server = new();
        server.Start();
        
        Thread.Sleep(100_000);
    }*/
}