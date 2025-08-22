import re
from abc import ABC

from robocode_tank_royale.bot_api.graphics.color import Color


class ColorUtil(ABC):
    _NUMERIC_RGB = re.compile(r"^#[0-9a-fA-F]{3,6}$")
    _HEX_DIGITS = re.compile(r"^[0-9a-fA-F]{3}$|^[0-9a-fA-F]{6}$")

    @staticmethod
    def to_hex(color: Color | None) -> str | None:
        """
        Converts the specified `Color` object to a hex triplet string representation.

        The hex triplet consists of six hexadecimal digits representing an RGB color, e.g., "0099CC".

        Args:
            color (Color): The `Color` object to convert to a hex triplet.

        Returns:
            str: A string representing the color as a hex triplet of six hexadecimal digits, or None if the color
            is None.
        """
        if color is None:
            return None
        return (ColorUtil._to_hex(color.red) +
                ColorUtil._to_hex(color.green) +
                ColorUtil._to_hex(color.blue))

    @staticmethod
    def _to_hex(value: int) -> str:
        """
        Converts an integer value to a two-character hexadecimal string.

        Args:
            value (int): The integer value representing a color component.

        Returns:
            str: The two-character hexadecimal string.
        """
        return f"{value >> 4:x}{value & 0xF:x}"

    @staticmethod
    def from_string(string: str | None) -> Color | None:
        """
        Creates a color from a string. Currently, only numeric RGB values are supported.

        This method works the same as `from_hex` except that it requires a hash sign before the hex value.
        An example of a numeric RGB value is "#09C" or "#0099CC", which both represent the same color.

        Args:
            string (str): A string containing either three or six hexadecimal RGB values like "#09C" or "#0099CC".

        Returns:
            Color: The created `Color` object, or None if the input parameter is None.

        Raises:
            ValueError: If the input string is not in the valid numeric RGB format.
        """
        if string is None:
            return None
        string = string.strip()
        if ColorUtil._NUMERIC_RGB.match(string):
            return ColorUtil.from_hex(string[1:])
        raise ValueError("You must supply the string in numeric RGB format #[0-9a-fA-F], e.g. \"#09C\" or \"#0099CC\"")

    @staticmethod
    def from_hex(hex_triplet: str) -> Color:
        """
        Creates a color from a hex triplet.

        A hex triplet is either three or six hexadecimal digits that represent an RGB color.
        An example of a hex triplet is "09C" or "0099CC", which both represent the same color.

        Args:
            hex_triplet (str): A string containing either three or six hexadecimal numbers like "09C" or "0099CC".

        Returns:
            Color: The created `Color` object.

        Raises:
            ValueError: If the input string is not a valid hex triplet.
        """
        hex_triplet = hex_triplet.strip()
        if not ColorUtil._HEX_DIGITS.match(hex_triplet):
            raise ValueError("You must supply 3 or 6 hex digits [0-9a-fA-F], e.g. \"09C\" or \"0099CC\"")

        is_three_digits = len(hex_triplet) == 3
        component_length = 1 if is_three_digits else 2

        r = int(hex_triplet[0:component_length], 16)
        g = int(hex_triplet[component_length:component_length * 2], 16)
        b = int(hex_triplet[component_length * 2:component_length * 3], 16)

        if is_three_digits:
            r = (r << 4) | r
            g = (g << 4) | g
            b = (b << 4) | b

        return Color.from_rgb(r, g, b)
