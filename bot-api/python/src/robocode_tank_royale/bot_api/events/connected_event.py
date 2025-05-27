from dataclasses import dataclass

from .connection_event import ConnectionEvent


@dataclass(frozen=True, repr=True)
class ConnectedEvent(ConnectionEvent):
    """
    Event occurring when bot gets connected to server
    """
    pass
