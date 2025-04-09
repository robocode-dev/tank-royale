from robocode_tank_royale.bot_api.events import BotEvent, Condition


class CustomEvent(BotEvent):
    """Represents a custom event triggered when a specific condition is satisfied."""

    def __init__(self, turn_number: int, condition: Condition):
        """
        Initializes a new instance of the CustomEvent class.

        Args:
            turn_number (int): The number of the turn when the condition was met.
            condition (Condition): The condition that triggered this event.
        """
        super().__init__(turn_number)
        self.condition = condition

    def get_condition(self) -> Condition:
        """
        Retrieves the condition that triggered this event.

        Returns:
            Condition: The condition that was satisfied to trigger this event.
        """
        return self.condition