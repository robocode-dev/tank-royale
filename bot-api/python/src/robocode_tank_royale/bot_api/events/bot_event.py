from robocode_tank_royale.bot_api.events import EventABC


class BotEvent(EventABC):
    """Represents an event that occurs during a battle in the Bot API."""

    def __init__(self, turn_number: int):
        """
        Initializes a new instance of the BotEvent class.

        Args:
            turn_number (int): The turn number when the event occurred.
        """
        self.turn_number = turn_number

    def get_turn_number(self) -> int:
        """
        Retrieves the turn number when the event occurred.

        Returns:
            int: The turn number associated with the event.
        """
        return self.turn_number

    def is_critical(self) -> bool:
        """
        Checks if the event is critical.

        Critical events should not be removed from the event queue when they become old.

        Returns:
            bool: True if the event is critical; otherwise, False. Defaults to False.
        """
        return False
