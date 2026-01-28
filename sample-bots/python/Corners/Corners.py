import random

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, DeathEvent


# ------------------------------------------------------------------
# Corners
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# This robot moves to a corner, then rotates its gun back and forth
# scanning for enemies. If it performs poorly in a round, it will
# try a different corner in the next round.
# ------------------------------------------------------------------
class Corners(Bot):
    """Moves to a corner, scans by swinging the gun, and adapts corner per round."""

    # Which corner we are currently using (in degrees). Set to a random corner at startup
    # Using a class attribute to persist between rounds in the same match, similar to Java 'static'.
    corner: int = 90 * random.randrange(4)  # 0, 90, 180, or 270

    def __init__(self) -> None:
        super().__init__()
        self._enemies: int = 0  # Number of enemy bots in the game
        self._stop_when_see_enemy: bool = False  # See go_corner()

    def run(self) -> None:
        """Called when a new round is started -> initialize and do some movement."""
        # Set colors
        self.body_color = Color.from_rgb(0xFF, 0x00, 0x00)   # red
        self.turret_color = Color.from_rgb(0x00, 0x00, 0x00) # black
        self.radar_color = Color.from_rgb(0xFF, 0xFF, 0x00)  # yellow
        self.bullet_color = Color.from_rgb(0x00, 0xFF, 0x00) # green
        self.scan_color = Color.from_rgb(0x00, 0xFF, 0x00)   # green

        # Save number of other bots
        self._enemies = self.enemy_count

        # Move to a corner
        self._go_corner()

        # Initialize gun turn speed to 3
        gun_increment = 3

        # Spin gun back and forth
        while self.running:
            for _ in range(30):
                self.turn_gun_left(gun_increment)
            gun_increment *= -1

    def _go_corner(self) -> None:
        """Move to the selected corner (very simple approach to mirror the Java sample)."""
        # We don't want to stop when we're just turning...
        self._stop_when_see_enemy = False
        # Turn to face the wall towards our desired corner
        self.turn_left(self.calc_bearing(Corners.corner))
        # Ok, now we don't want to crash into any bot in our way...
        self._stop_when_see_enemy = True
        # Move to that wall
        self.forward(5000)
        # Turn to face the corner
        self.turn_left(90)
        # Move to the corner
        self.forward(5000)
        # Turn gun to starting point
        self.turn_gun_left(90)

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        """We saw another bot -> stop and fire!"""
        distance = self.distance_to(float(e.x), float(e.y))

        if self._stop_when_see_enemy:
            # Stop movement
            self.stop()
            # Call our custom firing method
            self._smart_fire(distance)
            # Rescan for another bot
            try:
                self.rescan()
                # If not interrupted by a new scan, resume movement
                self.resume()
            except Exception:
                # A new ScannedBotEvent can interrupt rescan; internal logic handles interruption
                # No resume here to mirror Java behavior (resume only when not interrupted)
                pass
        else:
            self._smart_fire(distance)

    def _smart_fire(self, distance: float) -> None:
        """Custom fire method that determines firepower based on distance."""
        energy = self.energy
        if distance > 200 or energy < 15:
            self.fire(1)
        elif distance > 50:
            self.fire(2)
        else:
            self.fire(3)

    def on_death(self, e: DeathEvent) -> None:
        """We died -> figure out if we need to switch to another corner."""
        del e
        # Well, others should never be 0, but better safe than sorry.
        if self._enemies == 0:
            return

        # If 75% of the bots are still alive when we die, we'll switch corners.
        if self.enemy_count / float(self._enemies) >= 0.75:
            Corners.corner = (Corners.corner + 90) % 360  # Next corner, normalized
            print(f"I died and did poorly... switching corner to {Corners.corner}")
        else:
            print(f"I died but did well. I will still use corner {Corners.corner}")


def main() -> None:
    bot = Corners()
    bot.start()


if __name__ == "__main__":
    main()
