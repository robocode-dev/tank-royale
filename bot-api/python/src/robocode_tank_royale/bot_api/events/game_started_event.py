from dataclasses import dataclass

from ..game_setup import GameSetup
from ..initial_position import InitialPosition
from .event_abc import EventABC


@dataclass(frozen=True, repr=True)
class GameStartedEvent(EventABC):
    """
    Represents an event triggered when the game starts.

    Attributes:
        my_id (int): The unique identifier for your bot in the current battle.
        initial_position (InitialPosition): The starting position of the bot.
        game_setup (GameSetup): The configuration details for the game that has just started.
    """

    my_id: int
    initial_position: InitialPosition
    game_setup: GameSetup
