from robocode_tank_royale.bot_api.events import EventABC


class ConnectionEvent(EventABC):
    """Represents the base class for connection-related events."""

    def __init__(self, server_uri: str):
        """
        Initialize a new instance of the ConnectionEvent class.

        Args:
            server_uri (str): The URI of the server associated with the connection event.
        """
        self.server_uri = server_uri

    def get_server_uri(self) -> str:
        """
        Get the URI of the server.

        Returns:
            str: The URI of the server associated with the connection event.
        """
        return self.server_uri