from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class BulletHitWallEvent(BotEvent):
    """
    Represents an event that occurs when a bullet hits a wall during a game.

    This event provides information about which bullet hit the wall and at
    what turn the event occurred.
    """

    def __init__(self, turn_number: int, bullet: BulletState):
        """
        Initializes a new instance of the BulletHitWallEvent class.

        Args:
            turn_number (int): The turn number when the bullet hit the wall.
            bullet (BulletState): The bullet that hit the wall.
        """
        super().__init__(turn_number)
        self.bullet = bullet

    def get_bullet(self) -> BulletState:
        """
        Retrieves the bullet that hit the wall.

        Returns:
            BulletState: The bullet that hit the wall.
        """
        return self.bullet