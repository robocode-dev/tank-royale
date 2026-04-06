import {Bot, Color, Condition, CustomEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// Target
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// A stationary robot that moves when its energy drops below a certain
// threshold. This robot demonstrates how to use custom events.
// ------------------------------------------------------------------
class Target extends Bot {
    trigger = 0; // Keeps track of when to move

    static main() {
        new Target().start();
    }

    // Called when a new round is started -> initialize and do some movement
    override run() {
        // Set colors
        this.setBodyColor(Color.WHITE);
        this.setTurretColor(Color.WHITE);
        this.setRadarColor(Color.WHITE);

        // Initially, we'll move when energy passes 80
        this.trigger = 80;

        // Add a custom event named "trigger-hit"
        this.addCustomEvent(
            new Condition("trigger-hit", () => this.getEnergy() <= this.trigger),
        );
    }

    // A custom event occurred
    override onCustomEvent(e: CustomEvent) {
        // Check if our custom event "trigger-hit" went off
        if (e.condition.name === "trigger-hit") {
            // Adjust the trigger value, or else the event will fire again and again and again...
            this.trigger -= 20;

            // Print out energy level
            console.log(`Ouch, down to ${Math.round(this.getEnergy())} energy.`);

            // Move around a bit
            this.turnRight(65);
            this.forward(100);
        }
    }
}

Target.main();
