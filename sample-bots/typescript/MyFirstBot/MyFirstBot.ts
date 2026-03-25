import { Bot, BotInfo, HitByBulletEvent, ScannedBotEvent } from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// MyFirstBot
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Mathew Nelson.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion and spins the gun around at each end.
// ------------------------------------------------------------------
class MyFirstBot extends Bot {
  // The main method starts our bot
  static main() {
    new MyFirstBot().start();
  }

  // Called when a new round is started -> initialize and do some movement
  override run() {
    // Repeat while the bot is running
    while (this.isRunning()) {
      this.forward(100);
      this.turnGunLeft(360);
      this.back(100);
      this.turnGunLeft(360);
    }
  }

  // We saw another bot -> fire!
  override onScannedBot(e: ScannedBotEvent) {
    this.fire(1);
  }

  // We were hit by a bullet -> turn perpendicular to the bullet
  override onHitByBullet(e: HitByBulletEvent) {
    // Calculate the bearing to the direction of the bullet
    const bearing = this.calcBearing(e.bullet.direction);
    // Turn 90 degrees to the bullet direction based on the bearing
    this.turnRight(90 - bearing);
  }
}

MyFirstBot.main();
