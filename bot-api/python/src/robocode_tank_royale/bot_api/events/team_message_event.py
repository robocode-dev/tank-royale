from typing import Any
from robocode_tank_royale.bot_api.events import BotEvent


class TeamMessageEvent(BotEvent):
    """
    Represents an event triggered when a bot has received a message
    from a teammate during a specific turn.
    """

    def __init__(self, turn_number: int, message: Any, sender_id: int):
        """
        Initializes a new instance of the TeamMessageEvent class.

        Args:
            turn_number (int): The turn number during which the team message was received.
            message (Any): The message sent by the teammate. Cannot be None.
            sender_id (int): The unique ID of the teammate who sent the message.

        Raises:
            ValueError: If the 'message' argument is None.
        """
        super().__init__(turn_number)
        if message is None:
            raise ValueError("'message' cannot be None")
        self.message = message
        self.sender_id = sender_id

    def get_message(self) -> Any:
        """
        Retrieves the message that was received from the teammate.

        Returns:
            Any: The content of the message received.
        """
        return self.message

    def get_sender_id(self) -> int:
        """
        Retrieves the unique identifier of the teammate who sent the message.

        Returns:
            int: The ID of the teammate who sent the message.
        """
        return self.sender_id