from dataclasses import dataclass

from ..bullet_state import BulletState
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class BulletHitBulletEvent(BotEvent):
    """
    Event triggered when a bullet collides with another bullet in the arena.

    Attributes:
        bullet (BulletState): The bullet that collided with another bullet.
        hit_bullet (BulletState): The bullet that was hit during the collision.
    """
    bullet: BulletState
    hit_bullet: BulletState
