from typing import Any

from robocode_tank_royale.bot_api.events.bot_event import BotEvent


class TeamMessageEvent(BotEvent):
    """Event occurring when a bot has received a message from a teammate."""

    def __init__(self, turn_number: int, message: Any, sender_id: int):
        """Initializes a new instance of the TeamMessageEvent class.

        Args:
            turn_number: The turn number when the team message was received.
            message: The message that was received.
            sender_id: The id of the teammate that sent the message.

        Raises:
            ValueError: If 'message' is None.
        """
        super().__init__(turn_number)
        if message is None:
            raise ValueError("'message' cannot be None")
        self.message = message
        self.sender_id = sender_id

    def get_message(self) -> Any:
        """Returns the message that was received.

        Returns:
            The message that was received.
        """
        return self.message

    def get_sender_id(self) -> int:
        """Returns the ID of the teammate that sent the message.

        Returns:
            The ID of the teammate that sent the message.
        """
        return self.sender_id