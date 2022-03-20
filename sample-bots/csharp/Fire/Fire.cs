using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// Fire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This bot moves to a corner, then swings the gun back and forth.
// If it dies, it tries a new corner in the next round.
// ------------------------------------------------------------------
public class Fire : Bot
{
    int dist = 50; // Distance to move when we're hit, forward or back
    bool isScanning; // Flag indicating if OnScannedBot() handler is running

    // The main method starts our bot
    static void Main(string[] args)
    {
        new Fire().Start();
    }

    // Constructor, which loads the bot settings file
    Fire() : base(BotInfo.FromFile("Fire.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        isScanning = false; // Clear scanning flag for each new turn

        // Set colors
        BodyColor = Color.FromHex("FA0");   // orange
        GunColor = Color.FromHex("F70");    // dark orange
        TurretColor = Color.FromHex("F70"); // dark orange
        RadarColor = Color.FromHex("F00");  // red
        ScanColor = Color.FromHex("F00");   // red
        BulletColor = Color.FromHex("08F"); // light blue

        // Spin the gun around slowly... forever
        while (IsRunning)
        {

            if (isScanning)
            {
                // Skip a turn if the onScannedBot handler is running
                Go();
            }
            else
            {
                // Turn the gun a bit if the bot if the target speed is 0
                TurnGunLeft(5);
            }
        }
    }

    // We scanned another bot -> fire!
    public override void OnScannedBot(ScannedBotEvent e)
    {
        isScanning = true; // We are now scanning

        // If the other bot is close by, and we have plenty of life, fire hard!
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
        // Rescan
        Scan();

        isScanning = false; // We are not scanning any more
    }

    // We were hit by a bullet -> turn perpendicular to the bullet, and move a bit
    public override void OnHitByBullet(BulletHitBotEvent e)
    {
        // Turn perpendicular to the bullet direction
        TurnLeft(NormalizeRelativeAngle(90 - (Direction - e.Bullet.Direction)));

        // Move forward or backward depending if the distance is positive or negative
        Forward(dist);
        dist *= -1; // Change distance, meaning forward or backward direction

        // Rescan
        Scan();
    }

    // We have hit another bot -> aim at it and fire hard!
    public override void OnHitBot(HitBotEvent e)
    {
        // Turn gun to the bullet direction
        double direction = DirectionTo(e.X, e.Y);
        double gunBearing = NormalizeRelativeAngle(direction - GunDirection);
        TurnGunLeft(gunBearing);

        // Fire hard
        Fire(3);
    }
}