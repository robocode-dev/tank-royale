using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

/// <summary>
/// SpinBot - a sample bot, original version by Mathew Nelson for Robocode.
///
/// Moves in a circle, firing hard when an enemy is detected.
/// </summary>
public class SpinBot : Bot
{
    static void Main(string[] args)
    {
        new SpinBot().Start();
    }

    SpinBot() : base(BotInfo.FromFile("SpinBot.json")) { }

    // This method runs our bot program, where each command is executed one at a time
    public override void Run()
    {
        SetBodyColor("#00F"); // blue
        SetTurretColor("#00F"); // blue
        SetRadarColor("#000"); // black
        SetScanColor("#FF0"); // yellow

        // Repeat while the bot is running
        while (IsRunning)
        {
            // Tell the game that when we take move, we'll also want to turn right... a lot.
            SetTurnLeft(10_000);
            // Limit our speed to 5
            SetMaxSpeed(5);
            // Start moving (and turning)
            Forward(10_000);
        }
    }

    // OnScannedBot: Fire hard when scanning another bot!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
        Fire(3); // Fire the cannon!
    }

    // OnHitBot: If it's our fault, we'll stop turning and moving, so we need to turn again to keep spinning.
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