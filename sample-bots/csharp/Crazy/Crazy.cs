using System.Drawing;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// Crazy
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This bot moves around in a crazy pattern.
// ------------------------------------------------------------------
public class Crazy : Bot
{
    bool movingForward;

    // The main method starts our bot
    static void Main()
    {
        new Crazy().Start();
    }

    // Constructor, which loads the bot config file
    Crazy() : base(BotInfo.FromFile("Crazy.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        BodyColor = Color.FromArgb(0x00, 0xC8, 0x00);   // lime
        TurretColor = Color.FromArgb(0x00, 0x96, 0x32); // green
        RadarColor = Color.FromArgb(0x00, 0x64, 0x64);  // dark cyan
        BulletColor = Color.FromArgb(0xFF, 0xFF, 0x64); // yellow
        ScanColor = Color.FromArgb(0xFF, 0xC8, 0xC8);   // light red

        movingForward = true;

        // Loop while as long as the bot is running
        while (IsRunning)
        {
            // Tell the game we will want to move ahead 40000 -- some large number
            SetForward(40000);
            movingForward = true;
            // Tell the game we will want to turn right 90
            SetTurnRight(90);
            // At this point, we have indicated to the game that *when we do something*,
            // we will want to move ahead and turn right. That's what "Set" means.
            // It is important to realize we have not done anything yet!
            // In order to actually move, we'll want to call a method that takes real time, such as
            // WaitFor.
            // WaitFor actually starts the action -- we start moving and turning.
            // It will not return until we have finished turning.
            WaitFor(new TurnCompleteCondition(this));
            // Note: We are still moving ahead now, but the turn is complete.
            // Now we'll turn the other way...
            SetTurnLeft(180);
            // ... and wait for the turn to finish ...
            WaitFor(new TurnCompleteCondition(this));
            // ... then the other way ...
            SetTurnRight(180);
            // ... and wait for that turn to finish.
            WaitFor(new TurnCompleteCondition(this));
            // then back to the top to do it all again.
        }
    }

    // We collided with a wall -> reverse the direction
    public override void OnHitWall(HitWallEvent e)
    {
        // Bounce off!
        ReverseDirection();
    }

    // ReverseDirection: Switch from ahead to back & vice versa
    public void ReverseDirection()
    {
        if (movingForward)
        {
            SetBack(40000);
            movingForward = false;
        }
        else
        {
            SetForward(40000);
            movingForward = true;
        }
    }

    // We scanned another bot -> fire!
    public override void OnScannedBot(ScannedBotEvent e)
    {
        Fire(1);
    }

    // We hit another bot -> back up!
    public override void OnHitBot(HitBotEvent e)
    {
        // If we're moving into the other bot, reverse!
        if (e.IsRammed)
        {
            ReverseDirection();
        }
    }
}

// Condition that is triggered when the turning is complete
public class TurnCompleteCondition : Condition
{
    private readonly Bot bot;

    public TurnCompleteCondition(Bot bot)
    {
        this.bot = bot;
    }

    public override bool Test()
    {
        // turn is complete when the remainder of the turn is zero
        return bot.TurnRemaining == 0;
    }
}