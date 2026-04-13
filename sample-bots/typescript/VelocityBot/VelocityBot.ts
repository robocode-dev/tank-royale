import {Bot, HitByBulletEvent, HitWallEvent, ScannedBotEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// VelocityBot
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Joshua Galecki.
//
// Example bot of how to use turn rates.
// ------------------------------------------------------------------
class VelocityBot extends Bot {
    turnCounter = 0;

    static main() {
        new VelocityBot().start();
    }

    override run() {
        this.turnCounter = 0;
        this.setGunTurnRate(15);

        while (this.isRunning()) {
            if (this.turnCounter % 64 === 0) {
                // Straighten out, if we were hit by a bullet (ends turning)
                this.setTurnRate(0);
                // Go forward with a target speed of 4
                this.setTargetSpeed(4);
            }
            if (this.turnCounter % 64 === 32) {
                // Go backwards, faster
                this.setTargetSpeed(-6);
            }
            this.turnCounter++;
            this.go(); // execute turn
        }
    }

    // We scanned another bot -> fire!
    override onScannedBot(e: ScannedBotEvent) {
        this.fire(1);
    }

    // We were hit by a bullet -> set turn rate
    override onHitByBullet(e: HitByBulletEvent) {
        // Turn to confuse the other bots
        this.setTurnRate(5);
    }

    // We hit a wall -> move in the opposite direction
    override onHitWall(e: HitWallEvent) {
        // Move away from the wall by reversing the target speed.
        // Note that current speed is 0 as the bot just hit the wall.
        this.setTargetSpeed(-1 * this.getTargetSpeed());
    }
}

VelocityBot.main();
