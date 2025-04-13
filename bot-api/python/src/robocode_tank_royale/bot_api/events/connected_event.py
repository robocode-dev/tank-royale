from dataclasses import dataclass

from robocode_tank_royale.bot_api.events import ConnectionEvent


@dataclass(frozen=True, repr=True)
class ConnectedEvent(ConnectionEvent):
    """
    Event occurring when bot gets connected to server
    """
    pass
