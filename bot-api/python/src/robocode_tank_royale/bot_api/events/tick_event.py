from typing import Sequence

from dataclasses import dataclass

from ..bot_state import BotState
from ..bullet_state import BulletState
from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class TickEvent(BotEvent):
    """
    Represents an event that occurs at the start of a new turn within a round.

    Attributes:
        round_number (int): The current round number in the battle.
        bot_state (BotState): The current state of the bot.
        bullet_states (list[BulletState]): A list containing the states of bullets fired by the bot.
        events (list[BotEvent]): A list of events that occurred in this turn.
    """

    round_number: int
    bot_state: BotState | None
    bullet_states: Sequence[BulletState | None] | None
    events: Sequence[BotEvent]
