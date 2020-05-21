using System;
using System.IO;
using Microsoft.Extensions.Configuration;
using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.Sample.Bots
{
  public class MyFirstBot : Bot
  {
    static void Main(string[] args)
    {
      new MyFirstBot().Start();
    }

    MyFirstBot() : base(BotInfo.FromJsonFile("bot-settings.json")) { }

    public override void OnConnected(ConnectedEvent evt)
    {
      Console.WriteLine("Connected to server");
    }

    public override void OnDisconnected(DisconnectedEvent evt)
    {
      Console.WriteLine("Disconnected from server");
    }

    // This method runs our bot program, where each command is executed one at a time
    public override void Run()
    {
      while (IsRunning)
      {
        Forward(100);
        TurnGunRight(360);
        Back(100);
        TurnGunRight(360);
      }
    }

    // This method is called when our bot has scanned another bot
    public override void OnScannedBot(ScannedBotEvent evt)
    {
      Firepower = 1; // Fire the cannon!
    }

    // This method is called when our bot is hit by a bullet
    public override void OnHitByBullet(BulletHitBotEvent evt)
    {
      // Calculate the bearing to the direction of the bullet
      double bearing = evt.Bullet.Direction - Direction;

      // Turn 90 degrees to the bullet direction based on the bearing
      SetTurnLeft(90 - bearing);
    }
  }
}