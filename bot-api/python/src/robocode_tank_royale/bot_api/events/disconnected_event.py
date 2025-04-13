from dataclasses import dataclass
from typing import Optional

from .connection_event import ConnectionEvent


@dataclass(frozen=True, repr=True)
class DisconnectedEvent(ConnectionEvent):
    """
    Represents an event triggered when the bot gets disconnected from the server.

    Attributes:
        is_remote (bool): Indicates whether the disconnection was initiated by the remote host.
        status_code (Optional[int]): The status code indicating the reason for the disconnection, if available.
        reason (Optional[str]): A textual description of the reason for the disconnection, if provided.
    """

    is_remote: bool
    status_code: Optional[int] = None
    reason: Optional[str] = None
