import asyncio

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
    async def run(self) -> None:
        """Called when a new round is started -> initialize and do some movement."""
        # Repeat while the bot is running
        while self.is_running():
            await self.forward(100)
            await self.turn_gun_left(360)
            await self.back(100)
            await self.turn_gun_left(360)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        """We saw another bot -> fire!"""
        del e
        await self.fire(1)

    async def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        """We were hit by a bullet -> turn perpendicular to the bullet."""
        # Calculate the bearing to the direction of the bullet
        bearing = self.calc_bearing(e.bullet.direction)
        # Turn 90 degrees to the bullet direction based on the bearing
        await self.turn_right(90 - bearing)


async def main() -> None:
    bot = MyFirstBot()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
