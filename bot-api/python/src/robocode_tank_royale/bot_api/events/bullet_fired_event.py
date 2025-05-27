from dataclasses import dataclass

from ..bullet_state import BulletState
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class BulletFiredEvent(BotEvent):
    """
    Represents an event that occurs when a bullet is fired from a bot.

    This event contains information about the bullet that was fired and
    the turn number during which the event occurred.

    Attributes:
        bullet (BulletState): The state of the bullet that was fired.
    """
    bullet: BulletState
