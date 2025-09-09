from dataclasses import dataclass


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

    from .graphics.color import Color

    bullet_id: int
    owner_id: int
    power: float
    x: float
    y: float
    direction: float
    color: Color

    def get_speed(self) -> float:
        """
            Calculates and returns the speed of the bullet in units per turn.
            The speed decreases with higher firepower levels.

            Formula:
                speed = 20 - 3 * power

            Returns:
                float: The calculated speed of the bullet in units per turn.
            """
        return 20 - 3 * self.power

