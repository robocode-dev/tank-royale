using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.Sample.Bots
{
  /// <summary>
  /// TrackFire - a sample bot, original version by Mathew Nelson for Robocode.
  /// Modified by Flemming N. Larsen.
  /// 
  /// Sits still. Tracks and fires at the nearest robot it sees.
  /// </summary>
  public class TrackFire : Bot
  {
    // Last time we scanned
    int lastScanTurn;

    // Main method starts our bot
    static void Main_TrackFire(string[] args)
    {
      new TrackFire().Start();
    }

    // Constructor, which loads the bot settings file
    TrackFire() : base(BotInfo.FromJsonFile("trackfire-settings.json")) { }

    // TrackFire's run method
    public override void Run()
    {
      lastScanTurn = -1; // Reset last scan turn

      // Set colors
      var pink = "#FF69B4";
      SetBodyColor(pink);
      SetTurretColor(pink);
      SetRadarColor(pink);
      SetScanColor(pink);
      SetBulletColor(pink);

      // Loop while running
      while (IsRunning)
      {
        // Make sure we are at least one turn from last scanning turn before turning the gun
        if (TurnNumber - lastScanTurn > 1)
        {
          TurnGunLeft(10); // Scans automatically as radar is mounted on gun
        }
        Go(); // Skip next turn if we are doing nothing else (e.g. scanning)
      }
    }

    // OnScannedRobot: We have a target. Go get it.
    public override void OnScannedBot(ScannedBotEvent e)
    {
      // Save the turn number of this scan
      lastScanTurn = e.TurnNumber;

      // Calculate direction of the scanned bot and bearing to it for the gun
      double direction = DirectionTo(e.X, e.Y);
      double bearingFromGun = NormalizeRelativeAngle(direction - GunDirection);

      // Turn the gun toward the scanned bot
      TurnGunLeft(bearingFromGun);

      // If it is close enough, fire!
      if (Math.Abs(bearingFromGun) <= 3)
      {
        // We check gun heat here, because calling Fire() uses a turn,
        // which could cause us to lose track of the other bot.
        if (GunHeat == 0)
        {
          Fire(Math.Min(3 - Math.Abs(bearingFromGun), Energy - .1));
        }
      }
      // Generates another scan event if we see a robot.
      // We only need to call this if the gun (and therefore radar) are not turning
      // as the radar does not scan if it is not being turned.
      if (bearingFromGun == 0)
      {
        Scan();
      }
    }

    // OnWonRound: Do a victory dance!
    public override void OnWonRound(WonRoundEvent e)
    {
      // Victory dance turning right 360 degrees 100 times
      TurnLeft(36000);
    }
  }
}