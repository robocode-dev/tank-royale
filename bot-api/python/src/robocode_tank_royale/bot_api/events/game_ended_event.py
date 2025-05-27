from ..bot_results import BotResults
from .event_abc import EventABC


class GameEndedEvent(EventABC):
    """
    Event triggered when the game ends.

    Attributes:
        number_of_rounds (int): The total number of rounds played in the game.
        results (BotResults): The results of the battle for this bot, containing detailed scores and performance metrics.
    """

    number_of_rounds: int
    results: BotResults
