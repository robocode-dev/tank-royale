from dataclasses import dataclass
from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


@dataclass(frozen=True)
class BulletHitBulletEvent(BotEvent):
    """
    Event triggered when a bullet collides with another bullet in the arena.

    Attributes:
        bullet (BulletState): The bullet that collided with another bullet.
        hit_bullet (BulletState): The bullet that was hit during the collision.
    """

    bullet: BulletState
    hit_bullet: BulletState

    def __post_init__(self) -> None:
        """
        Validates the types of attributes after initialization.
        """
        super().__post_init__()
        if not isinstance(self.bullet, BulletState):
            raise TypeError(f"'bullet' must be of type BulletState, got {type(self.bullet).__name__}")
        if not isinstance(self.hit_bullet, BulletState):
            raise TypeError(f"'hit_bullet' must be of type BulletState, got {type(self.hit_bullet).__name__}")
