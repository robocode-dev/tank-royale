import asyncio
from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.events import ScannedBotEvent, WonRoundEvent
from robocode_tank_royale.bot_api.color import Color


class TrackFire(Bot):
    """
    A sample bot that sits still while tracking and firing at the nearest bot it detects.
    """

    async def run(self):
        """Main method"""
        pink = Color.from_rgb(0xFF, 0x69, 0xB4)
        self.body_color = pink
        self.turret_color = pink
        self.radar_color = pink
        self.scan_color = pink
        self.bullet_color = pink

        while self.is_running():
            await self.turn_gun_right(10)

    async def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        """Event handler for scanned bot"""
        bearing_from_gun = self.gun_bearing_to(scanned_bot_event.x, scanned_bot_event.y)
        self.set_turn_gun_left(bearing_from_gun)

        if abs(bearing_from_gun) <= 3 and self.get_gun_heat() == 0:
            self.set_fire(min(3 - abs(bearing_from_gun), self.get_energy() - 0.1))

        await self.rescan()

    async def on_won_round(self, won_round_event: WonRoundEvent) -> None:
        """Event handler for winning a round"""
        await self.turn_right(36_000)


async def main():
    """Main method"""
    bot = TrackFire()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
