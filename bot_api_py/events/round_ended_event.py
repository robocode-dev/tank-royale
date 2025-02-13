from bot_api_py.bot_results import BotResults
from bot_api_py.events.abstract_event import IEvent


class RoundEndedEvent(IEvent):
    """Event occurring when a round has just ended."""

    def __init__(self, round_number: int, turn_number: int, results: BotResults):
        """Initializes a new instance of the RoundEndedEvent class.

        Args:
            round_number: The round number.
            turn_number: The turn number.
            results: The accumulated bot results at the end of the round.
        """
        self.round_number = round_number
        self.turn_number = turn_number
        self.results = results

    def get_round_number(self) -> int:
        """Returns the round number.

        Returns:
            The round number.
        """
        return self.round_number

    def get_turn_number(self) -> int:
        """Returns the turn number.

        Returns:
            The turn number.
        """
        return self.turn_number

    def get_results(self) -> BotResults:
        """Returns the accumulated bot results at the end of the round.

        Returns:
            The accumulated bot results at the end of the round.
        """
        return self.results