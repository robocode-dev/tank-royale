from robocode_tank_royale.bot_api import BotState, BulletState
from robocode_tank_royale.bot_api.events import BotEvent


class TickEvent(BotEvent):
    """
    Represents an event that occurs at the start of a new turn within a round.
    """

    def __init__(self, turn_number: int, round_number: int, bot_state: BotState,
                 bullet_states: list[BulletState], events: list[BotEvent]):
        """
        Initializes a TickEvent representing the current game state at the start of a turn.

        Args:
            turn_number (int): The current turn number in the battle.
            round_number (int): The current round number in the battle.
            bot_state (BotState): The current state of the bot.
            bullet_states (list[BulletState]): A list containing the states of bullets fired by the bot.
            events (list[BotEvent]): A list of events that occurred in this turn.
        """
        super().__init__(turn_number)
        self.round_number = round_number
        self.bot_state = bot_state
        self.bullet_states = bullet_states
        self.events = events

    def get_round_number(self) -> int:
        """
        Gets the current round number.

        Returns:
            int: The current round number.
        """
        return self.round_number

    def get_bot_state(self) -> BotState:
        """
        Gets the current state of the bot.

        Returns:
            BotState: The current state of the bot.
        """
        return self.bot_state

    def get_bullet_states(self) -> list[BulletState]:
        """
        Gets the states of the bullets fired by the bot.

        Returns:
            list[BulletState]: A list of bullet states.
        """
        return self.bullet_states

    def get_events(self) -> list[BotEvent]:
        """
        Gets the events that occurred for the bot within the turn.

        Returns:
            list[BotEvent]: A list of events relevant to the bot in this turn.
        """
        return self.events