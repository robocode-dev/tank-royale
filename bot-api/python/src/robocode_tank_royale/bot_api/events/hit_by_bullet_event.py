from robocode_tank_royale.bot_api.bullet_state import BulletState
from robocode_tank_royale.bot_api.events.bot_event import BotEvent


class HitByBulletEvent(BotEvent):
    """Event occurring when a bullet has hit your bot."""

    def __init__(self, turn_number: int, bullet: BulletState, damage: float, energy: float):
        """Initializes a new instance of the HitByBulletEvent class.

        Args:
            turn_number: The turn number when the bullet has hit a bot.
            bullet: The bullet that hit the bot.
            damage: The damage inflicted by the bullet.
            energy: The remaining energy level of the bot that got hit.
        """
        super().__init__(turn_number)
        self.bullet = bullet
        self.damage = damage
        self.energy = energy

    def get_bullet(self) -> BulletState:
        """Returns the bullet that hit your bot.

        Returns:
            The bullet that hit your bot.
        """
        return self.bullet

    def get_damage(self) -> float:
        """Returns the damage inflicted by the bullet.

        Returns:
            The damage inflicted by the bullet.
        """
        return self.damage

    def get_energy(self) -> float:
        """Returns the remaining energy level after the bullet hit.

        Returns:
            The remaining energy level after the bullet hit.
        """
        return self.energy