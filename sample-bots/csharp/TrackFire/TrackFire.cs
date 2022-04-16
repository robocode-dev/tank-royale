using System;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// TrackFire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Sits still. Tracks and fires at the nearest bot it sees.
// ------------------------------------------------------------------
public class TrackFire : Bot
{
    bool isScanning; // flag set when scanning

    // The main method starts our bot
    static void Main(string[] args)
    {
        new TrackFire().Start();
    }

    // Constructor, which loads the bot config file
    TrackFire() : base(BotInfo.FromFile("TrackFire.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        isScanning = false; // reset scanning flag

        // Set colors
        var pink = Color.FromString("#FF69B4");
        BodyColor = pink;
        TurretColor = pink;
        RadarColor = pink;
        ScanColor = pink;
        BulletColor = pink;

        // Loop while running
        while (IsRunning)
        {
            if (isScanning)
                Go(); // skip turn if we a scanning
            else
                TurnGunLeft(10); // Scans automatically as radar is mounted on gun
        }
    }

    // We scanned another bot -> we have a target, so go get it
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

        // Generates another scan event if we see a bot.
        // We only need to call this if the gun (and therefore radar)
        // are not turning. Otherwise, scan is called automatically.
        if (bearingFromGun < 5)
            Scan();

        isScanning = false; // we stopped scanning
    }

    // We won the round -> do a victory dance!
    public override void OnWonRound(WonRoundEvent e)
    {
        // Victory dance turning right 360 degrees 100 times
        TurnLeft(36_000);
    }
}