using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Graphics;

// ------------------------------------------------------------------
// Fire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Sits still, continuously rotates its gun, and only moves when hit.
// ------------------------------------------------------------------
public class Fire : Bot
{
    int dist = 50; // Distance to move when we're hit, forward or back

    // The main method starts our bot
    static void Main(string[] args)
    {
        new Fire().Start();
    }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        // Set colors
        BodyColor = Color.FromRgb(0xFF, 0xAA, 0x00);   // orange
        GunColor = Color.FromRgb(0xFF, 0x77, 0x00);    // dark orange
        TurretColor = Color.FromRgb(0xFF, 0x77, 0x00); // dark orange
        RadarColor = Color.FromRgb(0xFF, 0x00, 0x00);  // red
        ScanColor = Color.FromRgb(0xFF, 0x00, 0x00);   // red
        BulletColor = Color.FromRgb(0x00, 0x88, 0xFF); // light blue

        // Spin the gun around slowly... forever
        while (IsRunning)
        {
            // Turn the gun a bit if the bot if the target speed is 0
            TurnGunRight(5);
        }
    }

    // We scanned another bot -> fire!
    public override void OnScannedBot(ScannedBotEvent e)
    {
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
        Rescan();
    }

    // We were hit by a bullet -> turn perpendicular to the bullet, and move a bit
    public override void OnHitByBullet(HitByBulletEvent e)
    {
        // Turn perpendicular to the bullet direction
        TurnLeft(NormalizeRelativeAngle(90 - (Direction - e.Bullet.Direction)));

        // Move forward or backward depending if the distance is positive or negative
        Forward(dist);
        dist *= -1; // Change distance, meaning forward or backward direction

        // Rescan
        Rescan();
    }

    // We have hit another bot -> aim at it and fire hard!
    public override void OnHitBot(HitBotEvent e)
    {
        // Turn gun to the bullet direction
        var direction = DirectionTo(e.X, e.Y);
        var gunBearing = NormalizeRelativeAngle(direction - GunDirection);
        TurnGunRight(gunBearing);

        // Fire hard
        Fire(3);
    }
}