from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class BulletHitBulletEvent(BotEvent):
    """
    Event triggered when a bullet collides with another bullet in the arena.
    """

    def __init__(self, turn_number: int, bullet: BulletState, hit_bullet: BulletState):
        """
        Initializes a new BulletHitBulletEvent instance, which represents the collision
        of two bullets during a specific turn.

        Args:
            turn_number (int): The turn number when the collision occurred.
            bullet (BulletState): The bullet that collided with another bullet.
            hit_bullet (BulletState): The bullet that was hit during the collision.
        """
        super().__init__(turn_number)
        self.bullet = bullet
        self.hit_bullet = hit_bullet

    def get_bullet(self) -> BulletState:
        """
        Retrieves the bullet that initiated the collision by hitting another bullet.

        Returns:
            BulletState: The bullet that collided with another bullet.
        """
        return self.bullet

    def get_hit_bullet(self) -> BulletState:
        """
        Retrieves the bullet that was hit during the collision with another bullet.

        Returns:
            BulletState: The bullet that was hit by another bullet.
        """
        return self.hit_bullet