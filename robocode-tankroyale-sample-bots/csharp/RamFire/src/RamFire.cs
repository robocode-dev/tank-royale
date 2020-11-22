using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// RamFire - a sample bot, original version by Mathew Nelson for Robocode.
  /// Modified by Flemming N. Larsen.
  /// 
  /// Drives at robots trying to ram them. Fires when it hits them.
  /// </summary>
  public class RamFire : Bot
  {
    int turnDirection = 1; // Clockwise (-1) or counterclockwise (1)

    // Main method starts our bot
    static void Main(string[] args)
    {
      new RamFire().Start();
    }

    // Constructor, which loads the bot settings file
    RamFire() : base(BotInfo.FromJsonFile("ramfire-settings.json")) { }

    // Run: Spin around looking for a target.
    public override void Run()
    {
      // Set colors
      SetBodyColor("#999"); // gray
      SetTurretColor("#888"); // gray
      SetRadarColor("#666"); // dark gray

      // Spin the gun around slowly... forever
      while (IsRunning)
      {
        TurnLeft(5 * turnDirection);
      }
    }

    /** OnScannedRobot: We have a target. Go ram it. */
    public override void OnScannedBot(ScannedBotEvent e)
    {
      TurnToFaceTarget(e.X, e.Y);

      Forward(DistanceTo(e.X, e.Y) + 5);
    }

    /** OnBulletHit: Turn to face robot, fire hard, and ram him again! */
    public override void OnHitBot(HitBotEvent e)
    {
      TurnToFaceTarget(e.X, e.Y);

      // Determine a shot that won't kill the robot...
      // We want to ram him instead for bonus points
      if (e.Energy > 16)
      {
        Fire(3);
      }
      else if (e.Energy > 10)
      {
        Fire(2);
      }
      else if (e.Energy > 4)
      {
        Fire(1);
      }
      else if (e.Energy > 2)
      {
        Fire(.5);
      }
      else if (e.Energy > .4)
      {
        Fire(.1);
      }
      Forward(40); // Ram him again!
    }

    /**
     * TurnToFaceTarget: Method that turns the bot to face the target at coordinate x,y, but also sets
     * the default turn direction used if no bot is being scanned within in the run() method.
     */
    private void TurnToFaceTarget(double x, double y)
    {
      double bearing = BearingTo(x, y);
      if (bearing >= 0)
      {
        turnDirection = 1;
      }
      else
      {
        turnDirection = -1;
      }
      TurnLeft(bearing);
    }
  }
}