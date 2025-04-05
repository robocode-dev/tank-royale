from dataclasses import dataclass


@dataclass(frozen=True)
class Color:
    """Represents a color with red, green, and blue components."""
    red: int
    green: int
    blue: int
    alpha: int = 255  # Defaults to fully opaque

    def __post_init__(self):
        """Validates the color component values.

        Ensures that all color components fall in the range [0, 255].
        """
        for component, name in [(self.red, "red"), (self.green, "green"), (self.blue, "blue"), (self.alpha, "alpha")]:
            if component is not None:  # Alpha can be None
                if not 0 <= component <= 255:
                    raise ValueError(f"{name} component must be between 0 and 255, got {component}")

    @classmethod
    def from_rgb(cls, red, green, blue):
        """Creates a Color instance from RGB values.

        Args:
            red: The red component of the color.
            green: The green component of the color.
            blue: The blue component of the color.

        Returns:
            A Color instance with the specified RGB values.
        """
        return cls(red, green, blue)

    @classmethod
    def from_rgba(cls, red, green, blue, alpha):
        """Creates a Color instance from RGBA values.

        Args:
            red: The red component of the color.
            green: The green component of the color.
            blue: The blue component of the color.
            alpha: The alpha (transparency) component of the color.

        Returns:
            A Color instance with the specified RGBA values.
        """
        return cls(red, green, blue, alpha)

    def to_tuple(self):
        """Returns the color as a tuple.

        Returns:
            A tuple of the form (red, green, blue, alpha).
        """
        return self.red, self.green, self.blue, self.alpha

    def to_rgb_tuple(self):
        """Returns the color as an RGB tuple.

        Returns:
            A tuple of the form (red, green, blue).
        """
        return self.red, self.green, self.blue
