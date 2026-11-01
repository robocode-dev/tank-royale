from abc import ABC

from robocode_tank_royale.bot_api.graphics.color import Color


class ColorUtil(ABC):
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
        Creates a color from a hex color, which is a hex string with a leading hash sign.

        The same four forms are accepted as by `from_hex`: "#RGB", "#RGBA", "#RRGGBB" and
        "#RRGGBBAA", in capital or small letters. An example is "#09C" or "#0099CC", which
        both represent the same, fully opaque color.

        Args:
            string (str): A string like "#09C", "#09C8", "#0099CC" or "#0099CC88". Surrounding spaces are ignored.

        Returns:
            Color: The created `Color` object, or None if the input parameter is None.

        Raises:
            ValueError: If the input string is not one of the four forms.
        """
        return Color.from_hex_color(string)

    @staticmethod
    def from_hex(hex_triplet: str) -> Color:
        """
        Creates a color from a hex string without a leading hash sign.

        Four forms are accepted, in capital or small letters: three digits (RGB), four digits (RGBA),
        six digits (RRGGBB) and eight digits (RRGGBBAA). In the short forms each digit is doubled.
        Without alpha digits the color is fully opaque.

        Args:
            hex_triplet (str): A string containing 3, 4, 6 or 8 hexadecimal digits like "09C" or "0099CC88".

        Returns:
            Color: The created `Color` object.

        Raises:
            ValueError: If the input string is not one of the four forms.
        """
        return Color.from_hex(hex_triplet)
