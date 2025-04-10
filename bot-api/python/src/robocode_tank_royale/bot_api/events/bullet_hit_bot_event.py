from dataclasses import dataclass
from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


@dataclass(frozen=True)
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

    def __post_init__(self):
        """
        Validates the types of attributes after initialization.
        """
        super().__post_init__()
        if not isinstance(self.victim_id, int):
            raise TypeError(f"victim_id must be an int, got {type(self.victim_id).__name__}")
        if not isinstance(self.bullet, BulletState):
            raise TypeError(f"bullet must be a BulletState, got {type(self.bullet).__name__}")
        if self.damage < 0:
            raise ValueError(f"Damage must be non-negative, got {self.damage}")
