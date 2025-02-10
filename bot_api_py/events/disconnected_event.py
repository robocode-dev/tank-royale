from typing import Optional

from bot_api_py.events.connection_event import ConnectionEvent

class DisconnectedEvent(ConnectionEvent):
    """Event occurring when bot gets disconnected from server."""

    def __init__(self, server_uri: str, remote: bool, status_code: Optional[int], reason: Optional[str]):
        """Initializes a new instance of the DisconnectedEvent class.

        Args:
            server_uri: The URI of the server.
            remote: A flag indicating if closing of the connection was initiated by the remote host.
            status_code: A status code that indicates the reason for closing the connection.
            reason: A message with the reason for closing the connection.
        """
        super().__init__(server_uri)
        self.remote = remote
        self.status_code = status_code
        self.reason = reason

    def is_remote(self) -> bool:
        """Checks if closing the connection was initiated by the remote host.

        Returns:
            True if closing the connection was initiated by the remote host; False otherwise.
        """
        return self.remote

    def get_status_code(self) -> Optional[int]:
        """Returns a status code that indicates the reason for closing the connection, if such status code exists.

        Returns:
            A status code that indicates the reason for closing the connection, if such status code exists.
        """
        return self.status_code  # No need for Optional.ofNullable; Python handles None directly

    def get_reason(self) -> Optional[str]:
        """Returns a message with the reason for closing the connection, if such reason exists.

        Returns:
            A message with the reason for closing the connection, if such reason exists.
        """
        return self.reason  # No need for Optional.ofNullable; Python handles None directly
