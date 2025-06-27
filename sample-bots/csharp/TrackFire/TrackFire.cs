using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Graphics;

// ------------------------------------------------------------------
// TrackFire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Sits still while tracking and firing at the nearest robot it
// detects.
// ------------------------------------------------------------------
public class TrackFire : Bot
{
    // The main method starts our bot
    static void Main(string[] args)
    {
        new TrackFire().Start();
    }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        // Set colors
        var pink = Color.FromRgb(0xFF, 0x69, 0xB4);
        BodyColor = pink;
        TurretColor = pink;
        RadarColor = pink;
        ScanColor = pink;
        BulletColor = pink;

        // Loop while running
        while (IsRunning)
        {
            TurnGunRight(10); // Scans automatically as radar is mounted on gun
        }
    }

    // We scanned another bot -> we have a target, so go get it
    public override void OnScannedBot(ScannedBotEvent e)
    {
        // Calculate direction of the scanned bot and bearing to it for the gun
        var bearingFromGun = GunBearingTo(e.X, e.Y);

        // Turn the gun toward the scanned bot
        TurnGunLeft(bearingFromGun);

        // If it is close enough, fire!
        if (Math.Abs(bearingFromGun) <= 3 && GunHeat == 0)
            Fire(Math.Min(3 - Math.Abs(bearingFromGun), Energy - .1));

        // Generates another scan event if we see a bot.
        // We only need to call this if the gun (and therefore radar)
        // are not turning. Otherwise, scan is called automatically.
        if (bearingFromGun == 0)
            Rescan();
    }

    // We won the round -> do a victory dance!
    public override void OnWonRound(WonRoundEvent e)
    {
        // Victory dance turning right 360 degrees 100 times
        TurnRight(36_000);
    }
}