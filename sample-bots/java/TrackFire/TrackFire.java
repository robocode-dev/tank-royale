import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// TrackFire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Sits still. Tracks and fires at the nearest bot it sees.
// ------------------------------------------------------------------
public class TrackFire extends Bot {

    boolean isScanning; // flag set when scanning

    // The main method starts our bot
    public static void main(String[] args) {
        new TrackFire().start();
    }

    // Constructor, which loads the bot config file
    TrackFire() {
        super(BotInfo.fromFile("TrackFire.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        isScanning = false; // reset scanning flag

        // Set colors
        Color pink = Color.fromString("#FF69B4");
        setBodyColor(pink);
        setTurretColor(pink);
        setRadarColor(pink);
        setScanColor(pink);
        setBulletColor(pink);

        // Loop while running
        while (isRunning()) {
            if (isScanning) {
                go(); // skip turn if we a scanning
            } else {
                turnGunLeft(10); // Scans automatically as radar is mounted on gun
            }
        }
    }

    // We scanned another bot -> we have a target, so go get it
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        isScanning = true; // we started scanning

        // Calculate direction of the scanned bot and bearing to it for the gun
        var bearingFromGun = gunBearingTo(e.getX(), e.getY());

        // Turn the gun toward the scanned bot
        turnGunLeft(bearingFromGun);

        // If it is close enough, fire!
        if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
            fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
        }

        // Generates another scan event if we see a bot.
        // We only need to call this if the gun (and therefore radar)
        // are not turning. Otherwise, scan is called automatically.
        if (bearingFromGun < 5) {
            scan();
        }

        isScanning = false; // we stopped scanning
    }

    // We won the round -> do a victory dance!
    @Override
    public void onWonRound(WonRoundEvent e) {
        // Victory dance turning right 360 degrees 100 times
        turnLeft(36_000);
    }
}
