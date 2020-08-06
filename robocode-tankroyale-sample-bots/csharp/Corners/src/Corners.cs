using System;
using Robocode.TankRoyale.BotApi;

namespace Robocode.TankRoyale.Sample.Bots
{
  public class Corners : Bot
  {

    int enemies; // Number of enemy robots in the game
    int corner = 0; // Which corner we are currently using
    bool stopWhenSeeRobot = false; // See GoCorner()

    static void Main(string[] args)
    {
      new Corners().Start();
    }

    Corners() : base(BotInfo.FromJsonFile("corners-settings.json")) { }

    // This method runs our bot program, where each command is executed one at a time in a loop.
    public override void Run()
    {
      // Set colors
      SetBodyColor("#F00"); // red
      SetGunColor("#000"); // black
      SetRadarColor("#FF0"); // yellow
      SetBulletColor("#0F0"); // green
      SetScanColor("#0F0"); // green

      // Save # of other bots
      enemies = EnemyCount;

      // Move to a corner
      GoCorner();

      // Initialize gun turn speed to 3
      int gunIncrement = 3;

      // Spin gun back and forth
      while (IsRunning)
      {
        for (int i = 0; i < 30; i++)
        {
          TurnGunRight(gunIncrement);
        }
        gunIncrement *= -1;
      }
    }

    /** A very inefficient way to get to a corner. Can you do better? */
    private void GoCorner()
    {
      // We don't want to stop when we're just turning...
      stopWhenSeeRobot = false;

      // turn to face the wall towards our desired corner.
      TurnLeft(CalcBearing(corner));
      // Ok, now we don't want to crash into any robot in our way...
      stopWhenSeeRobot = true;
      // Move to that wall
      Forward(5000);
      // Turn to face the corner
      TurnRight(90);
      // Move to the corner
      Forward(5000);
      // Turn gun to starting point
      TurnGunRight(90);
    }

    /** We saw another bot. Stop and fire! */
    public override void OnScannedBot(ScannedBotEvent e)
    {
      var distance = DistanceTo(e.X, e.Y);

      // Should we stop, or just fire?
      if (stopWhenSeeRobot)
      {
        // Stop movement
        Stop();
        // Call our custom firing method
        SmartFire(distance);
        // Resume movement
        Resume();
      }
      else
      {
        SmartFire(distance);
      }
    }

    // Custom fire method that determines firepower based on distance. 
    // distance: The distance to the robot to fire at.
    private void SmartFire(double distance)
    {
      if (distance > 200 || Energy < 15)
      {
        Fire(1);
      }
      else if (distance > 50)
      {
        Fire(2);
      }
      else
      {
        Fire(3);
      }
    }

    // We died. Figure out if we need to switch to another corner.
    public override void OnDeath(BotDeathEvent e)
    {
      // Well, others should never be 0, but better safe than sorry.
      if (enemies == 0)
      {
        return;
      }

      // If 75% of the robots are still alive when we die, we'll switch corners.
      if ((enemies - EnemyCount) / (double)enemies < .75)
      {
        corner += 90;
        if (corner == 270)
        {
          corner = -90;
        }
        Console.WriteLine("I died and did poorly... switching corner to " + corner);
      }
      else
      {
        Console.WriteLine("I died but did well. I will still use corner " + corner);
      }
    }
  }
}