using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// MyFirstBot - a sample bot.
  ///
  /// Probably the first bot you will learn about.
  /// </summary>
  public class MyFirstBot : Bot
  {
    static void Main(string[] args)
    {
      new MyFirstBot().Start();
    }

    MyFirstBot() : base(BotInfo.FromJsonFile("bot-settings.json")) { }

    // This method runs our bot program, where each command is executed one at a time
    public override void Run()
    {
      // Repeat while bot is running
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