from bot_api_py.bot_results import BotResults
from bot_api_py.events.abstract_event import IEvent


class GameEndedEvent(IEvent):
    """Event occurring when game has just ended."""

    def __init__(self, number_of_rounds: int, results: BotResults):
        """Initializes a new instance of the GameEndedEvent class.

        Args:
            number_of_rounds: The number of rounds played.
            results: The bot results of the battle.
        """
        self.number_of_rounds = number_of_rounds
        self.results = results

    def get_number_of_rounds(self) -> int:
        """Returns the number of rounds played.

        Returns:
            The number of rounds played.
        """
        return self.number_of_rounds

    def get_results(self) -> BotResults:
        """Returns the results of the battle.

        Returns:
            The results of the battle.
        """
        return self.results