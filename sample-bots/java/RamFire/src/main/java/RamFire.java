import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

import java.io.IOException;

// ------------------------------------------------------------------
// RamFire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Drives at robots trying to ram them. Fires when it hits them.
// ------------------------------------------------------------------
public class RamFire extends Bot {

    int turnDirection = 1; // clockwise (-1) or counterclockwise (1)

    // The main method starts our bot
    public static void main(String[] args) {
        new RamFire().start();
    }

    // Constructor, which loads the bot config file
    RamFire() {
        super(BotInfo.fromFile("RamFire.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor("#999"); // gray
        setTurretColor("#888"); // gray
        setRadarColor("#666"); // dark gray

        while (isRunning()) {
            turnLeft(5 * turnDirection);
        }
    }

    // We scanned another bot -> go ram it
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        turnToFaceTarget(e.getX(), e.getY());

        double distance = distanceTo(e.getX(), e.getY());
        forward(distance + 5);

        scan(); // Might want to move forward again!
    }

    // We have hit another bot -> turn to face robot, fire hard, and ram it again!
    @Override
    public void onHitBot(HitBotEvent e) {
        turnToFaceTarget(e.getX(), e.getY());

        // Determine a shot that won't kill the robot...
        // We want to ram him instead for bonus points
        if (e.getEnergy() > 16) {
            fire(3);
        } else if (e.getEnergy() > 10) {
            fire(2);
        } else if (e.getEnergy() > 4) {
            fire(1);
        } else if (e.getEnergy() > 2) {
            fire(.5);
        } else if (e.getEnergy() > .4) {
            fire(.1);
        }
        forward(40); // Ram him again!
    }

    // Method that turns the bot to face the target at coordinate x,y, but also sets the
    // default turn direction used if no bot is being scanned within in the run() method.
    private void turnToFaceTarget(double x, double y) {
        double bearing = bearingTo(x, y);
        if (bearing >= 0) {
            turnDirection = 1;
        } else {
            turnDirection = -1;
        }
        turnLeft(bearing);
    }
}
