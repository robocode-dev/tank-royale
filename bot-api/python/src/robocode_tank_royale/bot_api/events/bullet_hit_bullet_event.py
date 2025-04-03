from robocode_tank_royale.bot_api.bullet_state import BulletState
from robocode_tank_royale.bot_api.events.bot_event import BotEvent


class BulletHitBulletEvent(BotEvent):
    """Event occurring when a bullet has collided with another bullet."""

    def __init__(self, turn_number: int, bullet: BulletState, hit_bullet: BulletState):
        """Initializes a new instance of the BulletHitBulletEvent class.

        Args:
            turn_number: The turn number when the two bullet collided.
            bullet: The bullet that hit another bullet.
            hit_bullet: The other bullet that was hit by the bullet.
        """
        super().__init__(turn_number)
        self.bullet = bullet
        self.hit_bullet = hit_bullet

    def get_bullet(self) -> BulletState:
        """Returns the bullet that hit another bullet.

        Returns:
            The bullet that hit another bullet.
        """
        return self.bullet

    def get_hit_bullet(self) -> BulletState:
        """Returns the other bullet that was hit by the bullet.

        Returns:
            The other bullet that was hit by the bullet.
        """
        return self.hit_bullet
