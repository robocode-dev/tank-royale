import {Bot, Condition, CustomEvent} from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// Target
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// A stationary robot that moves when its energy drops below a
// certain threshold. Demonstrates how to use custom events.
// ------------------------------------------------------------------
class Target extends Bot {
    // The energy level at which the bot will start moving
    static readonly ENERGY_THRESHOLD = 50;

    static main() {
        new Target().start();
    }

    override run() {
        // Add a custom event that triggers when energy drops below threshold
        this.addCustomEvent(
            new Condition("lowEnergy", () => this.getEnergy() <= Target.ENERGY_THRESHOLD),
        );

        // Just sit still until the custom event fires
        while (this.isRunning()) {
            this.go();
        }
    }

    // Custom event handler: move when energy drops below threshold
    override onCustomEvent(e: CustomEvent) {
        if (e.condition.name === "lowEnergy") {
            // Remove the condition so it doesn't fire again
            this.removeCustomEvent(e.condition);
            // Move forward to avoid being a sitting duck
            this.setTurnLeft(180);
            this.setForward(100);
        }
    }
}

Target.main();
