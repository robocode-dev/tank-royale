from dataclasses import dataclass

from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class WonRoundEvent(BotEvent):
    """
    Represents an event triggered when a bot has won the round.
    """

    def is_critical(self) -> bool:
        """
        Indicates whether this event is critical.

        Returns:
            bool: Always returns True, as this event is considered critical.
        """
        return True