from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class BulletHitBotEvent(BotEvent):
    """Represents an event triggered when a bullet hits a bot."""

    def __init__(self, turn_number: int, victim_id: int, bullet: BulletState, damage: float, energy: float):
        """
        Initializes a BulletHitBotEvent instance.

        Args:
            turn_number (int): The turn number when the bullet hit the bot.
            victim_id (int): The ID of the bot that was hit.
            bullet (BulletState): The bullet that hit the bot.
            damage (float): The damage caused by the bullet.
            energy (float): The remaining energy of the bot that was hit.
        """
        super().__init__(turn_number)
        self.victim_id = victim_id
        self.bullet = bullet
        self.damage = damage
        self.energy = energy

    def get_victim_id(self) -> int:
        """
        Gets the ID of the bot that was hit.

        Returns:
            int: The ID of the bot that was hit.
        """
        return self.victim_id

    def get_bullet(self) -> BulletState:
        """
        Gets the bullet that hit the bot.

        Returns:
            BulletState: The bullet that hit the bot.
        """
        return self.bullet

    def get_damage(self) -> float:
        """
        Gets the damage caused by the bullet.

        Returns:
            float: The damage caused by the bullet.
        """
        return self.damage

    def get_energy(self) -> float:
        """
        Gets the remaining energy of the bot that was hit.

        Returns:
            float: The remaining energy of the bot that was hit.
        """
        return self.energy