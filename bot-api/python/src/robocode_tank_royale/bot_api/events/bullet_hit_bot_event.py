from robocode_tank_royale.bot_api import BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class BulletHitBotEvent(BotEvent):
    """Event occurring when a bullet has hit a bot."""

    def __init__(self, turn_number: int, victim_id: int, bullet: BulletState, damage: float, energy: float):
        """Initializes a new instance of the BulletHitBotEvent class.

        Args:
            turn_number: The turn number when the bullet has hit a bot.
            victim_id: The id of the victim bot that got hit.
            bullet: The bullet that hit the bot.
            damage: The damage inflicted by the bullet.
            energy: The remaining energy level of the bot that got hit.
        """
        super().__init__(turn_number)
        self.victim_id = victim_id
        self.bullet = bullet
        self.damage = damage
        self.energy = energy

    def get_victim_id(self) -> int:
        """Returns the id of the victim bot that got hit.

        Returns:
            The id of the victim bot that got hit.
        """
        return self.victim_id

    def get_bullet(self) -> BulletState:
        """Returns the bullet that hit the bot.

        Returns:
            The bullet that hit the bot.
        """
        return self.bullet

    def get_damage(self) -> float:
        """Returns the damage inflicted by the bullet.

        Returns:
            The damage inflicted by the bullet.
        """
        return self.damage

    def get_energy(self) -> float:
        """Returns the remaining energy level of the bot that got hit.

        Returns:
            The remaining energy level of the bot that got hit.
        """
        return self.energy
