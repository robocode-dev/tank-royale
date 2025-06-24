import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

// ------------------------------------------------------------------
// TrackFire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Sits still while tracking and firing at the nearest robot it
// detects.
// ------------------------------------------------------------------
public class TrackFire extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new TrackFire().start();
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        var pink = Color.fromRgb(0xFF, 0x69, 0xB4);
        setBodyColor(pink);
        setTurretColor(pink);
        setRadarColor(pink);
        setScanColor(pink);
        setBulletColor(pink);

        // Loop while running
        while (isRunning()) {
            turnGunRight(10); // Scans automatically as radar is mounted on gun
        }
    }

    // We scanned another bot -> we have a target, so go get it
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Calculate direction of the scanned bot and bearing to it for the gun
        var bearingFromGun = gunBearingTo(e.getX(), e.getY());

        // Turn the gun toward the scanned bot
        turnGunLeft(bearingFromGun);

        // If it is close enough, fire!
        if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
        }

        // Rescan immediately to keep tracking the target bot
        rescan();
    }

    // We won the round -> do a victory dance!
    @Override
    public void onWonRound(WonRoundEvent e) {
        // Victory dance turning right 360 degrees 100 times
        turnRight(36_000);
    }
}