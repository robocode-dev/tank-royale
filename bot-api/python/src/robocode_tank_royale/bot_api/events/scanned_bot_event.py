from robocode_tank_royale.bot_api.events import BotEvent


class ScannedBotEvent(BotEvent):
    """
    Event triggered when a bot scans another bot.
    """

    def __init__(self, turn_number: int, scanned_by_bot_id: int, scanned_bot_id: int, energy: float, x: float, y: float,
                 direction: float, speed: float):
        """
        Initializes a new instance of the ScannedBotEvent class.

        Args:
            turn_number (int): The turn number when the bot was scanned.
            scanned_by_bot_id (int): The ID of the bot that performed the scanning.
            scanned_bot_id (int): The ID of the bot that was scanned.
            energy (float): The energy level of the scanned bot.
            x (float): The X-coordinate of the scanned bot.
            y (float): The Y-coordinate of the scanned bot.
            direction (float): The direction (in degrees) of the scanned bot.
            speed (float): The speed (in units per turn) of the scanned bot.
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
        """
        Gets the ID of the bot that performed the scanning.

        Returns:
            int: The ID of the bot that performed the scanning.
        """
        return self.scanned_by_bot_id

    def get_scanned_bot_id(self) -> int:
        """
        Gets the ID of the bot that was scanned.

        Returns:
            int: The ID of the bot that was scanned.
        """
        return self.scanned_bot_id

    def get_energy(self) -> float:
        """
        Gets the energy level of the scanned bot.

        Returns:
            float: The energy level of the scanned bot.
        """
        return self.energy

    def get_x(self) -> float:
        """
        Gets the X-coordinate of the scanned bot.

        Returns:
            float: The X-coordinate of the scanned bot.
        """
        return self.x

    def get_y(self) -> float:
        """
        Gets the Y-coordinate of the scanned bot.

        Returns:
            float: The Y-coordinate of the scanned bot.
        """
        return self.y

    def get_direction(self) -> float:
        """
        Gets the direction (in degrees) of the scanned bot.

        Returns:
            float: The direction of the scanned bot in degrees.
        """
        return self.direction

    def get_speed(self) -> float:
        """
        Gets the speed (in units per turn) of the scanned bot.

        Returns:
            float: The speed of the scanned bot in units per turn.
        """
        return self.speed