from robocode_tank_royale.bot_api.bullet_state import BulletState
from robocode_tank_royale.bot_api.events.bot_event import BotEvent


class BulletFiredEvent(BotEvent):
    """Event occurring when a bullet has been fired from a bot."""

    def __init__(self, turn_number: int, bullet: BulletState):
        """Initializes a new instance of the BulletFiredEvent class.

        Args:
            turn_number: The turn number when the bullet was fired.
            bullet: The bullet that was fired.
        """
        super().__init__(turn_number)
        self.bullet = bullet

    def get_bullet(self) -> BulletState:
        """Returns the bullet that was fired.

        Returns:
            The bullet that was fired.
        """
        return self.bullet
