from dataclasses import dataclass

from .event_abc import EventABC


@dataclass(frozen=True, repr=True)
class ConnectionEvent(EventABC):
    """
    Represents the base class for connection-related events.

    Attributes:
        server_uri (str): The URI of the server associated with the connection event.
    """

    server_uri: str
