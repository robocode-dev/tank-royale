import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

// ------------------------------------------------------------------
// Walls
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// This robot navigates around the perimeter of the battlefield with
// the gun pointed inward.
// ------------------------------------------------------------------
public class Walls extends Bot {

    boolean peek; // Don't turn if there's a bot there
    double moveAmount; // How much to move

    // The main method starts our bot
    public static void main(String[] args) {
        new Walls().start();
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor(Color.BLACK);
        setTurretColor(Color.BLACK);
        setRadarColor(Color.ORANGE);
        setBulletColor(Color.CYAN);
        setScanColor(Color.CYAN);

        // Initialize moveAmount to the maximum possible for the arena
        moveAmount = Math.max(getArenaWidth(), getArenaHeight());
        // Initialize peek to false
        peek = false;

        // turn to face a wall.
        // `getDirection() % 90` means the remainder of getDirection() divided by 90.
        turnRight(getDirection() % 90);
        forward(moveAmount);

        // Turn the gun to turn right 90 degrees.
        peek = true;
        turnGunLeft(90);
        turnLeft(90);

        // Main loop
        while (isRunning()) {
            // Peek before we turn when forward() completes.
            peek = true;
            // Move up the wall
            forward(moveAmount);
            // Don't peek now
            peek = false;
            // Turn to the next wall
            turnLeft(90);
        }
    }

    // We hit another bot -> move away a bit
    @Override
    public void onHitBot(HitBotEvent e) {
        // If he's in front of us, set back up a bit.
        var bearing = bearingTo(e.getX(), e.getY());
        if (bearing > -90 && bearing < 90) {
            back(100);
        } else { // else he's in back of us, so set ahead a bit.
            forward(100);
        }
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(2);
        // Note that scan is called automatically when the bot is turning.
        // By calling it manually here, we make sure we generate another scan event if there's a bot
        // on the next wall, so that we do not start moving up it until it's gone.
        if (peek) {
            rescan();
        }
    }
}
