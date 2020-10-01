using Robocode.TankRoyale.BotApi;

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

    bool interrupt; // Flag for stop turning the gun temporarily

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
        if (interrupt)
        {
          // Stop turning gun if interrupted
          TurnGunRight(0);
        }
        else
        {
          // Else turn gun 5 more degrees to the right
          TurnGunRight(5);
        }
      }
    }

    // OnTick: Every new turn, reset/remove the interrupt
    public override void OnTick(TickEvent e)
    {
      interrupt = false; // no interrupt means that the gun will turn 5 degrees to the right in the run() method
    }

    // OnScannedBot: Fire!
    public override void OnScannedBot(ScannedBotEvent e)
    {
      // If the other robot is close by, and we have plenty of life, fire hard!
      double distance = DistanceTo(e.X, e.Y);
      if (distance < 50 && Energy > 50)
      {
        Fire(3);
      }
      else
      {
        // Otherwise, only fire 1
        Fire(1);
      }
      interrupt = true; // interrupt/stop turning the gun in the main loop in the run() method
    }

    // OnHitByBullet: Turn perpendicular to the bullet, and move a bit.
    public override void OnHitByBullet(BulletHitBotEvent e)
    {
      // Turn perpendicular to the bullet direction
      TurnRight(NormalizeRelativeDegrees(90 - (e.Bullet.Direction - Direction)));

      // Move forward or backward depending if the distance is positive or negative
      Forward(dist);
      dist *= -1; // Change distance, meaning forward or backward direction

      interrupt = true; // interrupt/stop turning the gun in the main loop in the run() method
    }

    // OnBulletHit: Aim at target (where bullet came from) and fire hard.
    public override void OnBulletHit(BulletHitBotEvent e)
    {
      // Turn gun to the bullet direction
      double gunBearing = NormalizeRelativeDegrees(e.Bullet.Direction - GunDirection);
      TurnGunLeft(gunBearing);

      // Fire hard
      Fire(3);
    }
  }
}