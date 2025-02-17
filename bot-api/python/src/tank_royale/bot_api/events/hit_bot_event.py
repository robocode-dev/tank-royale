from tank_royale.bot_api.events.bot_event import BotEvent


class HitBotEvent(BotEvent):
    """Event occurring when a bot has collided with another bot."""

    def __init__(self, turn_number: int, victim_id: int, energy: float, x: float, y: float, is_rammed: bool):
        """Initializes a new instance of the BotHitBotEvent class.

        Args:
            turn_number: The turn number where the bot hit another bot.
            victim_id: The id of the other bot that your bot has collided with.
            energy: The remaining energy level of the victim bot.
            x: The X coordinate of victim bot.
            y: The Y coordinate of victim bot.
            is_rammed: The flag specifying if the victim bot got rammed.
        """
        super().__init__(turn_number)
        self.victim_id = victim_id
        self.energy = energy
        self.x = x
        self.y = y
        self.is_rammed = is_rammed

    def get_victim_id(self) -> int:
        """Returns the id of the other bot that your bot has collided with.

        Returns:
            The id of the other bot that your bot has collided with.
        """
        return self.victim_id

    def get_energy(self) -> float:
        """Returns the remaining energy level of the victim bot.

        Returns:
            The remaining energy level of the victim bot.
        """
        return self.energy

    def get_x(self) -> float:
        """Returns the X coordinate of victim bot.

        Returns:
            The X coordinate of victim bot.
        """
        return self.x

    def get_y(self) -> float:
        """Returns the Y coordinate of victim bot.

        Returns:
            The Y coordinate of victim bot.
        """
        return self.y

    def is_rammed(self) -> bool:
        """Checks if the other bot got rammed by your bot.

        Returns:
            True if the other bot got rammed; False otherwise.
        """
        return self.is_rammed