import { Bot, Color, ScannedBotEvent, TickEvent } from "@robocode.dev/tank-royale-bot-api";

// ------------------------------------------------------------------
// PaintingBot
// ------------------------------------------------------------------
// A sample bot originally made for Robocode by Pavel Savara.
//
// Demonstrates how to paint stuff on the battlefield.
// Remember to enable Graphical Debugging for the bot when running a battle.
// ------------------------------------------------------------------
class PaintingBot extends Bot {
  scannedX = 0;
  scannedY = 0;
  scannedTime = 0;

  static main() {
    new PaintingBot().start();
  }

  override run() {
    // Continuous forward and backward movement repeating forever
    while (this.isRunning()) {
      this.forward(100);
      this.turnGunLeft(360);
      this.back(100);
      this.turnGunLeft(360);
    }
  }

  // We saw another bot -> save the coordinates of the scanned bot and turn (time) when scanned
  override onScannedBot(e: ScannedBotEvent) {
    // Get the coordinates of the scanned bot and the time (turn) when scanned
    this.scannedX = Math.trunc(e.x);
    this.scannedY = Math.trunc(e.y);
    this.scannedTime = e.turnNumber;
    // Also, fire the gun!
    this.fire(1);
  }

  // During each turn (tick), we draw a red circle at the bot's last known location.
  override onTick(e: TickEvent) {
    // Check if we scanned a bot by checking if the scanned time is not 0
    if (this.scannedTime !== 0) {
      // Calculate a color alpha value for transparency.
      // The alpha value is at its maximum when a bot is initially scanned, gradually
      // diminishing over time as more time passes since the scan.
      const deltaTime = e.turnNumber - this.scannedTime;
      const alpha = Math.max(0xff - deltaTime * 16, 0);
      // Draw a red circle with the alpha value we calculated using an ellipse
      const g = this.getGraphics();
      const color = Color.fromRgba(0xff, 0x00, 0x00, alpha);
      g.setFillColor(color);
      g.fillCircle(this.scannedX, this.scannedY, 20); // 20 is the radius of the bots bounding circle
    }
  }
}

PaintingBot.main();
