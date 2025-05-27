from dataclasses import dataclass

from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class HitWallEvent(BotEvent):
    """
    Event occurring when your bot has hit a wall.
    """
    pass
