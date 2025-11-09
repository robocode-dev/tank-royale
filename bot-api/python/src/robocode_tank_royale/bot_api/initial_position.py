from dataclasses import dataclass
from typing import Optional
import re


@dataclass
class InitialPosition:
    """
    Represents the initial position of a bot during debugging, with optional specific coordinates (x, y)
    and a shared direction for the body, gun, and radar. If not specified, the values are assigned randomly.

    Note:
        The initial position is only applied when debugging and if enabled on the server-side.
        Otherwise, it will be ignored.
    """

    x: Optional[float]
    """Optional x-coordinate for the starting position. If None, it will be randomly assigned."""

    y: Optional[float]
    """Optional y-coordinate for the starting position. If None, it will be randomly assigned."""

    direction: Optional[float]
    """Optional shared direction for the body, gun, and radar. If None, it will be randomly assigned."""

    def __str__(self) -> str:
        """
        Converts the `InitialPosition` object into a string format.

        Returns:
            A comma-separated string representation of the coordinates and direction.
            Empty values are represented as empty strings.
        """
        if self.x is None and self.y is None and self.direction is None:
            return ""
        str_x = "" if self.x is None else str(self.x)
        str_y = "" if self.y is None else str(self.y)
        str_direction = "" if self.direction is None else str(self.direction)
        return f"{str_x},{str_y},{str_direction}"

    @staticmethod
    def from_string(initial_position: str) -> Optional["InitialPosition"]:
        """
        Creates an `InitialPosition` instance from a string.

        Args:
            initial_position (str): A string containing coordinates and direction,
            separated by commas and/or whitespace.

        Returns:
            InitialPosition: An instance of the class if parsing is successful.
            None: If the input string is invalid or empty.
        """
        if initial_position is None:
            return None
        s = initial_position.strip()
        if s == "":
            return None
        # Split on commas (with optional surrounding whitespace) or any whitespace
        values = [v for v in re.split(r"\s*,\s*|\s+", s) if v != ""]
        return InitialPosition._parse_initial_position(values)

    @staticmethod
    def _parse_initial_position(values: list[str]) -> Optional["InitialPosition"]:
        """
        Parses a list of values to create an `InitialPosition` instance.

        Args:
            values (list[str]): A list containing string representations of coordinates and direction.

        Returns:
            InitialPosition: An instance parsed from the values.
            None: If parsing fails or the list is empty.
        """
        if len(values) < 1:
            return None

        x = InitialPosition._parse_double(values[0])
        if len(values) < 2:
            return InitialPosition(x, None, None)
        y = InitialPosition._parse_double(values[1])
        direction = None
        if len(values) >= 3:
            direction = InitialPosition._parse_double(values[2])
        return InitialPosition(x, y, direction)

    @staticmethod
    def _parse_double(s: str) -> Optional[float]:
        """
        Converts a string to a float, if possible.

        Args:
            s (str): The input string.

        Returns:
            float: Converted float value if successful.
            None: If the string is None or cannot be converted.
        """
        if s is None:
            return None
        try:
            return float(s.strip())
        except ValueError:
            return None