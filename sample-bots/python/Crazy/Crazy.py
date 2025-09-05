from typing import Any, Callable

import asyncio

from robocode_tank_royale import bot_api as ba
from robocode_tank_royale.bot_api.events import (
    HitBotEvent,
    HitWallEvent,
    ScannedBotEvent,
)


def turn_complete_condition(bot: ba.BotABC) -> Callable[[], bool]:
    """Condition that is triggered when the turning is complete."""
    return lambda b = bot: b.turn_remaining == 0


class Crazy(ba.Bot):
    """A sample bot original made for Robocode by Mathew Nelson.

    This robot moves in a zigzag pattern while firing at enemies.
    """

    def __init__(self, *args: Any, **kwargs: Any):
        super().__init__(*args, **kwargs)
        self._moving_forward = False

    async def run(self) -> None:
        self.body_color = ba.Color.from_rgb(0x00, 0xC8, 0x00)  # lime
        self.turent_color = ba.Color.from_rgb(0x00, 0x96, 0x32)  # green
        self.radar_color = ba.Color.from_rgb(0x00, 0x64, 0x64)  # dark cyan
        self.bullet_color = ba.Color.from_rgb(0xFF, 0xFF, 0x64)  # yellow
        self.scan_color = ba.Color.from_rgb(0xFF, 0xC8, 0xC8)  # light red

        while self.is_running():
            # Tell the game we will want to move ahead 40000 -- some large number
            self.set_forward(40000)
            self._moving_forward = True
            # Tell the game we will want to turn right 90
            self.set_turn_left(90)
            # At this point, we have indicated to the game that *when we do something*,
            # we will want to move ahead and turn right. That's what "set" means.
            # It is important to realize we have not done anything yet!
            # In order to actually move, we'll want to call a method that takes real time, such as
            # waitFor.
            # waitFor actually starts the action -- we start moving and turning.
            # It will not return until we have finished turning.
            await self.wait_for(turn_complete_condition(self))

            # Note: We are still moving ahead now, but the turn is complete.
            # Now we'll turn the other way...
            self.set_turn_right(180)
            # ... and wait for the turn to finish ...
            await self.wait_for(turn_complete_condition(self))
            # ... then the other way ...
            self.set_turn_left(180)
            # ... and wait for that turn to finish.
            await self.wait_for(turn_complete_condition(self))
            # then back to the top to do it all again.

    def _reverse_direction(self) -> None:
        if self._moving_forward:
            self.set_back(40000)
            self._moving_forward = False
        else:
            self.set_forward(40000)
            self._moving_forward = True

    async def on_hit_wall(self, bot_hit_wall_event: HitWallEvent) -> None:
        del bot_hit_wall_event
        # Bounce off!
        self._reverse_direction()

    async def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        await self.fire(1)

    async def on_hit_bot(self, bot_hit_bot_event: HitBotEvent) -> None:
        # If we're moving into the other bot, reverse!
        if bot_hit_bot_event.is_rammed:
            self._reverse_direction()


async def main():
    b = Crazy()
    await b.start()


if __name__ == "__main__":
    asyncio.run(main())
