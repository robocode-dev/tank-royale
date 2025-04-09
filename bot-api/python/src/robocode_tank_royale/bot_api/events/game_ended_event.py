from robocode_tank_royale.bot_api import BotResults
from robocode_tank_royale.bot_api.events import EventABC


class GameEndedEvent(EventABC):
    """
    Event triggered when the game ends.

    Attributes:
        number_of_rounds (int): The total number of rounds played in the game.
        results (BotResults): The results of the battle for this bot.
    """

    def __init__(self, number_of_rounds: int, results: BotResults):
        """
        Initializes the GameEndedEvent with the number of rounds played and the results of the battle.

        Args:
            number_of_rounds (int): The number of rounds that were played in the game.
            results (BotResults): The results of the battle, including the bot's performance data.
        """
        self.number_of_rounds = number_of_rounds
        self.results = results

    def get_number_of_rounds(self) -> int:
        """
        Retrieves the total number of rounds played in the game.

        Returns:
            int: The number of rounds played in the game.
        """
        return self.number_of_rounds

    def get_results(self) -> BotResults:
        """
        Retrieves the results of the battle for this bot.

        Returns:
            BotResults: An object containing the results of the battle, such as scores or rankings.
        """
        return self.results