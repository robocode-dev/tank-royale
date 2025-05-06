from abc import ABC


class MathUtil(ABC):
    """
    Math utility class.
    """

    @staticmethod
    def clamp(value: float, min_value: float, max_value: float) -> float:
        """
        Clamps a value to the inclusive range of min and max.

        Args:
            value: The value to be clamped.
            min_value: The lower bound of the result.
            max_value: The upper bound of the result.

        Returns:
            The clamped value.
        """
        return min(max_value, max(min_value, value))
