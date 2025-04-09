from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class HitByBulletEvent(BotEvent):
    """
    Represents an event when a bot is hit by a bullet.

    This event is triggered when a bullet hits the bot during a turn in the game.
    It provides information about the bullet, the damage caused, and the bot's
    remaining energy after the hit.
    """

    def __init__(self, turn_number: int, bullet: BulletState, damage: float, energy: float):
        """
        Initializes the HitByBulletEvent.

        Args:
            turn_number (int): The turn number when the bullet hit the bot.
            bullet (BulletState): The bullet that hit the bot.
            damage (float): The damage inflicted on the bot by the bullet.
            energy (float): The bot's remaining energy after being hit.
        """
        super().__init__(turn_number)
        self.bullet = bullet
        self.damage = damage
        self.energy = energy

    def get_bullet(self) -> BulletState:
        """
        Retrieves the bullet that hit the bot.

        Returns:
            BulletState: The bullet that caused the hit.
        """
        return self.bullet

    def get_damage(self) -> float:
        """
        Gets the amount of damage inflicted on the bot by the bullet.

        Returns:
            float: The damage amount.
        """
        return self.damage

    def get_energy(self) -> float:
        """
        Retrieves the bot's remaining energy after the bullet hit.

        Returns:
            float: The remaining energy level of the bot.
        """
        return self.energy