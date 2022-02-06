using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// SpinBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Moves in a circle, firing hard when an enemy is detected.
// ------------------------------------------------------------------
public class SpinBot : Bot
{
    // The main method starts our bot
    static void Main(string[] args)
    {
        new SpinBot().Start();
    }

    // Constructor, which loads the bot config file
    SpinBot() : base(BotInfo.FromFile("SpinBot.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        SetBodyColor("#00F"); // blue
        SetTurretColor("#00F"); // blue
        SetRadarColor("#000"); // black
        SetScanColor("#FF0"); // yellow

        // Repeat while the bot is running
        while (IsRunning)
        {
            // Tell the game that when we take move, we'll also want to turn right... a lot
            SetTurnLeft(10_000);
            // Limit our speed to 5
            SetMaxSpeed(5);
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
            TurnLeft(10);
        }
    }
}