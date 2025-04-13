from dataclasses import dataclass

from robocode_tank_royale.bot_api import BulletState
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class BulletHitWallEvent(BotEvent):
    """
    Represents an event that occurs when a bullet hits a wall during a game.

    This event provides information about which bullet hit the wall and at
    what turn the event occurred.

    Attributes:
        bullet (BulletState): The bullet that hit the wall.

    """

    bullet: BulletState
