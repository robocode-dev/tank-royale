import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

// ------------------------------------------------------------------
// Target
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
//
// A stationary robot that moves when its energy drops below a certain
// threshold. This robot demonstrates how to use custom events.
// ------------------------------------------------------------------
public class Target extends Bot {

    int trigger; // Keeps track of when to move

    // The main method starts our bot
    public static void main(String[] args) {
        new Target().start();
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor(Color.WHITE);
        setTurretColor(Color.WHITE);
        setRadarColor(Color.WHITE);

        // Initially, we'll move when energy passes 80
        trigger = 80;

        // Add a custom event named "trigger-hit"
        addCustomEvent(new Condition("trigger-hit") {
            public boolean test() {
                return getEnergy() <= trigger;
            }
        });
    }

    // A custom event occurred
    @Override
    public void onCustomEvent(CustomEvent e) {
        // Check if our custom event "trigger-hit" went off
        if (e.getCondition().getName().equals("trigger-hit")) {
            // Adjust the trigger value, or else the event will fire again and again and again...
            trigger -= 20;

            // Print out energy level
            System.out.println("Ouch, down to " + (int) (getEnergy() + .5) + " energy.");

            // Move around a bit
            turnRight(65);
            forward(100);
        }
    }
}
