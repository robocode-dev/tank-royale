import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import java.awt.Color;

// ------------------------------------------------------------------
// Target
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Sits still. Moves every time energy drops by 20.
// This bot demonstrates custom events.
// ------------------------------------------------------------------
public class Target extends Bot {

    int trigger; // Keeps track of when to move

    // The main method starts our bot
    public static void main(String[] args) {
        new Target().start();
    }

    // Constructor, which loads the bot config file
    Target() {
        super(BotInfo.fromFile("Target.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor(Color.white);
        setTurretColor(Color.white);
        setRadarColor(Color.white);

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
            turnLeft(65);
            forward(100);
        }
    }
}
