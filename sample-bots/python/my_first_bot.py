import asyncio

from robocode_tank_royale import bot_api as ba
from robocode_tank_royale.bot_api.events import HitByBulletEvent, ScannedBotEvent

class MyFirstBot(ba.Bot):
    """A sample bot original made for Robocode by Mathew Nelson.
    
    Probably the first bot you will learn about.
    Moves in a seesaw motion and spins the gun around at each end.
    """
    async def run(self):
        while self.is_running():
            await self.forward(100)
            await self.turn_gun_left(360)
            await self.back(100)
            await self.turn_gun_left(360)
    
    async def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        await self.fire(1)

    async def on_hit_by_bullet(self, hit_by_bullet_event: HitByBulletEvent) -> None:
        bearing = self.calc_bearing(hit_by_bullet_event.bullet.direction)
        await self.turn_right(90 - bearing)

async def main():
    b = MyFirstBot(
        bot_info=ba.BotInfo(
            name="MyFirstBotPython",
            version="1.0",
            authors=["Robocode Tank Royale"],
            description="Python my first bot",
        )
    , server_secret='RECTjjm7ntrLpoYFh+kDuA/LHONbTYsLEnLMbuCnaU')
    await b.start()

if __name__ == "__main__":
    asyncio.run(main())