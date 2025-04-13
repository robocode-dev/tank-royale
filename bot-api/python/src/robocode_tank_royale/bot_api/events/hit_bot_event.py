from dataclasses import dataclass

from robocode_tank_royale.bot_api.events import BotEvent


@dataclass(frozen=True, repr=True)
class HitBotEvent(BotEvent):
    """
    Represents an event triggered when the bot collides with another bot.

    Attributes:
        victim_id (int): The ID of the bot that your bot collided with.
        energy (float): The remaining energy level of the victim bot.
        x (float): The X coordinate of the victim bot at the time of collision.
        y (float): The Y coordinate of the victim bot at the time of collision.
        is_rammed (bool): Whether the collision was caused by ramming.
    """

    victim_id: int
    energy: float
    x: float
    y: float
    is_rammed: bool
