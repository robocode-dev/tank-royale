from dataclasses import dataclass

from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class BotDeathEvent(BotEvent):
    """
    Event occurring when another bot has died.

    Attributes:
        victim_id (int): The ID of the bot that has died.
    """

    victim_id: int

    def __post_init__(self) -> None:
        # validate victim_id
        if self.victim_id < 0:
            raise ValueError(f"victim_id must be a non-negative integer, got {self.victim_id}.")
