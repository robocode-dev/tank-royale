from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitByBulletEvent


# ------------------------------------------------------------------
# MyFirstBot
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# Probably the first bot you will learn about.
# Moves in a seesaw motion and spins the gun around at each end.
# ------------------------------------------------------------------
class MyFirstBot(Bot):
    def run(self) -> None:
        """Called when a new round is started -> initialize and do some movement."""
        # Repeat while the bot is running
        while self.running:
            self.forward(100)
            self.turn_gun_left(360)
            self.back(100)
            self.turn_gun_left(360)

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        """We saw another bot -> fire!"""
        del e
        self.fire(1)

    def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        """We were hit by a bullet -> turn perpendicular to the bullet."""
        # Calculate the bearing to the direction of the bullet
        bearing = self.calc_bearing(e.bullet.direction)
        # Turn 90 degrees to the bullet direction based on the bearing
        self.turn_right(90 - bearing)


def main() -> None:
    bot = MyFirstBot()
    bot.start()


if __name__ == "__main__":
    main()
