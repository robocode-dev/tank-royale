from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class BulletFiredEvent(BotEvent):
    """
    Represents an event that occurs when a bullet is fired from a bot.
    """

    def __init__(self, turn_number: int, bullet: BulletState):
        """
        Initializes a new instance of the BulletFiredEvent class.

        Args:
            turn_number (int): The turn number during which the bullet was fired.
            bullet (BulletState): The state of the bullet that was fired.
        """
        super().__init__(turn_number)
        self.bullet = bullet

    def get_bullet(self) -> BulletState:
        """
        Retrieves the bullet associated with this event.

        Returns:
            BulletState: The state of the bullet that was fired.
        """
        return self.bullet
