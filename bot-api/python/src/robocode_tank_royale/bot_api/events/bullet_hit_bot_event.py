from dataclasses import dataclass

from ..bullet_state import BulletState
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class BulletHitBotEvent(BotEvent):
    """
    Represents an event triggered when a bullet hits a bot.

    Attributes:
        victim_id (int): The ID of the bot that was hit.
        bullet (BulletState): The bullet that hit the bot.
        damage (float): The damage caused by the bullet.
        energy (float): The remaining energy of the bot that was hit.
            Starts at 100.0 (fully operational) and decreases with damage.
            Energy 0.0 represents a disabled bot. Negative energy
            indicates the bot is dead (e.g., -1 for a dead bot).
    """

    victim_id: int
    bullet: BulletState
    damage: float
    energy: float
