from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitByBulletEvent, HitWallEvent


# ------------------------------------------------------------------
# VelocityBot
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Joshua Galecki.
#
# Example bot of how to use turn rates and target speeds.
# ------------------------------------------------------------------
class VelocityBot(Bot):
    def __init__(self) -> None:
        super().__init__()
        self._turn_counter: int = 0

    def run(self) -> None:
        """Called when a new round is started -> initialize and control movement each turn."""

        self._turn_counter = 0

        # Set the gun (turret) turn rate to rotate slowly all the time
        self.gun_turn_rate = 15

        # Main control loop: one go() per iteration to represent a turn
        while self.running:
            if self._turn_counter % 64 == 0:
                # Straighten out if we were hit by a bullet (ends turning)
                self.turn_rate = 0
                # Go forward with a target speed of 4
                self.target_speed = 4

            if self._turn_counter % 64 == 32:
                # Go backwards, faster
                self.target_speed = -6

            self._turn_counter += 1

            # Execute the current turn
            self.go()

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        """We scanned another bot -> fire!"""
        del e
        self.fire(1)

    def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        """We were hit by a bullet -> set turn rate to make movement less predictable."""
        del e
        # Turn to confuse other bots
        self.turn_rate = 5

    def on_hit_wall(self, e: HitWallEvent) -> None:
        """We hit a wall -> move in the opposite direction by reversing target speed.
        Note that current speed is 0 as the bot just hit the wall.
        """
        del e
        # Move away from the wall by reversing the target speed.
        # Note that current speed is 0 as the bot just hit the wall.
        current = self.target_speed if self.target_speed is not None else 0.0
        self.target_speed = -1 * current if current != 0.0 else -4.0


def main() -> None:
    bot = VelocityBot()
    bot.start()


if __name__ == "__main__":
    main()
