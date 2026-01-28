from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import (
    ScannedBotEvent,
    HitWallEvent,
    HitBotEvent,
)


# ------------------------------------------------------------------
# Crazy
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# This robot moves in a zigzag pattern while firing at enemies.
# ------------------------------------------------------------------
class Crazy(Bot):
    """Moves in a zigzag pattern and fires when scanning other bots."""

    def __init__(self) -> None:
        super().__init__()
        self._moving_forward: bool = False

    def run(self) -> None:
        """Called when a new round is started -> initialize and do some movement."""
        # Set colors (matching the Java sample)
        self.body_color = Color.from_rgb(0x00, 0xC8, 0x00)   # lime
        self.turret_color = Color.from_rgb(0x00, 0x96, 0x32) # green
        self.radar_color = Color.from_rgb(0x00, 0x64, 0x64)  # dark cyan
        self.bullet_color = Color.from_rgb(0xFF, 0xFF, 0x64) # yellow
        self.scan_color = Color.from_rgb(0xFF, 0xC8, 0xC8)   # light red

        # Loop while the bot is running
        while self.running:
            # Tell the game we will want to move ahead 40000 -- some large number
            self.set_forward(40000)
            self._moving_forward = True

            # Tell the game we will want to turn left 90
            self.set_turn_left(90)

            # Start the action and wait until the turn is complete
            self.wait_for(lambda: self.turn_remaining == 0)

            # Note: We are still moving ahead now, but the turn is complete.
            # Now we'll turn the other way...
            self.set_turn_right(180)
            # ... and wait for the turn to finish ...
            self.wait_for(lambda: self.turn_remaining == 0)

            # ... then the other way ...
            self.set_turn_left(180)
            # ... and wait for that turn to finish.
            self.wait_for(lambda: self.turn_remaining == 0)
            # then back to the top to do it all again.

    def on_hit_wall(self, e: HitWallEvent) -> None:
        """We collided with a wall -> reverse the direction."""
        del e
        self._reverse_direction()

    def on_hit_bot(self, e: HitBotEvent) -> None:
        """We hit another bot -> back up if we rammed it."""
        if e.rammed:
            self._reverse_direction()

    def _reverse_direction(self) -> None:
        """Switch from ahead to back & vice versa and commit the change."""
        if self._moving_forward:
            self.set_back(40000)
            self._moving_forward = False
        else:
            self.set_forward(40000)
            self._moving_forward = True
        # Commit the command changes immediately
        self.go()

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        """We scanned another bot -> fire!"""
        del e
        self.fire(1)


def main() -> None:
    bot = Crazy()
    bot.start()


if __name__ == "__main__":
    main()
