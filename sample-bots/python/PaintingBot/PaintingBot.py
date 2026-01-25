from robocode_tank_royale.bot_api import Bot
from robocode_tank_royale.bot_api.events import ScannedBotEvent, TickEvent
from robocode_tank_royale.bot_api.graphics.color import Color

# ---------------------------------------------------------------------------
# PaintingBot
# ---------------------------------------------------------------------------
# A sample bot originally made for Robocode by Pavel Savara
#
# Demonstrates how to paint stuff on the battlefield.
# Remember to enable Graphical Debugging for the bot when running a battle.
# ---------------------------------------------------------------------------
class PaintingBot(Bot):
    scanned_x: float
    scanned_y: float
    scanned_time: int

    # The main method starts our bot
    def run(self) -> None:
        # Continuous forward and backward movement repeating forever
        while self.running:
            self.forward(100)
            self.turn_gun_left(360)
            self.back(100)
            self.turn_gun_left(360)

    # We saw another bot -> save the coordinates of the scanned bot and turn (time) when scanned
    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        # Get the coordinates of the scanned bot and the time (turn) when scanned
        self.scanned_x = float(e.x)
        self.scanned_y = float(e.y)
        self.scanned_time = e.turn_number

        # Also, fire the gun!
        self.fire(1)

    # During each turn (tick), we draw a red circle at the bot's last known location. We can't draw
    # the circle at the bot's current position because we need to scan it again to determine its
    # updated location.
    def on_tick(self, e: TickEvent) -> None:
        # Check if we scanned a bot by checking if the scanned time is not 0
        if getattr(self, "scanned_time", 0) != 0:
            # Calculate a color alpha value for transparency that.
            # The alpha value is at its maximum when a bot is initially scanned, gradually
            # diminishing over time as more time passes since the scan.
            delta_time = e.turn_number - self.scanned_time
            alpha = max(0xFF - (delta_time * 16), 0)

            # Draw a red circle with the alpha value we calculated using anm ellipse
            g = self.graphics

            color = Color.from_rgba(0xFF, 0x00, 0x00, alpha)
            g.set_fill_color(color)
            g.fill_circle(self.scanned_x, self.scanned_y, 20)  # 20 is the radius of the bots bounding circle


def main() -> None:
    # The main method starts our bot
    bot = PaintingBot()
    bot.start()


if __name__ == "__main__":
    main()
