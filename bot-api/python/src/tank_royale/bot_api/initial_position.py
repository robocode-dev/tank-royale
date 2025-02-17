from dataclasses import dataclass
from typing import Optional

@dataclass
class InitialPosition:
    """Initial starting position containing a start coordinate (x,y) and the shared direction of the body, gun, and radar.

    The initial position is only used when debugging to request the server to let a bot start at a specific position.
    Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
    is ignored.
    """

    x: Optional[float]
    """The x coordinate, where None means it is random."""

    y: Optional[float]
    """The y coordinate, where None means it is random."""

    direction: Optional[float]
    """The shared direction of the body, gun, and radar, where None means it is random."""

    def __str__(self) -> str:
        """
        Returns:
            A string representation of the initial position.
        """
        if self.x is None and self.y is None and self.direction is None:
            return ""
        str_x = "" if self.x is None else str(self.x)
        str_y = "" if self.y is None else str(self.y)
        str_direction = "" if self.direction is None else str(self.direction)
        return f"{str_x},{str_y},{str_direction}"

    @staticmethod
    def from_string(initial_position: str) -> Optional["InitialPosition"]:
        """Creates new instance of the InitialPosition class from a string.

        Args:
            initial_position: Comma and/or white-space separated string.

        Returns:
            An InitialPosition instance or None if the input string is invalid.
        """
        if initial_position is None or initial_position.isspace():
            return None
        values = initial_position.strip().split(r"\s*,\s*|\s+")
        return InitialPosition._parse_initial_position(values)

    @staticmethod
    def _parse_initial_position(values: list[str]) -> Optional["InitialPosition"]:
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
        if s is None:
            return None
        try:
            return float(s.strip())
        except ValueError:
            return None