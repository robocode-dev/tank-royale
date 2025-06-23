import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

// ------------------------------------------------------------------
// MyFirstLeader
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// Member of the MyFirstTeam. Looks around for enemies, and orders
// teammates to fire.
// ------------------------------------------------------------------
public class MyFirstLeader extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new MyFirstLeader().start();
    }

    // Called when a new round is started -> Leader's default behavior
    @Override
    public void run() {
        // Prepare robot colors to send to teammates
        var colors = new RobotColors();

        colors.bodyColor = Color.RED;
        colors.tracksColor = Color.CYAN;
        colors.turretColor = Color.RED;
        colors.gunColor = Color.YELLOW;
        colors.radarColor = Color.RED;
        colors.scanColor = Color.YELLOW;
        colors.bulletColor = Color.YELLOW;

        // Set the color of this robot containing the robot colors
        setBodyColor(colors.bodyColor);
        setTracksColor(colors.tracksColor);
        setTurretColor(colors.turretColor);
        setGunColor(colors.gunColor);
        setRadarColor(colors.radarColor);
        setScanColor(colors.scanColor);
        setBulletColor(colors.bulletColor);

        // Send RobotColors object to every member in the team
        broadcastTeamMessage(colors);

        // Set the radar to turn right forever
        setTurnRadarLeft(Double.POSITIVE_INFINITY);

        // Repeat while the bot is running
        while (isRunning()) {
            // Move forward and back
            forward(100);
            back(100);
        }
    }

    // Called when we scanned a bot -> Send enemy position to teammates
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // We scanned a teammate -> ignore
        if (isTeammate(e.getScannedBotId())) {
            return;
        }

        // Send enemy position to teammates
        broadcastTeamMessage(new Point(e.getX(), e.getY()));
    }


    // Called when we have been hit by a bullet -> turn perpendicular to the bullet direction
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bullet bearing
        double bulletBearing = calcBearing(e.getBullet().getDirection());

        // Turn perpendicular to the bullet direction
        turnLeft(90 - bulletBearing);
    }
}

// ------------------------------------------------------------------
// Communication objects for team messages
// ------------------------------------------------------------------

// Point (x,y) class
class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

// Robot colors
class RobotColors {
    public Color bodyColor;
    public Color tracksColor;
    public Color turretColor;
    public Color gunColor;
    public Color radarColor;
    public Color scanColor;
    public Color bulletColor;
}