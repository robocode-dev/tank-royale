import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// Fire
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Sits still. Spins gun around. Moves when hit.
// ------------------------------------------------------------------
public class Fire extends Bot {

    int dist = 50; // Distance to move when we're hit, forward or back

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
        // Set colors
        setBodyColor(Color.fromHex("FA0"));   // orange
        setGunColor(Color.fromHex("F70"));    // dark orange
        setTurretColor(Color.fromHex("F70")); // dark orange
        setRadarColor(Color.fromHex("F00"));  // red
        setScanColor(Color.fromHex("F00"));   // red
        setBulletColor(Color.fromHex("08F")); // light blue

        // Spin the gun around slowly... forever
        while (isRunning()) {
            // Turn the gun a bit if the bot if the target speed is 0
            turnGunLeft(5);
        }
    }

    // We scanned another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // If the other bot is close by, and we have plenty of life, fire hard!
        var distance = distanceTo(e.getX(), e.getY());
        if (distance < 50 && getEnergy() > 50) {
            fire(3);
        } else {
            // Otherwise, only fire 1
            fire(1);
        }
        // Rescan
        rescan();
    }

    // We were hit by a bullet -> turn perpendicular to the bullet, and move a bit
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Turn perpendicular to the bullet direction
        turnLeft(normalizeRelativeAngle(90 - (getDirection() - e.getBullet().getDirection())));

        // Move forward or backward depending if the distance is positive or negative
        forward(dist);
        dist *= -1; // Change distance, meaning forward or backward direction

        // Rescan
        rescan();
    }

    // We have hit another bot -> aim at it and fire hard!
    @Override
    public void onHitBot(HitBotEvent e) {
        // Turn gun to the bullet direction
        var direction = directionTo(e.getX(), e.getY());
        var gunBearing = normalizeRelativeAngle(direction - getGunDirection());
        turnGunLeft(gunBearing);

        // Fire hard
        fire(3);
    }
}
