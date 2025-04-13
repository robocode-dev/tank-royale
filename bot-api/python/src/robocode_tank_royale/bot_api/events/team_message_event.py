from dataclasses import dataclass
from typing import Any
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class TeamMessageEvent(BotEvent):
    """
    Represents an event triggered when a bot has received a message
    from a teammate during a specific turn.

    Attributes:
        message (Any): The message sent by the teammate. Cannot be None.
        sender_id (int): The unique ID of the teammate who sent the message.
    """

    message: Any
    sender_id: int
