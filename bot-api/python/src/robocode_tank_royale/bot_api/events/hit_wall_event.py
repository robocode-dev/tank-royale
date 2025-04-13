from dataclasses import dataclass

from robocode_tank_royale.bot_api.events import BotEvent


@dataclass(frozen=True, repr=True)
class HitWallEvent(BotEvent):
    """
    Event occurring when your bot has hit a wall.
    """
    pass
