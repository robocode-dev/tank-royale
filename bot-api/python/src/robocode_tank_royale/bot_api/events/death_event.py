from dataclasses import dataclass

from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class DeathEvent(BotEvent):
    """
    Represents an event triggered when the bot has died.
    """

    def is_critical(self) -> bool:
        """
        Overrides the default implementation to mark this event as critical.
        Returns:
            bool: Always returns True since a death event is critical.
        """
        return True
