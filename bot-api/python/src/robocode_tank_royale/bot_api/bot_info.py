import json
from typing import List, Set, Optional

from .initial_position import InitialPosition


class BotInfo:
    """
    BotInfo class contains the properties of a bot. Implements validation and supports a builder pattern.
    """

    # Constants for validation
    MAX_NAME_LENGTH = 30
    """Maximum number of characters accepted for the name."""

    MAX_VERSION_LENGTH = 20
    """Maximum number of characters accepted for the version."""

    MAX_AUTHOR_LENGTH = 50
    """Maximum number of characters accepted for an author name."""

    MAX_DESCRIPTION_LENGTH = 250
    """Maximum number of characters accepted for the description."""

    MAX_HOMEPAGE_LENGTH = 150
    """Maximum number of characters accepted for the link to the homepage."""

    MAX_GAME_TYPE_LENGTH = 20
    """Maximum number of characters accepted for a game type."""

    MAX_PLATFORM_LENGTH = 30
    """Maximum number of characters accepted for the platform name."""

    MAX_PROGRAMMING_LANG_LENGTH = 30
    """Maximum number of characters accepted for the programming language name."""

    MAX_NUMBER_OF_AUTHORS = 5
    """Maximum number of authors allowed."""

    MAX_NUMBER_OF_COUNTRY_CODES = 5
    """Maximum number of country codes allowed."""

    MAX_NUMBER_OF_GAME_TYPES = 10
    """Maximum number of game types allowed."""

    def __init__(
            self,
            name: str,
            version: str,
            authors: List[str],
            description: Optional[str] = None,
            homepage: Optional[str] = None,
            country_codes: Optional[List[str]] = None,
            game_types: Optional[Set[str]] = None,
            platform: Optional[str] = None,
            programming_lang: Optional[str] = None,
            initial_position: Optional[InitialPosition] = None,
    ):
        """
        Initializes a new instance of the BotInfo class.

        Note:
            The recommended method for creating a BotInfo instance is to use the `IBuilder` interface
            provided with the `BotInfo.builder()` static method.

        Attributes:
            name (str): The name of the bot (required).
            version (str): The version of the bot (required).
            authors (list): The author(s) of the bot (required).
            description (str): A short description of the bot (optional).
            homepage (str): A link to a homepage for the bot (optional).
            country_codes (list): The country code(s) for the bot (optional).
            game_types (set): The game types that this bot can handle (optional).
            platform (str): The platform used for running the bot (optional).
            programming_lang (str): The programming language used for developing the bot (optional).
            initial_position (InitialPosition): The initial position with starting coordinate and angle (optional).
        """

        # Required fields:
        self.name = self._process_name(name)
        self.version = self._process_version(version)
        self.authors = self._process_authors(authors)
        # Optional fields:
        self.description = self._process_description(description)
        self.homepage = self._process_homepage(homepage)
        self.country_codes = self._process_country_codes(country_codes or [])
        self.game_types = self._process_game_types(game_types or set())
        self.platform = self._process_platform(platform)
        self.programming_lang = self._process_programming_lang(programming_lang)
        # Optional special field:
        self.initial_position = initial_position

    # Validation and processing methods
    @staticmethod
    def _process_name(name: str) -> str:
        if not name or len(name.strip()) == 0:
            raise ValueError("'name' cannot be null, empty, or blank")
        name = name.strip()
        if len(name) > BotInfo.MAX_NAME_LENGTH:
            raise ValueError(f"'name' length exceeds {BotInfo.MAX_NAME_LENGTH} characters")
        return name

    @staticmethod
    def _process_version(version: str) -> str:
        if not version or len(version.strip()) == 0:
            raise ValueError("'version' cannot be null, empty, or blank")
        version = version.strip()
        if len(version) > BotInfo.MAX_VERSION_LENGTH:
            raise ValueError(f"'version' length exceeds {BotInfo.MAX_VERSION_LENGTH} characters")
        return version

    @classmethod
    def _process_authors(cls, authors: List[str]) -> List[str]:
        if not authors or len(authors) == 0:
            raise ValueError("'authors' cannot be null or empty")
        if len(authors) > cls.MAX_NUMBER_OF_AUTHORS:
            raise ValueError(f"Number of 'authors' exceeds {cls.MAX_NUMBER_OF_AUTHORS}")
        processed_authors: List[str] = []
        for author in authors:
            if not author or not author.strip():
                raise ValueError("'authors' cannot contain blank values")
            author = author.strip()
            if len(author) > cls.MAX_AUTHOR_LENGTH:
                raise ValueError(f"'author' length exceeds {cls.MAX_AUTHOR_LENGTH} characters")
            processed_authors.append(author)
        return processed_authors

    @classmethod
    def _process_description(cls, description: Optional[str]) -> Optional[str]:
        if description and len(description.strip()) > cls.MAX_DESCRIPTION_LENGTH:
            raise ValueError(f"'description' length exceeds {cls.MAX_DESCRIPTION_LENGTH} characters")
        return description.strip() if description else None

    @classmethod
    def _process_homepage(cls, homepage: Optional[str]) -> Optional[str]:
        if homepage and len(homepage.strip()) > cls.MAX_HOMEPAGE_LENGTH:
            raise ValueError(f"'homepage' length exceeds {cls.MAX_HOMEPAGE_LENGTH} characters")
        return homepage.strip() if homepage else None

    @classmethod
    def _process_country_codes(cls, country_codes: List[str]) -> List[str]:
        if len(country_codes) > cls.MAX_NUMBER_OF_COUNTRY_CODES:
            raise ValueError(f"'country_codes' must not exceed {cls.MAX_NUMBER_OF_COUNTRY_CODES}")
        return [code.strip() for code in country_codes if code.strip()]

    @classmethod
    def _process_game_types(cls, game_types: Set[str]) -> Set[str]:
        if len(game_types) > cls.MAX_NUMBER_OF_GAME_TYPES:
            raise ValueError(f"'game_types' must not exceed {cls.MAX_NUMBER_OF_GAME_TYPES}")
        processed: Set[str] = set()
        for gt in game_types:
            if gt is None:
                continue
            s = gt.strip()
            if not s:
                continue
            if len(s) > cls.MAX_GAME_TYPE_LENGTH:
                raise ValueError(f"'game_type' length exceeds {cls.MAX_GAME_TYPE_LENGTH} characters")
            processed.add(s)
        return processed

    @classmethod
    def _process_platform(cls, platform: Optional[str]) -> str:
        platform = platform.strip() if platform else f"Python {cls.MAX_PLATFORM_LENGTH}"
        if len(platform) > cls.MAX_PLATFORM_LENGTH:
            raise ValueError(f"'platform' length exceeds {cls.MAX_PLATFORM_LENGTH} characters")
        return platform

    @classmethod
    def _process_programming_lang(cls, programming_lang: Optional[str]) -> Optional[str]:
        if programming_lang and len(programming_lang.strip()) > cls.MAX_PROGRAMMING_LANG_LENGTH:
            raise ValueError(f"'programming_lang' length exceeds {cls.MAX_PROGRAMMING_LANG_LENGTH} characters")
        return programming_lang.strip() if programming_lang else None

    # Parse JSON content
    @classmethod
    def from_json(cls, json_data: str) -> "BotInfo":
        data = json.loads(json_data)
        return cls(
            name=data["name"],
            version=data["version"],
            authors=data["authors"],
            description=data.get("description"),
            homepage=data.get("homepage"),
            country_codes=data.get("countryCodes"),
            game_types=set(data["gameTypes"]) if data.get("gameTypes") else None,
            platform=data.get("platform"),
            programming_lang=data.get("programmingLang"),
            initial_position=data.get("initialPosition"),
        )

    @classmethod
    def from_file(cls, file_path: str) -> "BotInfo":
        """
        Reads the bot info from a JSON file.

        Args:
            file_path (str): The path to the JSON file containing bot info.

        Returns:
            BotInfo: An instance of BotInfo populated with data from the file.
        """
        with open(file_path, 'r') as file:
            json_data = file.read()
        return cls.from_json(json_data)

    # Support builder pattern
    class Builder:
        """
        Builder interface for creating builder objects for constructing BotInfo instances.
        Supports method chaining.
        """

        def __init__(self):
            self.name = None
            self.version = None
            self.authors = []
            self.description = None
            self.homepage = None
            self.country_codes = []
            self.game_types: set[str] = set()
            self.platform = None
            self.programming_lang = None
            self.initial_position = None

        def set_name(self, name: str) -> "BotInfo.Builder":
            """
            Sets the bot name. (required)

            Note:
                The maximum length of the name is defined by `MAX_NAME_LENGTH` characters.

            Example:
                "Rampage"

            Args:
                name (str): The name of the bot.

            Returns:
                IBuilder: The IBuilder instance, allowing for method chaining.
            """
            self.name = name
            return self

        def set_version(self, version: str) -> "BotInfo.Builder":
            """
            Sets the bot version. (required)

            Note:
                The maximum length of the version is 20 characters.

            Example:
                "1.0"

            Args:
                version (str): The version of the bot.

            Returns:
                Self, for method chaining.
            """
            self.version = version
            return self

        def set_authors(self, authors: List[str]) -> "BotInfo.Builder":
            """
            Sets the names(s) of the author(s) of the bot. (required)

            Note:
                - The maximum length of an author name is 50 characters.
                - Maximum number of names is 5.
                - Providing `None` will remove all authors.

            Example:
                ["John Doe"]

            Args:
                authors (list): A list containing the names(s) of the author(s).

            Returns:
                Self, for method chaining.
            """
            self.authors = authors
            return self

        def add_author(self, author: str) -> "BotInfo.Builder":
            """
            Adds an author of the bot. (required)

            See also:
                set_authors()

            Args:
                author (str): The name of an author to add.

            Returns:
                Self, for method chaining.
            """
            self.authors.append(author)
            return self

        def set_description(self, description: str) -> "BotInfo.Builder":
            """
            Sets a short description of the bot. (optional)

            Note:
                - The maximum length of the description is 250 characters.
                - Line-breaks (line-feed or newline) are supported, but expect up to 3 lines to be displayed on UI.

            Example:
                "The rampage bot will try to ram bots that are very close.\n"
                "Sneaks around corners and shoots at bots that come too near."

            Args:
                description (str): A short description of the bot.

            Returns:
                Self, for method chaining.
            """
            self.description = description
            return self

        def set_homepage(self, homepage: str) -> "BotInfo.Builder":
            """
            Sets a link to the homepage for the bot. (optional)

            Note:
                The maximum length of a link is 150 characters.

            Example:
                "https://fictive-homepage.net/Rampage"

            Args:
                homepage (str): A link to a homepage for the bot.

            Returns:
                Self, for method chaining.
            """
            self.homepage = homepage
            return self

        def set_country_codes(self, country_codes: List[str]) -> "BotInfo.Builder":
            """
            Sets the country codes for the bot. (optional)

            Notes:
                - Each country code uses a 2-character alpha-2 format from the ISO 3166 standard.
                - Maximum number of country codes is 5.
                - Providing `None` removes all country codes.
                - If no valid country code is specified, the default locale country code is used.

            Example:
                ["dk"]

            Args:
                country_codes (list): A list of country codes.

            Returns:
                Self, for method chaining.
            """
            self.country_codes = country_codes
            return self

        def add_country_code(self, country_code: str) -> "BotInfo.Builder":
            """
            Adds a country code for the bot. (optional)

            See also:
                set_country_codes()

            Args:
                country_code (str): The country code to add.

            Returns:
                Self, for method chaining.
            """
            self.country_codes.append(country_code)
            return self

        def set_game_types(self, game_types: Set[str]) -> "BotInfo.Builder":
            """
            Sets the game types that this bot is capable of participating in. (required)

            Notes:
                - Standard game types can be found at the referenced documentation or API spec.
                - More types may be added in the future.
                - Use the predefined strings from the GameType class when possible.
                - Maximum game type length is 20 characters.
                - Maximum number of game types is 10.
                - Providing `None` removes all game types.

            Example:
                {"classic", "melee", "future-type"}

            Args:
                game_types (set): A set of game types that the bot is capable of participating in.

            Returns:
                Self, for method chaining.
            """
            self.game_types = game_types
            return self

        def add_game_type(self, game_type: str) -> "BotInfo.Builder":
            """
            Adds a game type that this bot is capable of participating in. (required)

            See also:
                set_game_types()

            Example:
                "classic"

            Args:
                game_type (str): A game type that the bot can participate in.

            Returns:
                Self, for method chaining.
            """
            self.game_types.add(game_type)
            return self

        def set_platform(self, platform: str) -> "BotInfo.Builder":
            """
            Sets the name of the platform that this bot is built for. (optional)

            Note:
                - The maximum length of the platform name is 30 characters.
                - If `None` or a blank string is provided, the default string (Java Runtime Environment) is used.

            Example:
                "Java Runtime Environment (JRE) [version]"

            Args:
                platform (str): The name of the platform for this bot.

            Returns:
                Self, for method chaining.
            """
            self.platform = platform
            return self

        def set_programming_lang(self, language: str) -> "BotInfo.Builder":
            """
            Sets the name of the programming language used for developing this bot. (optional)

            Note:
                The maximum length of the programming language's name is 30 characters.

            Example:
                Python 3.12

            Args:
                language (str): The name of the programming language.

            Returns:
                Self, for method chaining.
            """
            self.programming_lang = language
            return self

        def set_initial_position(self, initial_position: str) -> "BotInfo.Builder":
            """
            Sets the initial position of the bot. (optional)

            Note:
                Initial positions must be enabled or allowed by the game (server) to take effect.

            Args:
                initial_position: The initial position of the bot.

            Returns:
                Self, for method chaining.
            """
            self.initial_position = initial_position
            return self

        def build(self) -> "BotInfo":
            """
            Builds and returns the BotInfo instance based on the data set added to this builder so far.

            This method is typically the final step in the builder's workflow to extract the result
            of the building process.

            Returns:
                BotInfo: The resulting BotInfo instance.
            """
            assert self.name is not None, "Name cannot be null"
            assert self.version is not None, "Version cannot be null"
            assert self.initial_position is not None, "Initial position cannot be null"
            return BotInfo(
                name=self.name,
                version=self.version,
                authors=self.authors,
                description=self.description,
                homepage=self.homepage,
                country_codes=self.country_codes,
                game_types=self.game_types,
                platform=self.platform,
                programming_lang=self.programming_lang,
                initial_position=InitialPosition.from_string(self.initial_position),
            )
