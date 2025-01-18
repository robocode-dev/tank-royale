using System;
using System.Drawing;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// Corners
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This bot moves to a corner, then swings the gun back and forth.
// If it dies, it tries a new corner in the next round.
// ------------------------------------------------------------------
public class Corners : Bot
{
    int enemies; // Number of enemy bots in the game
    int corner = RandomCorner(); // Which corner we are currently using. Set to random corner
    bool stopWhenSeeEnemy = false; // See GoCorner()

    // The main method starts our bot
    static void Main(string[] args)
    {
        new Corners().Start();
    }

    // Constructor, which loads the bot config file
    Corners() : base(BotInfo.FromFile("Corners.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        // Set colors
        BodyColor = Color.Red;
        TurretColor = Color.Black;
        RadarColor = Color.Yellow;
        BulletColor = Color.Green;
        ScanColor = Color.Green;

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

    // Returns a random corner (0, 90, 180, 270)
    private static int RandomCorner()
    {
        return 90 * new Random().Next(4); // Random number is between 0-3
    }

    // A very inefficient way to get to a corner.
    // Can you do better as an home exercise? :)
    private void GoCorner()
    {
        // We don't want to stop when we're just turning...
        stopWhenSeeEnemy = false;
        // Turn to face the wall towards our desired corner
        TurnLeft(CalcBearing(corner));
        // Ok, now we don't want to crash into any bot in our way...
        stopWhenSeeEnemy = true;
        // Move to that wall
        Forward(5000);
        // Turn to face the corner
        TurnRight(90);
        // Move to the corner
        Forward(5000);
        // Turn gun to starting point
        TurnGunRight(90);
    }

    // We saw another bot -> stop and fire!
    public override void OnScannedBot(ScannedBotEvent e)
    {
        var distance = DistanceTo(e.X, e.Y);

        // Should we stop, or just fire?
        if (stopWhenSeeEnemy)
        {
            // Stop movement
            Stop();
            // Call our custom firing method
            SmartFire(distance);
            // Rescan for another bot
            Rescan();
            // This line will not be reached when scanning another bot.
            // So we did not scan another bot -> resume movement
            Resume();
        }
        else
            SmartFire(distance);
    }

    // Custom fire method that determines firepower based on distance.
    // distance: The distance to the bot to fire at.
    private void SmartFire(double distance)
    {
        if (distance > 200 || Energy < 15)
            Fire(1);
        else if (distance > 50)
            Fire(2);
        else
            Fire(3);
    }

    // We died -> figure out if we need to switch to another corner
    public override void OnDeath(DeathEvent e)
    {
        // Well, others should never be 0, but better safe than sorry.
        if (enemies == 0)
            return;

        // If 75% of the bots are still alive when we die, we'll switch corners.
        if (EnemyCount / (double)enemies >= .75)
        {
            corner += 90; // Next corner
            corner %= 360; // Make sure the corner is within 0 - 359

            Console.WriteLine("I died and did poorly... switching corner to " + corner);
        }
        else
            Console.WriteLine("I died but did well. I will still use corner " + corner);
    }
}