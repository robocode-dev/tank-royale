import os
import re
from typing import List, Set, Optional

from ..bot_info import BotInfo, InitialPosition


class EnvVars:
    """Utility class for reading environment variables."""

    ServerUrl = "SERVER_URL"
    """Name of environment variable for server URL."""

    ServerSecret = "SERVER_SECRET"
    """Name of environment variable for server secret."""

    BotName = "BOT_NAME"
    """Name of environment variable for bot name."""

    BotVersion = "BOT_VERSION"
    """Name of environment variable for bot version."""

    BotAuthors = "BOT_AUTHORS"
    """Name of environment variable for bot author(s)."""

    BotDescription = "BOT_DESCRIPTION"
    """Name of environment variable for bot description."""

    BotHomepage = "BOT_HOMEPAGE"
    """Name of environment variable for bot homepage URL."""

    BotCountryCodes = "BOT_COUNTRY_CODES"
    """Name of environment variable for bot country code(s)."""

    BotGameTypes = "BOT_GAME_TYPES"
    """Name of environment variable for bot game type(s)."""

    BotPlatform = "BOT_PLATFORM"
    """Name of environment variable for bot platform."""

    BotProgrammingLang = "BOT_PROG_LANG"
    """Name of environment variable for bot programming language."""

    BotInitialPosition = "BOT_INITIAL_POS"
    """Name of environment variable for bot initial position."""

    TeamId = "TEAM_ID"
    """Name of environment variable for bot team id."""

    TeamName = "TEAM_NAME"
    """Name of environment variable for bot team name."""

    TeamVersion = "TEAM_VERSION"
    """Name of environment variable for bot team version."""

    BotBooted = "BOT_BOOTED"
    """Name of environment variable that set if the bot is being booted."""

    MissingEnvValue = "Missing environment variable: "

    @staticmethod
    def get_bot_info() -> BotInfo:
        """
        Gets the bot info from environment variables.

        Returns:
            BotInfo: An object containing the bot's information.

        Raises:
            Exception: If any of the required environment variables are missing.
        """
        bot_name = EnvVars.get_bot_name()
        if not bot_name:
            raise Exception(EnvVars.MissingEnvValue + EnvVars.BotName)

        bot_version = EnvVars.get_bot_version()
        if not bot_version:
            raise Exception(EnvVars.MissingEnvValue + EnvVars.BotVersion)

        authors = EnvVars.get_bot_authors()
        if not authors or all(not s.strip() for s in authors):
            raise Exception(EnvVars.MissingEnvValue + EnvVars.BotAuthors)

        return BotInfo(
            bot_name,
            bot_version,
            authors,
            EnvVars.get_bot_description(),
            EnvVars.get_bot_homepage(),
            EnvVars.get_bot_country_codes(),
            EnvVars.get_bot_game_types(),
            EnvVars.get_bot_platform(),
            EnvVars.get_bot_programming_lang(),
            EnvVars.get_bot_initial_position()
        )

    @staticmethod
    def get_server_url() -> Optional[str]:
        """
        Gets the server URL from environment variables.

        Returns:
            Optional[str]: The server URL, or None if not set.
        """
        return os.getenv(EnvVars.ServerUrl)

    @staticmethod
    def get_server_secret() -> Optional[str]:
        """
        Gets the server secret from environment variables.

        Returns:
            Optional[str]: The server secret, or None if not set.
        """
        return os.getenv(EnvVars.ServerSecret)

    @staticmethod
    def get_bot_name() -> Optional[str]:
        """
        Gets the bot name from environment variables.

        Returns:
            Optional[str]: The bot name, or None if not set.
        """
        return os.getenv(EnvVars.BotName)

    @staticmethod
    def get_bot_version() -> Optional[str]:
        """
        Gets the bot version from environment variables.

        Returns:
            Optional[str]: The bot version, or None if not set.
        """
        return os.getenv(EnvVars.BotVersion)

    @staticmethod
    def get_bot_authors() -> List[str]:
        """
        Gets the bot author(s) from environment variables.

        Returns:
            List[str]: A list of author names, or an empty list if not set.
        """
        return EnvVars._get_env_var_as_list(EnvVars.BotAuthors)

    @staticmethod
    def get_bot_description() -> Optional[str]:
        """
        Gets the bot description from environment variables.

        Returns:
            Optional[str]: The bot description, or None if not set.
        """
        return os.getenv(EnvVars.BotDescription)

    @staticmethod
    def get_bot_homepage() -> Optional[str]:
        """
        Gets the bot homepage URL from environment variables.

        Returns:
            Optional[str]: The bot homepage URL, or None if not set.
        """
        return os.getenv(EnvVars.BotHomepage)

    @staticmethod
    def get_bot_country_codes() -> List[str]:
        """
        Gets the bot country code(s) from environment variables.

        Returns:
            List[str]: A list of country codes, or an empty list if not set.
        """
        return EnvVars._get_env_var_as_list(EnvVars.BotCountryCodes)

    @staticmethod
    def get_bot_game_types() -> Set[str]:
        """
        Gets the list of game type(s) supported by the bot from environment variables.

        Returns:
            Set[str]: A set of game types, or an empty set if not set.
        """
        return set(EnvVars._get_env_var_as_list(EnvVars.BotGameTypes))

    @staticmethod
    def get_bot_platform() -> Optional[str]:
        """
        Gets the platform used for running the game from environment variables.

        Returns:
            Optional[str]: The platform, or None if not set.
        """
        return os.getenv(EnvVars.BotPlatform)

    @staticmethod
    def get_bot_programming_lang() -> Optional[str]:
        """
        Gets the programming language used for running the game from environment variables.

        Returns:
            Optional[str]: The programming language, or None if not set.
        """
        return os.getenv(EnvVars.BotProgrammingLang)

    @staticmethod
    def get_bot_initial_position() -> InitialPosition | None:
        """
        Gets the initial starting position for the bot used for debugging from 
        environment variables.

        Returns:
            InitialPosition: The initial position of the bot.
        """
        init_pos = os.getenv(EnvVars.BotInitialPosition)
        if not init_pos:
            return None
        return InitialPosition.from_string(init_pos)

    @staticmethod
    def get_team_id() -> Optional[int]:
        """
        Gets the bot team ID if provided from environment variables.

        Returns:
            Optional[int]: The team ID, or None if not set.
        """
        team_id = os.getenv(EnvVars.TeamId)
        if team_id is None:
            return None
        team_id = team_id.strip()
        return int(team_id) if team_id else None

    @staticmethod
    def get_team_name() -> Optional[str]:
        """
        Gets the bot team name if provided from environment variables.

        Returns:
            Optional[str]: The team name, or None if not set.
        """
        return os.getenv(EnvVars.TeamName)

    @staticmethod
    def get_team_version() -> Optional[str]:
        """
        Gets the bot team version if provided from environment variables.

        Returns:
            Optional[str]: The team version, or None if not set.
        """
        return os.getenv(EnvVars.TeamVersion)

    @staticmethod
    def is_bot_booted() -> bool:
        """
        Checks if the bot is being booted.

        Returns:
            bool: True if the bot is being booted, False otherwise.
        """
        return os.getenv(EnvVars.BotBooted) is not None

    @staticmethod
    def _get_env_var_as_list(env_var_name: str) -> List[str]:
        """
        Splits an environment variable's value into a list by commas.

        Args:
            env_var_name (str): The name of the environment variable.

        Returns:
            List[str]: A list of strings or an empty list if the variable is not set.
        """
        value = os.getenv(env_var_name)
        return re.split(r'\s*,\s*', value.strip()) if value and value.strip() else []
