using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

// ------------------------------------------------------------------
// Crazy
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This robot moves around in a crazy pattern.
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
    private Crazy() : base(BotInfo.FromFile("Crazy.json")) { }

    // Called when a new round is started -> initialize and do movement
    public override void Run()
    {
        // Set colors
        SetBodyColor("#00C800");
        SetGunColor("#009632");
        SetRadarColor("#006464");
        SetBulletColor("#FFFF64");
        SetScanColor("#FFC8C8");

        movingForward = true;

        // Loop while as long as the bot is running
        while (IsRunning)
        {
            // Tell the game we will want to move ahead 40000 -- some large number
            SetForward(40000);
            SetBodyColor("#00F");
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
        // If we're moving into the other robot, reverse!
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