import { Bot, Color, HitBotEvent, ScannedBotEvent } from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// SpinBot
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// Continuously moves in a circle while firing at maximum power when
// detecting enemies.
// ------------------------------------------------------------------
class SpinBot extends Bot {
  static main() {
    new SpinBot().start();
  }

  override run() {
    this.setBodyColor(Color.BLUE);
    this.setTurretColor(Color.BLUE);
    this.setRadarColor(Color.BLACK);
    this.setScanColor(Color.YELLOW);

    // Repeat while the bot is running
    while (this.isRunning()) {
      // Tell the game that when we take move, we'll also want to turn right... a lot
      this.setTurnRight(10000);
      // Limit our speed to 5
      this.setMaxSpeed(5);
      // Start moving (and turning)
      this.forward(10000);
    }
  }

  // We scanned another bot -> fire hard!
  override onScannedBot(e: ScannedBotEvent) {
    this.fire(3);
  }

  // We hit another bot -> if it's our fault, we'll stop turning and moving,
  // so we need to turn again to keep spinning.
  override onHitBot(e: HitBotEvent) {
    const direction = this.directionTo(e.x, e.y);
    const bearing = this.calcBearing(direction);
    if (bearing > -10 && bearing < 10) {
      this.fire(3);
    }
    if (e.isRammed) {
      this.turnRight(10);
    }
  }
}

SpinBot.main();
