from bot_api_py.bot_state import BotState
from bot_api_py.bullet_state import BulletState
from bot_api_py.events.bot_event import BotEvent


class TickEvent(BotEvent):
    """Event occurring whenever a new turn in a round has started."""

    def __init__(self, turn_number: int, round_number: int, bot_state: BotState,
                 bullet_states: list[BulletState], events: list[BotEvent]):
        """Initializes a new instance of the TickEvent class.

        Args:
            turn_number: The current turn number in the battle.
            round_number: The current round number in the battle.
            bot_state: The current state of this bot.
            bullet_states: The current state of the bullets fired by this bot.
            events: The events occurring in the turn relevant for this bot.
        """
        super().__init__(turn_number)
        self.round_number = round_number
        self.bot_state = bot_state
        self.bullet_states = bullet_states
        self.events = events

    def get_round_number(self) -> int:
        """Returns the current round number.

        Returns:
            The current round number.
        """
        return self.round_number

    def get_bot_state(self) -> BotState:
        """Returns the current state of this bot.

        Returns:
            The current state of this bot.
        """
        return self.bot_state

    def get_bullet_states(self) -> list[BulletState]:
        """Returns the current state of the bullets fired by this bot.

        Returns:
            The current state of the bullets fired by this bot.
        """
        return self.bullet_states

    def get_events(self) -> list[BotEvent]:
        """Returns the events that occurred for the bot within the turn.

        Returns:
            The events that occurred for the bot within the turn.
        """
        return self.events