from dataclasses import dataclass

from .event_abc import EventABC


@dataclass(frozen=True, repr=True)
class RoundStartedEvent(EventABC):
    """
    Represents an event that occurs when a new round has started.

    Attributes:
        round_number (int): The number of the current round.
    """

    round_number: int
