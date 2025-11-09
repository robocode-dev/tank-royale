import asyncio

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitBotEvent


# ------------------------------------------------------------------
# RamFire
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# This robot actively seeks out opponents, rams into them, and fires
# with appropriate power based on the enemy's remaining energy.
# ------------------------------------------------------------------
class RamFire(Bot):
    """Seeks, rams, and fires depending on enemy energy."""

    def __init__(self) -> None:
        super().__init__()
        self._turn_dir: int = 1  # clockwise (-1) or counterclockwise (1)

    async def run(self) -> None:
        # Set colors
        self.body_color = Color.from_rgb(0x99, 0x99, 0x99)   # lighter gray
        self.turret_color = Color.from_rgb(0x88, 0x88, 0x88) # gray
        self.radar_color = Color.from_rgb(0x66, 0x66, 0x66)  # dark gray

        while self.is_running():
            await self.turn_right(5 * self._turn_dir)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        # Turn to face target
        await self._turn_to_face_target(float(e.x), float(e.y))

        # Move towards the target with a small overshoot
        distance = self.distance_to(float(e.x), float(e.y))
        await self.forward(distance + 5)

        # Might want to move forward again!
        await self.rescan()

    async def on_hit_bot(self, e: HitBotEvent) -> None:
        # Turn to face target
        await self._turn_to_face_target(float(e.x), float(e.y))

        # Determine a shot that won't kill the bot... we want to ram instead for bonus points
        energy = float(e.energy)
        if energy > 16:
            await self.fire(3)
        elif energy > 10:
            await self.fire(2)
        elif energy > 4:
            await self.fire(1)
        elif energy > 2:
            await self.fire(0.5)
        elif energy > 0.4:
            await self.fire(0.1)

        # Ram again!
        await self.forward(40)

    async def _turn_to_face_target(self, x: float, y: float) -> None:
        # Calculate bearing to target
        bearing = self.bearing_to(x, y)
        # Update default turn direction used in run()
        self._turn_dir = 1 if bearing >= 0 else -1
        # Turn towards target
        await self.turn_left(bearing)


async def main() -> None:
    bot = RamFire()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
