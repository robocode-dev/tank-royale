using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// Walls - a sample bot, original version by Mathew Nelson for Robocode.
/// Modified by Flemming N. Larsen.
///
/// Moves around the outer edge with the gun facing in.
/// </summary>
namespace Robocode.TankRoyale.Sample.Bots
{
  public class Walls : Bot
  {
    bool peek; // Don't turn if there's a robot there
    double moveAmount; // How much to move

    bool stopMoving; // flag for stop moving temporarily

    static void Main_Walls()
    {
      new Walls().Start();
    }

    Walls() : base(BotInfo.FromJsonFile("walls-settings.json")) { }

    // Run: This method runs our bot program, where each command is executed one at a time in a loop.
    public override void Run()
    {
      // Set colors
      SetBodyColor("#000"); // black
      SetGunColor("#000"); // black
      SetRadarColor("#F90"); // orange
      SetTurretColor("#F90"); // orange
      SetBulletColor("#0FF"); // cyan

      // Initialize moveAmount to the maximum possible for the arena
      moveAmount = Math.Max(ArenaWidth, ArenaHeight);
      // Initialize peek to false
      peek = false;

      // turn to face a wall.
      // 'Direction % 90' means the remainder of Direction divided by 90.
      TurnRight(Direction % 90);
      Forward(moveAmount);

      // Turn the gun to turn right 90 degrees.
      peek = true;
      TurnGunRight(90);
      TurnRight(90);

      // Main loop
      while (IsRunning)
      {
        if (stopMoving)
        {
          Go(); // Do nothing this turn, but let the turn pass
        }
        else
        {
          // Peek before we turn when forward() completes.
          peek = true;
          // Move up the wall
          Forward(moveAmount);
          // Don't peek now
          peek = false;
          // Turn to the next wall
          TurnRight(90);
        }
      }
    }

    /** OnTick: Every new turn, reset/remove the interrupt */

    public override void OnTick(TickEvent e)
    {
      stopMoving = false; // Reset the stopMoving flag automatically each turn. Only OnScannedBot() will set it
    }

    /** OnHitBot: Move away a bit. */
    public override void OnHitBot(HitBotEvent e)
    {
      // If he's in front of us, set back up a bit.
      double bearing = BearingTo(e.X, e.Y);
      if (bearing > -90 && bearing < 90)
      {
        Back(100);
      }
      else
      { // else he's in back of us, so set ahead a bit.
        Forward(100);
      }
    }

    /** OnScannedBot: Fire! */
    public override void OnScannedBot(ScannedBotEvent e)
    {
      SetFire(2);

      if (peek)
      {
        stopMoving = true; // interrupt/stop turning the gun in the main loop in the Run() method
      }
    }
  }
}