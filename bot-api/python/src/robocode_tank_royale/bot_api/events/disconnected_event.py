from typing import Optional
from robocode_tank_royale.bot_api.events import ConnectionEvent


class DisconnectedEvent(ConnectionEvent):
    """
    Represents an event triggered when the bot gets disconnected from the server.
    """

    def __init__(self, server_uri: str, remote: bool, status_code: Optional[int], reason: Optional[str]):
        """
        Initializes a new instance of the DisconnectedEvent class.

        Args:
            server_uri (str): The URI of the server the bot was connected to.
            remote (bool): Indicates whether the disconnection was initiated by the remote host.
            status_code (Optional[int]): The status code indicating the reason for the disconnection, if available.
            reason (Optional[str]): A textual description of the reason for the disconnection, if provided.
        """
        super().__init__(server_uri)
        self.remote = remote
        self.status_code = status_code
        self.reason = reason

    def is_remote(self) -> bool:
        """
        Determines whether the connection was closed by the remote host.

        Returns:
            bool: True if the disconnection was initiated by the remote host; False otherwise.
        """
        return self.remote

    def get_status_code(self) -> Optional[int]:
        """
        Retrieves the status code associated with the disconnection.

        Returns:
            Optional[int]: The status code if available; otherwise, None.
        """
        return self.status_code

    def get_reason(self) -> Optional[str]:
        """
        Retrieves the reason for the disconnection, if provided.

        Returns:
            Optional[str]: A string describing the reason for the disconnection, or None if not available.
        """
        return self.reason