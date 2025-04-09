from robocode_tank_royale.bot_api.events import BotEvent


class HitBotEvent(BotEvent):
    """Represents an event triggered when the bot collides with another bot."""

    def __init__(self, turn_number: int, victim_id: int, energy: float, x: float, y: float, is_rammed: bool):
        """
        Initializes a new instance of the `HitBotEvent` class.

        Args:
            turn_number (int): The turn number during which the collision occurred.
            victim_id (int): The ID of the bot that your bot collided with.
            energy (float): The remaining energy level of the victim bot.
            x (float): The X coordinate of the victim bot at the time of collision.
            y (float): The Y coordinate of the victim bot at the time of collision.
            is_rammed (bool): Whether the collision was caused by ramming.
        """
        super().__init__(turn_number)
        self.victim_id = victim_id
        self.energy = energy
        self.x = x
        self.y = y
        self.is_rammed = is_rammed

    def get_victim_id(self) -> int:
        """
        Gets the ID of the bot that your bot collided with.

        Returns:
            int: The ID of the collided bot.
        """
        return self.victim_id

    def get_energy(self) -> float:
        """
        Gets the remaining energy level of the victim bot.

        Returns:
            float: The energy level of the victim bot.
        """
        return self.energy

    def get_x(self) -> float:
        """
        Gets the X coordinate of the victim bot at the time of collision.

        Returns:
            float: The X coordinate of the victim bot.
        """
        return self.x

    def get_y(self) -> float:
        """
        Gets the Y coordinate of the victim bot at the time of collision.

        Returns:
            float: The Y coordinate of the victim bot.
        """
        return self.y

    def is_rammed(self) -> bool:
        """
        Determines whether the collision was caused by ramming.

        Returns:
            bool: `True` if the collision was caused by ramming; otherwise, `False`.
        """
        return self.is_rammed