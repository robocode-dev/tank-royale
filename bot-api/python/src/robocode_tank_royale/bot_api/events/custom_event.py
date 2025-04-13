from dataclasses import dataclass

from .bot_event import BotEvent
from .condition import Condition


@dataclass(frozen=True, repr=True)
class CustomEvent(BotEvent):
    """
    Represents a custom event triggered when a specific condition is satisfied.

    Attributes:
        condition (Condition): The condition that was satisfied to trigger this event.
    """

    condition: Condition
