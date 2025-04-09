from robocode_tank_royale.bot_api import BotResults
from robocode_tank_royale.bot_api.events import EventABC


class RoundEndedEvent(EventABC):
    """Represents an event triggered when a round has ended in Robocode."""

    def __init__(self, round_number: int, turn_number: int, results: BotResults):
        """
        Initializes a new instance of the `RoundEndedEvent` class.

        Args:
            round_number (int): The number of the round that just ended.
            turn_number (int): The final turn number of the round.
            results (BotResults): The accumulated results of all bots at the end of the round.
        """
        self.round_number = round_number
        self.turn_number = turn_number
        self.results = results

    def get_round_number(self) -> int:
        """
        Retrieves the number of the round that just ended.

        Returns:
            int: The round number.
        """
        return self.round_number

    def get_turn_number(self) -> int:
        """
        Retrieves the last turn number of the just-ended round.

        Returns:
            int: The turn number.
        """
        return self.turn_number

    def get_results(self) -> BotResults:
        """
        Retrieves the cumulative results of bots at the end of the round.

        Returns:
            BotResults: The accumulated bot results at the end of the round.
        """
        return self.results