import {Bot, Color, HitBotEvent, HitByBulletEvent, ScannedBotEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// Fire
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// Sits still, continuously rotates its gun, and only moves when hit.
// ------------------------------------------------------------------
class Fire extends Bot {
    dist = 50; // Distance to move when we're hit, forward or back

    static main() {
        new Fire().start();
    }

    override run() {
        // Set colors
        this.setBodyColor(Color.fromRgb(0xff, 0xaa, 0x00));   // orange
        this.setGunColor(Color.fromRgb(0xff, 0x77, 0x00));    // dark orange
        this.setTurretColor(Color.fromRgb(0xff, 0x77, 0x00)); // dark orange
        this.setRadarColor(Color.fromRgb(0xff, 0x00, 0x00));  // red
        this.setScanColor(Color.fromRgb(0xff, 0x00, 0x00));   // red
        this.setBulletColor(Color.fromRgb(0x00, 0x88, 0xff)); // light blue

        // Spin the gun around slowly... forever
        while (this.isRunning()) {
            this.turnGunRight(5);
        }
    }

    // We scanned another bot -> fire!
    override onScannedBot(e: ScannedBotEvent) {
        // If the other bot is close by, and we have plenty of life, fire hard!
        const distance = this.distanceTo(e.x, e.y);
        if (distance < 50 && this.getEnergy() > 50) {
            this.fire(3);
        } else {
            // Otherwise, only fire 1
            this.fire(1);
        }
        // Rescan
        this.rescan();
    }

    // We were hit by a bullet -> turn perpendicular to the bullet, and move a bit
    override onHitByBullet(e: HitByBulletEvent) {
        // Turn perpendicular to the bullet direction
        this.turnLeft(this.normalizeRelativeAngle(90 - (this.getDirection() - e.bullet.direction)));
        // Move forward or backward depending if the distance is positive or negative
        this.forward(this.dist);
        this.dist *= -1; // Change distance, meaning forward or backward direction
        // Rescan
        this.rescan();
    }

    // We have hit another bot -> aim at it and fire hard!
    override onHitBot(e: HitBotEvent) {
        // Turn gun to the bullet direction
        const direction = this.directionTo(e.x, e.y);
        const gunBearing = this.normalizeRelativeAngle(direction - this.getGunDirection());
        this.turnGunRight(gunBearing);
        // Fire hard
        this.fire(3);
    }
}

Fire.main();
