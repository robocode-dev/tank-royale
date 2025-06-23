import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;
import java.util.Random;

// ------------------------------------------------------------------
// Corners
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// This robot moves to a corner, then rotates its gun back and forth
// scanning for enemies. If it performs poorly in a round, it will
// try a different corner in the next round.
// ------------------------------------------------------------------
public class Corners extends Bot {

    int enemies; // Number of enemy bots in the game
    static int corner = randomCorner(); // Which corner we are currently using. Set to random corner
    boolean stopWhenSeeEnemy = false; // See goCorner()

    // The main method starts our bot
    public static void main(String[] args) {
        new Corners().start();
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor(Color.RED);
        setTurretColor(Color.BLACK);
        setRadarColor(Color.YELLOW);
        setBulletColor(Color.GREEN);
        setScanColor(Color.GREEN);

        // Save # of other bots
        enemies = getEnemyCount();

        // Move to a corner
        goCorner();

        // Initialize gun turn speed to 3
        int gunIncrement = 3;

        // Spin gun back and forth
        while (isRunning()) {
            for (int i = 0; i < 30; i++) {
                turnGunLeft(gunIncrement);
            }
            gunIncrement *= -1;
        }
    }

    // Returns a random corner (0, 90, 180, 270)
    private static int randomCorner() {
        return 90 * new Random().nextInt(4); // Random number is between 0-3
    }

    // A very inefficient way to get to a corner.
    // Can you do better as an home exercise? :)
    private void goCorner() {
        // We don't want to stop when we're just turning...
        stopWhenSeeEnemy = false;
        // Turn to face the wall towards our desired corner
        turnLeft(calcBearing(corner));
        // Ok, now we don't want to crash into any bot in our way...
        stopWhenSeeEnemy = true;
        // Move to that wall
        forward(5000);
        // Turn to face the corner
        turnLeft(90);
        // Move to the corner
        forward(5000);
        // Turn gun to starting point
        turnGunLeft(90);
    }

    // We saw another bot -> stop and fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        var distance = distanceTo(e.getX(), e.getY());

        // Should we stop, or just fire?
        if (stopWhenSeeEnemy) {
            // Stop movement
            stop();
            // Call our custom firing method
            smartFire(distance);
            // Rescan for another bot
            rescan();
            // This line will not be reached when scanning another bot.
            // So we did not scan another bot -> resume movement
            resume();
        } else {
            smartFire(distance);
        }
    }

    // Custom fire method that determines firepower based on distance.
    // distance: The distance to the bot to fire at.
    private void smartFire(double distance) {
        if (distance > 200 || getEnergy() < 15) {
            fire(1);
        } else if (distance > 50) {
            fire(2);
        } else {
            fire(3);
        }
    }

    // We died -> figure out if we need to switch to another corner
    @Override
    public void onDeath(DeathEvent e) {
        // Well, others should never be 0, but better safe than sorry.
        if (enemies == 0) {
            return;
        }

        // If 75% of the bots are still alive when we die, we'll switch corners.
        if (getEnemyCount() / (double) enemies >= .75) {
            corner += 90; // Next corner
            corner %= 360; // Make sure the corner is within 0 - 359

            System.out.println("I died and did poorly... switching corner to " + corner);
        } else {
            System.out.println("I died but did well. I will still use corner " + corner);
        }
    }
}