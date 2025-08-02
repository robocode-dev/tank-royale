import asyncio

from robocode_tank_royale.bot_api import Bot
from robocode_tank_royale.bot_api import events

class VelocityBot(Bot):
    turn_counter: int

    async def run(self) -> None:
        self.turn_counter = 0
        self.gun_turn_rate = 15

        while self.is_running():
            if self.turn_counter % 64 == 0:
                self.turn_rate = 0
                self.target_speed = 4
            
            if self.turn_counter % 64 == 32:
                self.target_speed = -6
            
            self.turn_counter += 1
            await self.go()

    async def on_scanned_bot(self, scanned_bot_event: events.ScannedBotEvent) -> None:
        await self.fire(1)

    async def on_hit_by_bullet(self, hit_by_bullet_event: events.HitByBulletEvent) -> None:
        self.turn_rate = 5

    async def on_hit_wall(self, bot_hit_wall_event: events.HitWallEvent) -> None:
        assert self.target_speed is not None
        self.target_speed = -1 * self.target_speed


async def main():
    """Main method"""
    bot = VelocityBot()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
