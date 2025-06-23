using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;
using Robocode.TankRoyale.BotApi.Graphics;

using System.IO;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Configuration.Json;

// ------------------------------------------------------------------
// SpinBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Continuously moves in a circle while firing at maximum power when
// detecting enemies.
// ------------------------------------------------------------------
public class SpinBot : Bot
{
    // The main method starts our bot
    static void Main(string[] args)
    {
        new SpinBot().Start();
    }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        BodyColor = Color.Blue;
        TurretColor = Color.Blue;
        RadarColor = Color.Black;
        ScanColor = Color.Yellow;

        // Repeat while the bot is running
        while (IsRunning)
        {
            // Tell the game that when we take move, we'll also want to turn right... a lot
            SetTurnRight(10_000);
            // Limit our speed to 5
            MaxSpeed = 5;
            // Start moving (and turning)
            Forward(10_000);
        }
    }

    // We scanned another bot -> fire hard!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
        Fire(3);
    }

    // We hit another bot -> if it's our fault, we'll stop turning and moving,
    // so we need to turn again to keep spinning.
    public override void OnHitBot(HitBotEvent e)
    {
        var bearing = BearingTo(e.X, e.Y);
        if (bearing > -10 && bearing < 10)
        {
            Fire(3);
        }
        if (e.IsRammed)
        {
            TurnRight(10);
        }
    }
}