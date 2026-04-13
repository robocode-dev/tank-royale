import {Bot, Color, DeathEvent, ScannedBotEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// Corners
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// This robot moves to a corner, then rotates its gun back and forth
// scanning for enemies. If it performs poorly in a round, it will
// try a different corner in the next round.
// ------------------------------------------------------------------
class Corners extends Bot {
    enemies = 0; // Number of enemy bots in the game
    static corner = Corners.randomCorner(); // Which corner we are currently using
    stopWhenSeeEnemy = false; // See goCorner()

    static main() {
        new Corners().start();
    }

    // Called when a new round is started -> initialize and do some movement
    override run() {
        // Set colors
        this.setBodyColor(Color.RED);
        this.setTurretColor(Color.BLACK);
        this.setRadarColor(Color.YELLOW);
        this.setBulletColor(Color.GREEN);
        this.setScanColor(Color.GREEN);

        // Save # of other bots
        this.enemies = this.getEnemyCount();

        // Move to a corner
        this.goCorner();

        // Initialize gun turn speed to 3
        let gunIncrement = 3;

        // Spin gun back and forth
        while (this.isRunning()) {
            for (let i = 0; i < 30; i++) {
                this.turnGunLeft(gunIncrement);
            }
            gunIncrement *= -1;
        }
    }

    // Returns a random corner (0, 90, 180, 270)
    static randomCorner(): number {
        return 90 * Math.floor(Math.random() * 4);
    }

    // A very inefficient way to get to a corner.
    private goCorner() {
        // We don't want to stop when we're just turning...
        this.stopWhenSeeEnemy = false;
        // Turn to face the wall towards our desired corner
        this.turnLeft(this.calcBearing(Corners.corner));
        // Ok, now we don't want to crash into any bot in our way...
        this.stopWhenSeeEnemy = true;
        // Move to that wall
        this.forward(5000);
        // Turn to face the corner
        this.turnLeft(90);
        // Move to the corner
        this.forward(5000);
        // Turn gun to starting point
        this.turnGunLeft(90);
    }

    // We saw another bot -> stop and fire!
    override onScannedBot(e: ScannedBotEvent) {
        const distance = this.distanceTo(e.x, e.y);
        // Should we stop, or just fire?
        if (this.stopWhenSeeEnemy) {
            // Stop movement
            this.stop();
            // Call our custom firing method
            this.smartFire(distance);
            // Rescan for another bot
            this.rescan();
            // This line will not be reached when scanning another bot.
            // So we did not scan another bot -> resume movement
            this.resume();
        } else {
            this.smartFire(distance);
        }
    }

    // Custom fire method that determines firepower based on distance.
    private smartFire(distance: number) {
        if (distance > 200 || this.getEnergy() < 15) {
            this.fire(1);
        } else if (distance > 50) {
            this.fire(2);
        } else {
            this.fire(3);
        }
    }

    // We died -> figure out if we need to switch to another corner
    override onDeath(e: DeathEvent) {
        // Well, others should never be 0, but better safe than sorry.
        if (this.enemies === 0) {
            return;
        }
        // If 75% of the bots are still alive when we die, we'll switch corners.
        if (this.getEnemyCount() / this.enemies >= 0.75) {
            Corners.corner += 90; // Next corner
            Corners.corner %= 360; // Make sure the corner is within 0 - 359
            console.log("I died and did poorly... switching corner to " + Corners.corner);
        } else {
            console.log("I died but did well. I will still use corner " + Corners.corner);
        }
    }
}

Corners.main();
