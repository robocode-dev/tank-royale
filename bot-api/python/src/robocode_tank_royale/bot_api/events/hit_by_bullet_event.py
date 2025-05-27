from dataclasses import dataclass

from ..bullet_state import BulletState
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class HitByBulletEvent(BotEvent):
    """
    Represents an event when a bot is hit by a bullet.

    This event is triggered when a bullet hits the bot during a turn in the game.
    It provides information about the bullet, the damage caused, and the bot's
    remaining energy after the hit.

    Attributes:
        bullet (BulletState): The bullet that hit the bot.
        damage (float): The damage inflicted on the bot by the bullet.
        energy (float): The bot's remaining energy after being hit.

    """

    bullet: BulletState
    damage: float
    energy: float
