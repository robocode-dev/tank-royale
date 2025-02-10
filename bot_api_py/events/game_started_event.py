from bot_api_py.initial_position import InitialPosition
from bot_api_py.game_setup import GameSetup
from bot_api_py.events.abstract_event import IEvent


class GameStartedEvent(IEvent):
    """Event occurring when game has just started."""

    def __init__(self, my_id: int, initial_position: InitialPosition, game_setup: GameSetup):
        """Initializes a new instance of the GameStartedEvent class.

        Args:
            my_id: The id used for identifying your bot in the current battle.
            initial_position: The initial position of the bot.
            game_setup: The game setup for the battle just started.
        """
        self.my_id = my_id
        self.initial_position = initial_position
        self.game_setup = game_setup

    def get_my_id(self) -> int:
        """Returns the id used for identifying your bot in the current battle.

        Returns:
            The id used for identifying your bot.
        """
        return self.my_id

    def get_initial_position(self) -> InitialPosition:
        """Returns the start position of the bot.

        Returns:
            The start position of the bot.
        """
        return self.initial_position

    def get_game_setup(self) -> GameSetup:
        """Returns the game setup for the battle just started.

        Returns:
            The game setup for the battle just started.
        """
        return self.game_setup