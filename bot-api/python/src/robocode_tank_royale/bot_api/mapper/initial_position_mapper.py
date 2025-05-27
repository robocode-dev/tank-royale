from ..initial_position import InitialPosition
from robocode_tank_royale.schema import InitialPosition as InitialPositionSchema


class InitialPositionMapper:
    """Utility class for mapping initial positions."""

    @staticmethod
    def map(source: InitialPosition | None) -> InitialPositionSchema | None:
        """Map a bot-api initial position to a schema initial position."""
        if source is None:
            return None

        # Create a schema InitialPosition object
        # The Python schema classes use snake_case for attributes
        initial_position :InitialPositionSchema = InitialPositionSchema()

        initial_position.x = source.x if hasattr(source, 'x') else None  # type: ignore
        initial_position.y = source.y if hasattr(source, 'y') else None  # type: ignore
        initial_position.direction = source.direction if hasattr(source, 'direction') else None  # type: ignore

        return initial_position
