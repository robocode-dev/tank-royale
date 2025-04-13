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
        super.__post_init__()  # Ensures turn_number validation in BotEvent
        # validate victim_id
        if not isinstance(self.victim_id, int):
            raise TypeError(f"victim_id must be an int, got {type(self.victim_id).__name__}.")
        if self.victim_id < 0:
            raise ValueError(f"victim_id must be a non-negative integer, got {self.victim_id}.")
