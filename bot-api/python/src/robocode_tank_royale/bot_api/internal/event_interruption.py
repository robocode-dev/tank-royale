from typing import Type

from ..events import EventABC

class EventInterruption:
    """
    Manages event interruption settings for bot events.

    This class is responsible for tracking which bot event types are marked as interruptible.
    Events marked as interruptible can interrupt the normal execution flow of bot events.
    """

    _INTERRUPTIBLES: set[Type[EventABC]] = set()
    """
    Set containing all event classes that are currently marked as interruptible.
    """

    def __init__(self):
        """
        Initializes the EventInterruption class.

        This is a private constructor to prevent instantiation of this class.
        """
        raise NotImplementedError("This class cannot be instantiated")

    @staticmethod
    def set_interruptible(event_class: Type[EventABC], interruptible: bool) -> None:
        """
        Sets whether a specific event class should be interruptible or not.

        Args:
            event_class: The class of the event to configure.
            interruptible: True if the event should be interruptible; False otherwise.
        """
        if interruptible:
            EventInterruption._INTERRUPTIBLES.add(event_class)
        else:
            EventInterruption._INTERRUPTIBLES.discard(event_class)

    @staticmethod
    def is_interruptible(event_class: Type[EventABC]) -> bool:
        """
        Checks if a specific event class is marked as interruptible.

        Args:
            event_class: The class of the event to check.

        Returns:
            bool: True if the event is interruptible; False otherwise.
        """
        return event_class in EventInterruption._INTERRUPTIBLES
