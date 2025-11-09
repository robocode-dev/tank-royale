import asyncio

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitBotEvent


# ------------------------------------------------------------------
# SpinBot
# ------------------------------------------------------------------
# A sample bot originally made for Robocode by Mathew Nelson.
#
# Continuously moves in a circle while firing at maximum power when
# detecting enemies.
# ------------------------------------------------------------------
class SpinBot(Bot):
    async def run(self) -> None:
        # Set colors
        self.body_color = Color.BLUE
        self.turret_color = Color.BLUE
        self.radar_color = Color.BLACK
        self.scan_color = Color.YELLOW

        # Repeat while the bot is running
        while self.is_running():
            # Tell the game that when we move, we'll also want to turn right... a lot
            self.set_turn_right(10_000)
            # Limit our speed to 5
            self.max_speed = 5
            # Start moving (and turning)
            await self.forward(10_000)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        del e
        # Fire hard!
        await self.fire(3)

    async def on_hit_bot(self, e: HitBotEvent) -> None:
        # If the bot is roughly in front of us, fire hard
        bearing = self.calc_bearing(self.direction_to(float(e.x), float(e.y)))
        if -10 < bearing < 10:
            await self.fire(3)
        # If we rammed it, nudge to keep spinning
        if e.is_rammed:
            await self.turn_right(10)


async def main() -> None:
    bot = SpinBot()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
