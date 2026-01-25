from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, WonRoundEvent


# ------------------------------------------------------------------
# TrackFire
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# Sits still while tracking and firing at the nearest robot it detects.
# ------------------------------------------------------------------
class TrackFire(Bot):
    def run(self) -> None:
        """Called when a new round is started -> initialize and start scanning."""
        # Set colors to pink (matches Java sample)
        pink = Color.from_rgb(0xFF, 0x69, 0xB4)
        self.body_color = pink
        self.turret_color = pink
        self.radar_color = pink
        self.scan_color = pink
        self.bullet_color = pink

        # Loop while running: keep turning the gun to scan (radar is mounted on gun)
        while self.running:
            self.turn_gun_right(10)

    def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        """We scanned another bot -> track with gun and fire when aligned."""
        # Calculate bearing from gun to the scanned bot
        bearing_from_gun = self.gun_bearing_to(float(e.x), float(e.y))

        # Turn the gun toward the scanned bot
        self.turn_gun_left(bearing_from_gun)

        # If it is close enough and gun is cool, fire with power based on alignment & energy
        if abs(bearing_from_gun) <= 3 and self.gun_heat == 0:
            firepower = min(3 - abs(bearing_from_gun), self.energy - 0.1)
            if firepower > 0:
                self.fire(firepower)

        # Rescan immediately to keep tracking the target bot
        self.rescan()

    def on_won_round(self, e: WonRoundEvent) -> None:
        """We won the round -> do a victory dance!"""
        del e
        # Victory dance turning right 360 degrees 100 times
        self.turn_right(36_000)


def main() -> None:
    bot = TrackFire()
    bot.start()


if __name__ == "__main__":
    main()
