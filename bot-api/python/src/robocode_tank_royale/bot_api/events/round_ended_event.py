from dataclasses import dataclass

from ..bot_results import BotResults
from .event_abc import EventABC


@dataclass(frozen=True, repr=True)
class RoundEndedEvent(EventABC):
    """
    Represents an event triggered when a round has ended in Robocode.

    Attributes:
        round_number (int): The number of the round that just ended.
        turn_number (int): The final turn number of the round.
        results (BotResults): The accumulated results of all bots at the end of the round.
    """

    round_number: int
    turn_number: int
    results: BotResults
