from bot_api_py.events.bot_event import BotEvent


class ScannedBotEvent(BotEvent):
    """Event occurring when a bot has scanned another bot."""

    def __init__(self, turn_number: int, scanned_by_bot_id: int, scanned_bot_id: int, energy: float, x: float, y: float, direction: float, speed: float):
        """Initializes a new instance of the ScannedBotEvent class.

        Args:
            turn_number: The turn number when the bot was scanned.
            scanned_by_bot_id: The id of the bot did the scanning.
            scanned_bot_id: The id of the bot that was scanned.
            energy: The energy level of the scanned bot.
            x: The X coordinate of the scanned bot.
            y: The Y coordinate of the scanned bot.
            direction: The direction in degrees of the scanned bot.
            speed: The speed measured in units per turn of the scanned bot.
        """
        super().__init__(turn_number)
        self.scanned_by_bot_id = scanned_by_bot_id
        self.scanned_bot_id = scanned_bot_id
        self.energy = energy
        self.x = x
        self.y = y
        self.direction = direction
        self.speed = speed

    def get_scanned_by_bot_id(self) -> int:
        """Returns the id of the bot did the scanning.

        Returns:
            The id of the bot did the scanning.
        """
        return self.scanned_by_bot_id

    def get_scanned_bot_id(self) -> int:
        """Returns the id of the bot that was scanned.

        Returns:
            The id of the bot that was scanned.
        """
        return self.scanned_bot_id

    def get_energy(self) -> float:
        """Returns the energy level of the scanned bot.

        Returns:
            The energy level of the scanned bot.
        """
        return self.energy

    def get_x(self) -> float:
        """Returns the X coordinate of the scanned bot.

        Returns:
            The X coordinate of the scanned bot.
        """
        return self.x

    def get_y(self) -> float:
        """Returns the Y coordinate of the scanned bot.

        Returns:
            The Y coordinate of the scanned bot.
        """
        return self.y

    def get_direction(self) -> float:
        """Returns the direction in degrees of the scanned bot.

        Returns:
            The direction in degrees of the scanned bot.
        """
        return self.direction

    def get_speed(self) -> float:
        """Returns the Speed measured in units per turn of the scanned bot.

        Returns:
            The Speed measured in units per turn of the scanned bot.
        """
        return self.speed
