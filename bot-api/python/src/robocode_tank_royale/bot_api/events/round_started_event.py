from robocode_tank_royale.bot_api.events import EventABC


class RoundStartedEvent(EventABC):
    """
    Represents an event that occurs when a new round has started.
    """

    def __init__(self, round_number: int):
        """
        Initializes a new instance of the RoundStartedEvent class.

        Args:
            round_number (int): The number of the current round.
        """
        self.round_number = round_number

    def get_round_number(self) -> int:
        """
        Retrieves the current round number.

        Returns:
            int: The number of the current round.
        """
        return self.round_number