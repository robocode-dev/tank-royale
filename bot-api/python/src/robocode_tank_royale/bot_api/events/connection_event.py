from robocode_tank_royale.bot_api.events import EventABC


class ConnectionEvent(EventABC):
    """Base class of all connection events."""

    def __init__(self, server_uri: str):
        """Initializes a new instance of the ConnectionEvent class.

        Args:
            server_uri: The URI of the server.
        """
        self.server_uri = server_uri

    def get_server_uri(self) -> str:
        """Return the URI of the server.

        Returns:
            The URI of the server.
        """
        return self.server_uri
