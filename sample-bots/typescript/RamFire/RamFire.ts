import {Bot, Color, HitBotEvent, ScannedBotEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// RamFire
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// This robot actively seeks out opponents, rams into them, and fires
// with appropriate power based on the enemy's remaining energy.
// ------------------------------------------------------------------
class RamFire extends Bot {
    turnDirection = 1; // clockwise (-1) or counterclockwise (1)

    static main() {
        new RamFire().start();
    }

    override run() {
        // Set colors
        this.setBodyColor(Color.fromRgb(0x99, 0x99, 0x99));   // lighter gray
        this.setTurretColor(Color.fromRgb(0x88, 0x88, 0x88)); // gray
        this.setRadarColor(Color.fromRgb(0x66, 0x66, 0x66));  // dark gray

        while (this.isRunning()) {
            this.turnRight(5 * this.turnDirection);
        }
    }

    // We scanned another bot -> go ram it
    override onScannedBot(e: ScannedBotEvent) {
        this.turnToFaceTarget(e.x, e.y);
        const distance = this.distanceTo(e.x, e.y);
        this.forward(distance + 5);
        this.rescan(); // Might want to move forward again!
    }

    // We have hit another bot -> turn to face bot, fire hard, and ram it again!
    override onHitBot(e: HitBotEvent) {
        this.turnToFaceTarget(e.x, e.y);
        // Determine a shot that won't kill the bot...
        // We want to ram him instead for bonus points
        if (e.energy > 16) {
            this.fire(3);
        } else if (e.energy > 10) {
            this.fire(2);
        } else if (e.energy > 4) {
            this.fire(1);
        } else if (e.energy > 2) {
            this.fire(0.5);
        } else if (e.energy > 0.4) {
            this.fire(0.1);
        }
        this.forward(40); // Ram him again!
    }

    // Method that turns the bot to face the target at coordinate x,y, but also sets the
    // default turn direction used if no bot is being scanned within in the run() method.
    private turnToFaceTarget(x: number, y: number) {
        const bearing = this.bearingTo(x, y);
        if (bearing >= 0) {
            this.turnDirection = 1;
        } else {
            this.turnDirection = -1;
        }
        this.turnLeft(bearing);
    }
}

RamFire.main();
