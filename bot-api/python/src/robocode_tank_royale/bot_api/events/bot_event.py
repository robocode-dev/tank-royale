from dataclasses import dataclass

from .event_abc import EventABC


@dataclass(frozen=True, repr=True)
class BotEvent(EventABC):
    """
    Represents any event related to a bot during a battle.
    This class serves as the parent for all bot-related events and provides
    default implementations for common bot event methods.

    Attributes:
        turn_number (int): The turn number when this event occurred.
    """
    turn_number: int

    def is_critical(self) -> bool:
        """
           Determines whether the event is critical.
           By default, events are not critical, but subclasses can override this
           to provide event-specific criticality logic.
           Returns:
               bool: False by default.
           """
        return False
