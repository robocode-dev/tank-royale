import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

// ------------------------------------------------------------------
// SpinBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Continuously moves in a circle while firing at maximum power when
// detecting enemies.
// ------------------------------------------------------------------
public class SpinBot extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new SpinBot().start();
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        setBodyColor(Color.BLUE);
        setTurretColor(Color.BLUE);
        setRadarColor(Color.BLACK);
        setScanColor(Color.YELLOW);

        // Repeat while the bot is running
        while (isRunning()) {
            // Tell the game that when we take move, we'll also want to turn right... a lot
            setTurnRight(10_000);
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
        var direction = directionTo(e.getX(), e.getY());
        var bearing = calcBearing(direction);
        if (bearing > -10 && bearing < 10) {
            fire(3);
        }
        if (e.isRammed()) {
            turnRight(10);
        }
    }
}