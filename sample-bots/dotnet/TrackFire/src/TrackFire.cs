using System;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.BotApi.Sample.Bots
{
  /// <summary>
  /// TrackFire - a sample bot, original version by Mathew Nelson for Robocode.
  /// Modified by Flemming N. Larsen.
  /// 
  /// Sits still. Tracks and fires at the nearest robot it sees.
  /// </summary>
  public class TrackFire : Bot
  {
    bool isScanning; // flag set when scanning

    // Main method starts our bot
    static void Main(string[] args)
    {
      new TrackFire().Start();
    }

    // Constructor, which loads the bot settings file
    TrackFire() : base(BotInfo.FromJsonFile("trackfire-settings.json")) { }

    // TrackFire's run method
    public override void Run()
    {
      isScanning = false; // reset scanning flag

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
        if (isScanning)
          Go(); // skip turn if we a scanning
        else
          TurnGunLeft(10); // Scans automatically as radar is mounted on gun
      }
    }

    // OnScannedRobot: We have a target. Go get it.
    public override void OnScannedBot(ScannedBotEvent e)
    {
      isScanning = true; // we started scanning

      // Calculate direction of the scanned bot and bearing to it for the gun
      double bearingFromGun = GunBearingTo(e.X, e.Y);

      // Turn the gun toward the scanned bot
      TurnGunLeft(bearingFromGun);

      // If it is close enough, fire!
      if (Math.Abs(bearingFromGun) <= 3 && GunHeat == 0)
      {
        Fire(Math.Min(3 - Math.Abs(bearingFromGun), Energy - .1));
      }

      // Generates another scan event if we see a robot.
      // We only need to call this if the gun (and therefore radar)
      // are not turning. Otherwise, scan is called automatically.
      if (bearingFromGun < 5)
        Scan();

      isScanning = false; // we stopped scanning
    }

    // OnWonRound: Do a victory dance!
    public override void OnWonRound(WonRoundEvent e)
    {
      // Victory dance turning right 360 degrees 100 times
      TurnLeft(36_000);
    }
  }
}