import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

import java.io.IOException;

// ------------------------------------------------------------------
// SpinBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Moves in a circle, firing hard when an enemy is detected.
// ------------------------------------------------------------------
public class SpinBot extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new SpinBot().start();
    }

    // Constructor, which loads the bot config file
    SpinBot() {
        super(BotInfo.fromFile("SpinBot.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        setBodyColor("#00F"); // blue
        setTurretColor("#00F"); // blue
        setRadarColor("#000"); // black
        setScanColor("#FF0"); // yellow

        // Repeat while the bot is running
        while (isRunning()) {
            // Tell the game that when we take move, we'll also want to turn right... a lot
            setTurnLeft(10_000);
            // Limit our speed to 5
            setMaxSpeed(5);
            // Start moving (and turning)
            forward(10_000);
        }
    }

    // We scanned another bot -> fire hard!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(3);
    }

    // We hit another bot -> if it's our fault, we'll stop turning and moving,
    // so we need to turn again to keep spinning.
    @Override
    public void onHitBot(HitBotEvent e) {
        double direction = directionTo(e.getX(), e.getY());
        double bearing = calcBearing(direction);
        if (bearing > -10 && bearing < 10) {
            fire(3);
        }
        if (e.isRammed()) {
            turnLeft(10);
        }
    }
}