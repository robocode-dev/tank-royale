import asyncio

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import (
    ScannedBotEvent,
    HitByBulletEvent,
    HitBotEvent,
)


# ------------------------------------------------------------------
# Fire
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# Sits still, continuously rotates its gun, and only moves when hit.
# ------------------------------------------------------------------
class Fire(Bot):
    """Sits still, rotates its gun, and moves reactively when hit."""

    def __init__(self) -> None:
        super().__init__()
        self._dist: int = 50  # Distance to move when we're hit, forward or back

    async def run(self) -> None:
        # Set colors
        self.body_color = Color.from_rgb(0xFF, 0xAA, 0x00)   # orange
        self.turret_color = Color.from_rgb(0xFF, 0x77, 0x00) # dark orange
        self.radar_color = Color.from_rgb(0xFF, 0x00, 0x00)  # red
        self.scan_color = Color.from_rgb(0xFF, 0x00, 0x00)   # red
        self.bullet_color = Color.from_rgb(0x00, 0x88, 0xFF) # light blue

        # Spin the gun around slowly... forever
        while self.is_running():
            await self.turn_gun_right(5)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        # If the other bot is close by, and we have plenty of life, fire hard!
        distance = self.distance_to(float(e.x), float(e.y))
        if distance < 50 and self.get_energy() > 50:
            await self.fire(3)
        else:
            # Otherwise, only fire 1
            await self.fire(1)
        # Rescan
        await self.rescan()

    async def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        # Turn perpendicular to the bullet direction
        bearing = self.calc_bearing(e.bullet.direction)
        await self.turn_right(90 - bearing)

        # Move forward or backward depending on current sign
        await self.forward(self._dist)
        self._dist *= -1  # Change sign -> toggles forward/backward

        # Rescan
        await self.rescan()

    async def on_hit_bot(self, e: HitBotEvent) -> None:
        # Turn gun to face the target
        gun_bearing = self.gun_bearing_to(float(e.x), float(e.y))
        await self.turn_gun_right(gun_bearing)
        # Fire hard
        await self.fire(3)


async def main() -> None:
    bot = Fire()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
