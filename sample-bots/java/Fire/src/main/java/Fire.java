import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

import java.io.IOException;

// ------------------------------------------------------------------
// Fire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// This bot moves to a corner, then swings the gun back and forth.
// If it dies, it tries a new corner in the next round.
// ------------------------------------------------------------------
public class Fire extends Bot {

    int dist = 50; // Distance to move when we're hit, forward or back
    boolean isScanning; // Flag indicating if onScannedBot() handler is running

    // The main method starts our bot
    public static void main(String[] args) {
        new Fire().start();
    }

    // Constructor, which loads the bot settings file
    Fire() {
        super(BotInfo.fromFile("Fire.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        isScanning = false; // Clear scanning flag for each new turn

        // Set colors
        setBodyColor("#FA0"); // orange
        setGunColor("#F70"); // dark orange
        setTurretColor("#F70"); // dark orange
        setRadarColor("#F00"); // red
        setScanColor("#F00"); // red
        setBulletColor("#08F"); // light blue

        // Spin the gun around slowly... forever
        while (isRunning()) {
            if (isScanning) {
                // Skip a turn if the onScannedBot handler is running
                go();
            } else {
                // Turn the gun a bit if the bot if the target speed is 0
                turnGunLeft(5);
            }
        }
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        isScanning = true; // We are now scanning

        // If the other robot is close by, and we have plenty of life, fire hard!
        double distance = distanceTo(e.getX(), e.getY());
        if (distance < 50 && getEnergy() > 50) {
            fire(3);
        } else {
            // Otherwise, only fire 1
            fire(1);
        }
        // Rescan
        scan();

        isScanning = false; // We are not scanning any more
    }

    // We were hit by a bullet -> turn perpendicular to the bullet, and move a bit
    @Override
    public void onHitByBullet(BulletHitBotEvent e) {
        // Turn perpendicular to the bullet direction
        turnLeft(normalizeRelativeAngle(90 - (getDirection() - e.getBullet().getDirection())));

        // Move forward or backward depending if the distance is positive or negative
        forward(dist);
        dist *= -1; // Change distance, meaning forward or backward direction

        // Rescan
        scan();
    }

    // We have hit another bot -> aim at it and fire hard!
    @Override
    public void onHitBot(HitBotEvent e) {
        // Turn gun to the bullet direction
        double direction = directionTo(e.getX(), e.getY());
        double gunBearing = normalizeRelativeAngle(direction - getGunDirection());
        turnGunLeft(gunBearing);

        // Fire hard
        fire(3);
    }
}
