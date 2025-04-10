from dataclasses import dataclass
from robocode_tank_royale.bot_api.events import EventABC


@dataclass(frozen=True)
class BotEvent(EventABC):
    """
    Represents any event related to a bot during a battle.
    This class serves as the parent for all bot-related events and provides
    default implementations for common bot event methods.

    Attributes:
        turn_number (int): The turn number when this event occurred.
    """
    turn_number: int

    def __post_init__(self) -> None:
        """
        Validates the types and values of attributes after initialization.
        """
        if not isinstance(self.turn_number, int):
            raise TypeError(f"Turn number must be an integer, got {type(self.turn_number).__name__}")
        if self.turn_number < 0:
            raise ValueError(f"Turn number must be a non-negative integer, got {self.turn_number}")

    def is_critical(self) -> bool:
        """
           Determines whether the event is critical.
           By default, events are not critical, but subclasses can override this
           to provide event-specific criticality logic.
           Returns:
               bool: False by default.
           """
        return False
