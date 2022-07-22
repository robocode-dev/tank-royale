import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// Crazy
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This bot moves around in a crazy pattern.
// ------------------------------------------------------------------
public class Crazy extends Bot {

    boolean movingForward;

    // The main method starts our bot
    public static void main(String[] args) {
        new Crazy().start();
    }

    // Constructor, which loads the bot config file
    Crazy() {
        super(BotInfo.fromFile("Crazy.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    public void run() {
        // Set colors
        setBodyColor(Color.fromString("#00C800"));   // lime
        setTurretColor(Color.fromString("#009632")); // green
        setRadarColor(Color.fromString("#006464"));  // dark cyan
        setBulletColor(Color.fromString("#FFFF64")); // yellow
        setScanColor(Color.fromString("#FFC8C8"));   // light red

        // Loop while as long as the bot is running
        while (isRunning()) {
            // Tell the game we will want to move ahead 40000 -- some large number
            setForward(40000);
            movingForward = true;
            // Tell the game we will want to turn right 90
            setTurnRight(90);
            // At this point, we have indicated to the game that *when we do something*,
            // we will want to move ahead and turn right. That's what "set" means.
            // It is important to realize we have not done anything yet!
            // In order to actually move, we'll want to call a method that takes real time, such as
            // waitFor.
            // waitFor actually starts the action -- we start moving and turning.
            // It will not return until we have finished turning.
            waitFor(new TurnCompleteCondition(this));
            // Note: We are still moving ahead now, but the turn is complete.
            // Now we'll turn the other way...
            setTurnLeft(180);
            // ... and wait for the turn to finish ...
            waitFor(new TurnCompleteCondition(this));
            // ... then the other way ...
            setTurnRight(180);
            // ... and wait for that turn to finish.
            waitFor(new TurnCompleteCondition(this));
            // then back to the top to do it all again.
        }
    }

    // We collided with a wall -> reverse the direction
    @Override
    public void onHitWall(HitWallEvent e) {
        // Bounce off!
        reverseDirection();
    }

    // ReverseDirection: Switch from ahead to back & vice versa
    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setForward(40000);
            movingForward = true;
        }
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }

    // We hit another bot -> back up!
    @Override
    public void onHitBot(HitBotEvent e) {
        // If we're moving into the other bot, reverse!
        if (e.isRammed()) {
            reverseDirection();
        }
    }

    // Condition that is triggered when the turning is complete
    public static class TurnCompleteCondition extends Condition {

        private final IBot bot;

        public TurnCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            // turn is complete when the remainder of the turn is zero
            return bot.getTurnRemaining() == 0;
        }
    }
}