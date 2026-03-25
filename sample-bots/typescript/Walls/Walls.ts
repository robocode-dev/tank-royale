import { Bot, Color, HitBotEvent, ScannedBotEvent } from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// Walls
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// This robot navigates around the perimeter of the battlefield with
// the gun pointed inward.
// ------------------------------------------------------------------
class Walls extends Bot {
  peek = false;    // Don't turn if there's a bot there
  moveAmount = 0; // How much to move

  static main() {
    new Walls().start();
  }

  override run() {
    // Set colors
    this.setBodyColor(Color.BLACK);
    this.setTurretColor(Color.BLACK);
    this.setRadarColor(Color.ORANGE);
    this.setBulletColor(Color.CYAN);
    this.setScanColor(Color.CYAN);

    // Initialize moveAmount to the maximum possible for the arena
    this.moveAmount = Math.max(this.getArenaWidth(), this.getArenaHeight());
    // Initialize peek to false
    this.peek = false;

    // Turn to face a wall.
    // `getDirection() % 90` means the remainder of getDirection() divided by 90.
    this.turnRight(this.getDirection() % 90);
    this.forward(this.moveAmount);

    // Turn the gun to turn right 90 degrees.
    this.peek = true;
    this.turnGunLeft(90);
    this.turnLeft(90);

    // Main loop
    while (this.isRunning()) {
      // Peek before we turn when forward() completes.
      this.peek = true;
      // Move up the wall
      this.forward(this.moveAmount);
      // Don't peek now
      this.peek = false;
      // Turn to the next wall
      this.turnLeft(90);
    }
  }

  // We hit another bot -> move away a bit
  override onHitBot(e: HitBotEvent) {
    // If he's in front of us, set back up a bit.
    const bearing = this.bearingTo(e.x, e.y);
    if (bearing > -90 && bearing < 90) {
      this.back(100);
    } else { // else he's in back of us, so set ahead a bit.
      this.forward(100);
    }
  }

  // We scanned another bot -> fire!
  override onScannedBot(e: ScannedBotEvent) {
    this.fire(2);
    // Note that scan is called automatically when the bot is turning.
    // By calling it manually here, we make sure we generate another scan event if there's a bot
    // on the next wall, so that we do not start moving up it until it's gone.
    if (this.peek) {
      this.rescan();
    }
  }
}

Walls.main();
