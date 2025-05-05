class InitialPositionMapper:
    """Utility class for mapping initial positions."""

    @staticmethod
    def map(source):
        """Map a bot-api initial position to a schema initial position."""

        # Create a schema InitialPosition object
        # The Python schema classes use snake_case for attributes
        initial_position = type('InitialPosition', (), {})()
        
        initial_position.x = source.x if hasattr(source, 'x') else None
        initial_position.y = source.y if hasattr(source, 'y') else None
        initial_position.direction = source.direction if hasattr(source, 'direction') else None
        
        return initial_position
