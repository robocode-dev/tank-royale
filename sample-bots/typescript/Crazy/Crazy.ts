import {
    Bot,
    Color,
    Condition,
    HitBotEvent,
    HitWallEvent,
    IBot,
    ScannedBotEvent
} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// Crazy
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// This robot moves in a zigzag pattern while firing at enemies.
// ------------------------------------------------------------------
class Crazy extends Bot {
    movingForward = false;

    static main() {
        new Crazy().start();
    }

    override run() {
        // Set colors
        this.setBodyColor(Color.fromRgb(0x00, 0xc8, 0x00));   // lime
        this.setTurretColor(Color.fromRgb(0x00, 0x96, 0x32)); // green
        this.setRadarColor(Color.fromRgb(0x00, 0x64, 0x64));  // dark cyan
        this.setBulletColor(Color.fromRgb(0xff, 0xff, 0x64)); // yellow
        this.setScanColor(Color.fromRgb(0xff, 0xc8, 0xc8));   // light red

        // Loop while as long as the bot is running
        while (this.isRunning()) {
            // Tell the game we will want to move ahead 40000 -- some large number
            this.setForward(40000);
            this.movingForward = true;
            // Tell the game we will want to turn left 90
            this.setTurnLeft(90);
            // waitFor actually starts the action -- we start moving and turning.
            // It will not return until we have finished turning.
            this.waitFor(new TurnCompleteCondition(this));
            // Now we'll turn the other way...
            this.setTurnRight(180);
            // ... and wait for the turn to finish ...
            this.waitFor(new TurnCompleteCondition(this));
            // ... then the other way ...
            this.setTurnLeft(180);
            // ... and wait for that turn to finish.
            this.waitFor(new TurnCompleteCondition(this));
        }
    }

    // We collided with a wall -> reverse the direction
    override onHitWall(e: HitWallEvent) {
        this.reverseDirection();
    }

    // ReverseDirection: Switch from ahead to back & vice versa
    reverseDirection() {
        if (this.movingForward) {
            this.setBack(40000);
            this.movingForward = false;
        } else {
            this.setForward(40000);
            this.movingForward = true;
        }
    }

    // We scanned another bot -> fire!
    override onScannedBot(e: ScannedBotEvent) {
        this.fire(1);
    }

    // We hit another bot -> back up!
    override onHitBot(e: HitBotEvent) {
        // If we're moving into the other bot, reverse!
        if (e.isRammed) {
            this.reverseDirection();
        }
    }
}

// Condition that is triggered when the turning is complete
class TurnCompleteCondition extends Condition {
    private readonly bot: IBot;

    constructor(bot: IBot) {
        super("turnComplete");
        this.bot = bot;
    }

    override test(): boolean {
        // turn is complete when the remainder of the turn is zero
        return this.bot.getTurnRemaining() === 0;
    }
}

Crazy.main();
