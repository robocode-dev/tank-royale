from robocode_tank_royale.bot_api.events import ConnectionEvent


class ConnectionErrorEvent(ConnectionEvent):
    """Event occurring when a connection error occurs."""

    def __init__(self, server_uri: str, error: Exception):
        """Initializes a new instance of the ConnectionErrorEvent class.

        Args:
            server_uri: The URI of the server.
            error: The error.
        """
        super().__init__(server_uri)
        self.error = error

    def get_error(self) -> Exception:
        """Returns the error.

        Returns:
            The error.
        """
        return self.error
