import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

// ------------------------------------------------------------------
// MyFirstDroid
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// This is a droid bot meaning that is has more energy, but no radar.
// Member of the MyFirstTeam. Follows orders of team leader.
// ------------------------------------------------------------------
public class MyFirstDroid extends Bot implements Droid {

    // The main method starts our bot
    public static void main(String[] args) {
        new MyFirstDroid().start();
    }

    // Called when a new round is started -> just print out that the bot is ready
    @Override
    public void run() {
        System.out.println("MyFirstDroid ready");

        while (isRunning()) {
            go(); // execute next turn (onTeamMessage() takes handles all bot logic based on team messages)
        }
        // terminates when this point is reached
    }

    // Called when a team message is received, which will be send from MyTeamLeader
    @Override
    public void onTeamMessage(TeamMessageEvent e) {
        var message = e.getMessage();

        if (message instanceof Point) {
            // ------------------------------------------------------
            // Message is a point towards a target
            // ------------------------------------------------------

            // Read the target point
            var target = (Point) message;

            // Turn gun to target
            turnRight(bearingTo(target.x, target.y));

            // Fire hard!
            fire(3);

        } else if (message instanceof RobotColors) {
            // ------------------------------------------------------
            // Message is containing new robot colors
            // ------------------------------------------------------

            // Read and set the robot colors
            var colors = (RobotColors) message;

            setBodyColor(colors.bodyColor);
            setTracksColor(colors.tracksColor);
            setTurretColor(colors.turretColor);
            setGunColor(colors.gunColor);
            setRadarColor(colors.radarColor);
            setScanColor(colors.scanColor);
            setBulletColor(colors.bulletColor);
        }
    }
}

// ------------------------------------------------------------------
// Communication objects for team messages
// ------------------------------------------------------------------

// Point (x,y) class
class Point {
    public final double x;
    public final double y;

    public Point(double x, Double y) {
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