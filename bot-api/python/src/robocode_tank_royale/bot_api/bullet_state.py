from dataclasses import dataclass
from robocode_tank_royale.bot_api.color import Color


@dataclass(frozen=True)
class BulletState:
    """
    Represents the state of a bullet that has been fired by a bot.

    Attributes:
        bullet_id (int): Unique identifier for the bullet.
        owner_id (int): Identifier of the bot that fired the bullet.
        power (float): Firepower level of the bullet, which also determines its speed.
        x (float): The X-coordinate of the bullet's position.
        y (float): The Y-coordinate of the bullet's position.
        direction (float): The direction of the bullet in degrees, where 0 points to the right.
        color (Color): The visual color of the bullet.
    """

    bullet_id: int  # Unique identifier for the bullet.
    owner_id: int  # Identifier of the bot that fired the bullet.
    power: float  # Firepower level of the bullet, impacting its speed.
    x: float  # The X-coordinate of the bullet's position.
    y: float  # The Y-coordinate of the bullet's position.
    direction: float  # The direction of the bullet in degrees.
    color: Color  # The color of the bullet.

    def get_speed(self) -> float:
        """
        Calculates and returns the speed of the bullet in units per turn.
        The speed decreases with higher firepower levels.

        Returns:
            float: The calculated speed of the bullet in units per turn.
        """
        return 20 - 3 * self.power
