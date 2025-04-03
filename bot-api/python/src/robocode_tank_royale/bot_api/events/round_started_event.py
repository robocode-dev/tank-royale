from robocode_tank_royale.bot_api.events import EventABC


class RoundStartedEvent(EventABC):
    """Event occurring when a new round has just started."""

    def __init__(self, round_number: int):
        """Initializes a new instance of the RoundStartedEvent class.

        Args:
            round_number: The round number.
        """
        self.round_number = round_number

    def get_round_number(self) -> int:
        """Returns the round number.

        Returns:
            The round number.
        """
        return self.round_number
