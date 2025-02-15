from tank_royale.bot_api.events.bot_event import BotEvent
from tank_royale.bot_api.events.condition import Condition


class CustomEvent(BotEvent):
    """A custom event occurring when a condition has been met."""

    def __init__(self, turn_number: int, condition: Condition):
        """Initializes a new instance of the CustomEvent class.

        Args:
            turn_number: The turn number when the condition was met.
            condition: The condition that has been met.
        """
        super().__init__(turn_number)
        self.condition = condition

    def get_condition(self) -> Condition:
        """Returns the condition that was met to trigger this custom event.

        Returns:
            The condition that was met to trigger this custom event.
        """
        return self.condition