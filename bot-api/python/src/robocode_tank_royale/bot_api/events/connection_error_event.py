from dataclasses import dataclass

from .connection_event import ConnectionEvent


@dataclass(frozen=True, repr=True)
class ConnectionErrorEvent(ConnectionEvent):
    """
    Represents an event that occurs when a connection error happens.

    Attributes:
        error (Exception): The exception representing the connection error.
    """

    error: Exception | None = None
