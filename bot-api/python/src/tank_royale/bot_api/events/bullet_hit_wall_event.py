from tank_royale.bot_api.bullet_state import BulletState
from tank_royale.bot_api.events.bot_event import BotEvent


class BulletHitWallEvent(BotEvent):
    """Event occurring when a bullet has hit a wall."""

    def __init__(self, turn_number: int, bullet: BulletState):
        """Initializes a new instance of the BulletHitWallEvent class.

        Args:
            turn_number: The turn number when the bullet has hit a wall.
            bullet: The bullet that has hit a wall.
        """
        super().__init__(turn_number)
        self.bullet = bullet

    def get_bullet(self) -> BulletState:
        """Returns the bullet that has hit a wall.

        Returns:
            The bullet that has hit a wall.
        """
        return self.bullet
