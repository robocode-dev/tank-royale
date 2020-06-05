using System;
using System.Collections.Generic;
using System.Threading;
using Newtonsoft.Json;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.Schema;

public class Test
{
  public static void Main2(string[] args)
  {
    var socket = new WebSocketClient(new Uri("ws://localhost"));

    socket.OnError += (ex) =>
    {
      Console.Error.WriteLine(ex.ToString());
    };
    socket.OnTextMessage += (msg) =>
    {
      Dictionary<string, object> jsonObj = JsonConvert.DeserializeObject<Dictionary<string, object>>(msg);

      string type = (string)jsonObj["type"];

      Console.WriteLine("type: " + type);

      MessageType msgType = (MessageType)Enum.Parse(typeof(MessageType), type);
      switch (msgType)
      {
        case MessageType.ServerHandshake:
          Console.WriteLine("Bingo!");
          break;
        default:
          break;
      }

      if (type == MessageType.ServerHandshake.ToString())
      {

      }

      /*
            var serverHandshake = JsonConvert.DeserializeObject<ServerHandshake>(msg);

            Console.WriteLine(serverHandshake.Variant);*/
    };

    socket.Connect();
    Thread.Sleep(2000);
    socket.Disconnect();
  }
}