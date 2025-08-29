from __future__ import annotations
from abc import ABC, abstractmethod
from typing import List

from .color import Color
from .point import Point


class GraphicsABC(ABC):
    """
    Interface for a graphics context that provides methods for drawing graphics primitives.
    """

    @abstractmethod
    def draw_line(self, x1: float, y1: float, x2: float, y2: float) -> None:
        """Draws a line from point (x1, y1) to point (x2, y2).

        Args:
            x1: The x coordinate of the first point.
            y1: The y coordinate of the first point.
            x2: The x coordinate of the second point.
            y2: The y coordinate of the second point.
        """
        pass

    @abstractmethod
    def draw_rectangle(self, x: float, y: float, width: float, height: float) -> None:
        """Draws the outline of a rectangle.

        Args:
            x: The x coordinate of the upper-left corner of the rectangle.
            y: The y coordinate of the upper-left corner of the rectangle.
            width: The width of the rectangle.
            height: The height of the rectangle.
        """
        pass

    @abstractmethod
    def fill_rectangle(self, x: float, y: float, width: float, height: float) -> None:
        """Fills a rectangle with the current fill color.

        Args:
            x: The x coordinate of the upper-left corner of the rectangle.
            y: The y coordinate of the upper-left corner of the rectangle.
            width: The width of the rectangle.
            height: The height of the rectangle.
        """
        pass

    @abstractmethod
    def draw_circle(self, x: float, y: float, radius: float) -> None:
        """Draws the outline of a circle.

        Args:
            x: The x coordinate of the center of the circle.
            y: The y coordinate of the center of the circle.
            radius: The radius of the circle.
        """
        pass

    @abstractmethod
    def fill_circle(self, x: float, y: float, radius: float) -> None:
        """Fills a circle with the current fill color.

        Args:
            x: The x coordinate of the center of the circle.
            y: The y coordinate of the center of the circle.
            radius: The radius of the circle.
        """
        pass

    @abstractmethod
    def draw_polygon(self, points: List[Point]) -> None:
        """Draws the outline of a polygon defined by a list of points.

        Args:
            points: List of points defining the polygon.
        """
        pass

    @abstractmethod
    def fill_polygon(self, points: List[Point]) -> None:
        """Fills a polygon defined by a list of points with the current fill color.

        Args:
            points: List of points defining the polygon.
        """
        pass

    @abstractmethod
    def draw_text(self, text: str, x: float, y: float) -> None:
        """Draws text at the specified position.

        Args:
            text: The text to draw.
            x: The x coordinate where to draw the text.
            y: The y coordinate where to draw the text.
        """
        pass

    @abstractmethod
    def set_stroke_color(self, color: Color) -> None:
        """Sets the color used for drawing outlines.

        Args:
            color: The color to use for drawing outlines.
        """
        pass

    @abstractmethod
    def set_fill_color(self, color: Color) -> None:
        """Sets the color used for filling shapes.

        Args:
            color: The color to use for filling shapes.
        """
        pass

    @abstractmethod
    def set_stroke_width(self, width: float) -> None:
        """Sets the width of the stroke used for drawing outlines.

        Args:
            width: The width of the stroke.
        """
        pass

    @abstractmethod
    def set_font(self, font_family: str, font_size: float) -> None:
        """Sets the font used for drawing text.

        Args:
            font_family: The font family name.
            font_size: The font size.
        """
        pass

    @abstractmethod
    def to_svg(self) -> str:
        """Generates the SVG representation of all drawing operations.

        Returns:
            A string containing the SVG representation.
        """
        pass

    @abstractmethod
    def clear(self) -> None:
        """Clears all drawing operations."""
        pass
