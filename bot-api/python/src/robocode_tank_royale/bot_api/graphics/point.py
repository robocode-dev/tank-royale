from __future__ import annotations
from dataclasses import dataclass


@dataclass(frozen=True)
class Point:
    """Represents an ordered pair of x and y coordinates that define a point in a two-dimensional plane."""

    x: float
    y: float

    def __str__(self) -> str:
        """Returns a string that represents the current Point.

        Returns:
            A string that represents the current Point.
        """
        return f"(x={self.x}, y={self.y})"
