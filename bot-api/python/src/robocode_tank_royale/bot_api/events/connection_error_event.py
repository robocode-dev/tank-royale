from robocode_tank_royale.bot_api.events import ConnectionEvent


class ConnectionErrorEvent(ConnectionEvent):
    """
    Represents an event that occurs when a connection error happens.
    """

    def __init__(self, server_uri: str, error: Exception):
        """
        Initializes a new instance of the ConnectionErrorEvent class.

        Args:
            server_uri (str): The URI of the server where the error occurred.
            error (Exception): The exception representing the connection error.
        """
        super().__init__(server_uri)
        self.error = error

    def get_error(self) -> Exception:
        """
        Retrieves the connection error associated with this event.

        Returns:
            Exception: The connection error.
        """
        return self.error