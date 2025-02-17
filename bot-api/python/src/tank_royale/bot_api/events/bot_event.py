from tank_royale.bot_api.events.abstract_event import IEvent

class BotEvent(IEvent):
    """Bot event occurring during a battle."""

    def __init__(self, turn_number: int):
        """Initializes a new instance of the Event class.

        Args:
            turn_number: The turn number when the event occurred.
        """
        self.turn_number = turn_number

    def get_turn_number(self) -> int:
        """Returns the turn number when the event occurred.

        Returns:
            The turn number when the event occurred.
        """
        return self.turn_number

    def is_critical(self) -> bool:
        """Indicates if this event is critical.
        
        Critical events should not be removed from event queue when it gets old.

        Returns:
            True if this event is critical; False otherwise. Default is False.
        """
        return False