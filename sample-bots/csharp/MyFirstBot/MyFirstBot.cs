using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// MyFirstBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion and spins the gun around at each end.
// ------------------------------------------------------------------
public class MyFirstBot : Bot
{
    // The main method starts our bot
    static void Main(string[] args)
    {
        new MyFirstBot().Start();
    }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        // Repeat while the bot is running
        while (IsRunning)
        {
            Forward(100);
            TurnGunLeft(360);
            Back(100);
            TurnGunLeft(360);
        }
    }

    // We saw another bot -> fire!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
        Fire(1);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    public override void OnHitByBullet(HitByBulletEvent evt)
    {
        // Calculate the bearing to the direction of the bullet
        var bearing = CalcBearing(evt.Bullet.Direction);

        // Turn 90 degrees to the bullet direction based on the bearing
        TurnRight(90 - bearing);
    }
}