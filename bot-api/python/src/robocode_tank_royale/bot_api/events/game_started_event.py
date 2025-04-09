from robocode_tank_royale.bot_api import GameSetup, InitialPosition
from robocode_tank_royale.bot_api.events import EventABC


class GameStartedEvent(EventABC):
    """Represents an event triggered when the game starts."""

    def __init__(self, my_id: int, initial_position: InitialPosition, game_setup: GameSetup):
        """
        Initializes a new instance of the GameStartedEvent class.

        Args:
            my_id (int): The unique identifier for your bot in the current battle.
            initial_position (InitialPosition): The starting position of the bot.
            game_setup (GameSetup): The configuration details for the game that has just started.
        """
        self.my_id = my_id
        self.initial_position = initial_position
        self.game_setup = game_setup

    def get_my_id(self) -> int:
        """
        Retrieves the unique identifier for the bot.

        Returns:
            int: The unique identifier assigned to the bot for the current battle.
        """
        return self.my_id

    def get_initial_position(self) -> InitialPosition:
        """
        Retrieves the initial position of the bot.

        Returns:
            InitialPosition: The starting position of the bot in the arena.
        """
        return self.initial_position

    def get_game_setup(self) -> GameSetup:
        """
        Retrieves the details of the game's configuration.

        Returns:
            GameSetup: The setup/configuration of the game that has just started.
        """
        return self.game_setup