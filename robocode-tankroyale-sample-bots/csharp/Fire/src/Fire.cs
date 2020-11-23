using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// Fire - a sample bot, original version by Mathew Nelson for Robocode.
  /// Modified by Flemming N. Larsen.
  /// 
  /// This bot moves to a corner, then swings the gun back and forth. If it dies, it tries a new
  /// corner in the next round.
  /// </summary>
  public class Fire : Bot
  {
    int dist = 50; // Distance to move when we're hit, forward or back

    // Main method starts our bot
    static void Main(string[] args)
    {
      new Fire().Start();
    }

    // Constructor, which loads the bot settings file
    Fire() : base(BotInfo.FromJsonFile("fire-settings.json")) { }

    // This method runs our bot program, where each command is executed one at a time in a loop.
    public override void Run()
    {
      // Set colors
      SetBodyColor("#FA0"); // orange
      SetGunColor("##FA0"); // orange
      SetRadarColor("#F00"); // red
      SetBulletColor("#F00"); // red
      SetScanColor("#F00"); // red

      // Spin the gun around slowly... forever
      while (IsRunning)
      {
        TurnGunRight(5);
      }
    }

    // OnScannedBot: Fire!
    public override void OnScannedBot(ScannedBotEvent e)
    {
      // Set bot to stop movement (executed with next command - fire)
      SetStop();

      // If the other robot is close by, and we have plenty of life, fire hard!
      var distance = DistanceTo(e.X, e.Y);
      if (distance < 50 && Energy > 50)
      {
        Fire(3);
      }
      else
      {
        // Otherwise, only fire 1
        Fire(1);
      }
      // Scan, and resume movement if we did not scan anything
      if (!Scan())
      {
        SetResume();
      }
    }

    // OnHitByBullet: Turn perpendicular to the bullet, and move a bit.
    public override void OnHitByBullet(BulletHitBotEvent e)
    {
      // Set bot to resume movement, if it was stopped
      SetResume();

      // Turn perpendicular to the bullet direction
      var direction = DirectionTo(e.Bullet.X, e.Bullet.Y);
      TurnRight(NormalizeRelativeAngle(90 - (direction - Direction)));

      // Move forward or backward depending if the distance is positive or negative
      Forward(dist);
      dist *= -1; // Change distance, meaning forward or backward direction

      // Rescan
      SetScan();
    }

    // OnBulletHit: Aim at target (where bullet came from) and fire hard.
    public override void OnBulletHit(BulletHitBotEvent e)
    {
      // Set bot to resume movement, if it was stopped
      SetResume();

      // Turn gun to the bullet direction
      var bearing = BearingTo(e.Bullet.X, e.Bullet.Y);
      var gunBearing = NormalizeRelativeAngle(bearing + Direction - GunDirection);
      TurnGunLeft(gunBearing);

      // Fire hard
      Fire(3);
    }
  }
}