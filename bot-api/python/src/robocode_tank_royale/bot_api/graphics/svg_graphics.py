from __future__ import annotations
from typing import List

from .color import Color
from .point import Point
from .graphics_abc import GraphicsABC


class SvgGraphicsABC(GraphicsABC):
    """Implementation of a graphics context that generates SVG markup.

    This mirrors the Java SvgGraphics implementation and is intended to produce
    equivalent SVG output for the same drawing operations.
    """

    def __init__(self) -> None:
        """Initializes a new SVG graphics context with default state."""
        self._elements: List[str] = []
        self._stroke_color: str = "none"
        self._fill_color: str = "none"
        self._stroke_width: float = 0.0
        self._font_family: str = "Arial"
        self._font_size: float = 12.0

    # Drawing primitives
    def draw_line(self, x1: float, y1: float, x2: float, y2: float) -> None:
        """Draws a line from point (x1, y1) to point (x2, y2).

        Args:
            x1: The x coordinate of the first point.
            y1: The y coordinate of the first point.
            x2: The x coordinate of the second point.
            y2: The y coordinate of the second point.
        """
        self._elements.append(
            f"<line x1=\"{_fmt(x1)}\" y1=\"{_fmt(y1)}\" x2=\"{_fmt(x2)}\" y2=\"{_fmt(y2)}\" "
            f"stroke=\"{self._stroke_color}\" stroke-width=\"{_fmt(self._stroke_width)}\" />\n"
        )

    def draw_rectangle(self, x: float, y: float, width: float, height: float) -> None:
        """Draws the outline of a rectangle.

        Args:
            x: The x coordinate of the upper-left corner of the rectangle.
            y: The y coordinate of the upper-left corner of the rectangle.
            width: The width of the rectangle.
            height: The height of the rectangle.
        """
        stroke_color = "#000000" if self._stroke_color == "none" else self._stroke_color
        stroke_width = 1.0 if self._stroke_width == 0 else self._stroke_width
        self._elements.append(
            f"<rect x=\"{_fmt(x)}\" y=\"{_fmt(y)}\" width=\"{_fmt(width)}\" height=\"{_fmt(height)}\" "
            f"fill=\"none\" stroke=\"{stroke_color}\" stroke-width=\"{_fmt(stroke_width)}\" />\n"
        )

    def fill_rectangle(self, x: float, y: float, width: float, height: float) -> None:
        """Fills a rectangle with the current fill color.

        Args:
            x: The x coordinate of the upper-left corner of the rectangle.
            y: The y coordinate of the upper-left corner of the rectangle.
            width: The width of the rectangle.
            height: The height of the rectangle.
        """
        self._elements.append(
            f"<rect x=\"{_fmt(x)}\" y=\"{_fmt(y)}\" width=\"{_fmt(width)}\" height=\"{_fmt(height)}\" "
            f"fill=\"{self._fill_color}\" stroke=\"{self._stroke_color}\" stroke-width=\"{_fmt(self._stroke_width)}\" />\n"
        )

    def draw_circle(self, x: float, y: float, radius: float) -> None:
        """Draws the outline of a circle.

        Args:
            x: The x coordinate of the center of the circle.
            y: The y coordinate of the center of the circle.
            radius: The radius of the circle.
        """
        stroke_color = "#000000" if self._stroke_color == "none" else self._stroke_color
        stroke_width = 1.0 if self._stroke_width == 0 else self._stroke_width
        self._elements.append(
            f"<circle cx=\"{_fmt(x)}\" cy=\"{_fmt(y)}\" r=\"{_fmt(radius)}\" fill=\"none\" "
            f"stroke=\"{stroke_color}\" stroke-width=\"{_fmt(stroke_width)}\" />\n"
        )

    def fill_circle(self, x: float, y: float, radius: float) -> None:
        """Fills a circle with the current fill color.

        Args:
            x: The x coordinate of the center of the circle.
            y: The y coordinate of the center of the circle.
            radius: The radius of the circle.
        """
        self._elements.append(
            f"<circle cx=\"{_fmt(x)}\" cy=\"{_fmt(y)}\" r=\"{_fmt(radius)}\" fill=\"{self._fill_color}\" "
            f"stroke=\"{self._stroke_color}\" stroke-width=\"{_fmt(self._stroke_width)}\" />\n"
        )

    def draw_polygon(self, points: List[Point]) -> None:
        """Draws the outline of a polygon defined by a list of points.

        Args:
            points: List of points defining the polygon.
        """
        if points is None or len(points) < 3:
            return
        pts = " ".join(f"{_fmt(p.x)},{_fmt(p.y)}" for p in points).strip()
        stroke_color = "#000000" if self._stroke_color == "none" else self._stroke_color
        stroke_width = 1.0 if self._stroke_width == 0 else self._stroke_width
        self._elements.append(
            f"<polygon points=\"{pts}\" fill=\"none\" stroke=\"{stroke_color}\" "
            f"stroke-width=\"{_fmt(stroke_width)}\" />\n"
        )

    def fill_polygon(self, points: List[Point]) -> None:
        """Fills a polygon defined by a list of points with the current fill color.

        Args:
            points: List of points defining the polygon.
        """
        if points is None or len(points) < 3:
            return
        pts = " ".join(f"{_fmt(p.x)},{_fmt(p.y)}" for p in points).strip()
        self._elements.append(
            f"<polygon points=\"{pts}\" fill=\"{self._fill_color}\" stroke=\"{self._stroke_color}\" "
            f"stroke-width=\"{_fmt(self._stroke_width)}\" />\n"
        )

    def draw_text(self, text: str, x: float, y: float) -> None:
        """Draws text at the specified position.

        Args:
            text: The text to draw.
            x: The x coordinate where to draw the text.
            y: The y coordinate where to draw the text.
        """
        escaped = _escape_xml_text(text)
        self._elements.append(
            f"<text x=\"{_fmt(x)}\" y=\"{_fmt(y)}\" font-family=\"{self._font_family}\" "
            f"font-size=\"{_fmt(self._font_size)}\" fill=\"{self._stroke_color}\">{escaped}</text>\n"
        )

    # State setters
    def set_stroke_color(self, color: Color) -> None:
        """Sets the color used for drawing outlines.

        Args:
            color: The color to use for drawing outlines.
        """
        self._stroke_color = _to_hex(color)

    def set_fill_color(self, color: Color) -> None:
        """Sets the color used for filling shapes.

        Args:
            color: The color to use for filling shapes.
        """
        self._fill_color = _to_hex(color)

    def set_stroke_width(self, width: float) -> None:
        """Sets the width of the stroke used for drawing outlines.

        Args:
            width: The width of the stroke.
        """
        self._stroke_width = width

    def set_font(self, font_family: str, font_size: float) -> None:
        """Sets the font used for drawing text.

        Args:
            font_family: The font family name.
            font_size: The font size.
        """
        self._font_family = font_family
        self._font_size = font_size

    # Output & maintenance
    def to_svg(self) -> str:
        """Generates the SVG representation of all drawing operations.

        Returns:
            A string containing the SVG representation.
        """
        svg = ["<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 5000 5000\">\n"]
        svg.extend(self._elements)
        svg.append("</svg>\n")
        return "".join(svg)

    def clear(self) -> None:
        """Clears all drawing operations."""
        self._elements.clear()


def _fmt(value: float) -> str:
    # Format with at most 3 decimals, US-style dot decimal separator
    # and without trailing zeros beyond decimal point.
    s = f"{value:.3f}"
    # strip trailing zeros and possibly trailing dot
    if "." in s:
        s = s.rstrip("0").rstrip(".")
    return s


def _escape_xml_text(s: str) -> str:
    if s is None:
        return None
    # Ampersand first to avoid double-escaping
    return (
        s.replace("&", "&amp;")
         .replace("<", "&lt;")
         .replace(">", "&gt;")
         .replace('"', "&quot;")
    )


def _to_hex(color: Color) -> str:
    # Uppercase hex like Java tests expect
    if color.alpha == 255:
        return f"#{color.red:02X}{color.green:02X}{color.blue:02X}"
    return f"#{color.red:02X}{color.green:02X}{color.blue:02X}{color.alpha:02X}"
