import {Bot, Color, ScannedBotEvent, WonRoundEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// TrackFire
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// Sits still while tracking and firing at the nearest robot it
// detects.
// ------------------------------------------------------------------
class TrackFire extends Bot {
    static main() {
        new TrackFire().start();
    }

    override run() {
        // Set colors
        const pink = Color.fromRgb(0xff, 0x69, 0xb4);
        this.setBodyColor(pink);
        this.setTurretColor(pink);
        this.setRadarColor(pink);
        this.setScanColor(pink);
        this.setBulletColor(pink);

        // Loop while running
        while (this.isRunning()) {
            this.turnGunRight(10); // Scans automatically as radar is mounted on gun
        }
    }

    // We scanned another bot -> we have a target, so go get it
    override onScannedBot(e: ScannedBotEvent) {
        // Calculate direction of the scanned bot and bearing to it for the gun
        const bearingFromGun = this.gunBearingTo(e.x, e.y);
        // Turn the gun toward the scanned bot
        this.turnGunLeft(bearingFromGun);
        // If it is close enough, fire!
        if (Math.abs(bearingFromGun) <= 3 && this.getGunHeat() === 0) {
            this.fire(Math.min(3 - Math.abs(bearingFromGun), this.getEnergy() - 0.1));
        }
        // Rescan immediately to keep tracking the target bot
        this.rescan();
    }

    // We won the round -> do a victory dance!
    override onWonRound(e: WonRoundEvent) {
        // Victory dance turning right 360 degrees 100 times
        this.turnRight(36000);
    }
}

TrackFire.main();
