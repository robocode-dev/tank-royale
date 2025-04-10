from dataclasses import dataclass
from robocode_tank_royale.bot_api.events import BotEvent


@dataclass(frozen=True)
class BotDeathEvent(BotEvent):
    """
    Event occurring when another bot has died.

    Attributes:
        victim_id (int): The ID of the bot that has died.
    """

    victim_id: int

    def __post_init__(self) -> None:
        """
        Validates the types and values of attributes after initialization.
        """
        if not isinstance(self.victim_id, int):
            raise TypeError(f"victim_id must be an int, got {type(self.victim_id).__name__}.")
        if self.victim_id < 0:
            raise ValueError(f"victim_id must be a non-negative integer, got {self.victim_id}.")
