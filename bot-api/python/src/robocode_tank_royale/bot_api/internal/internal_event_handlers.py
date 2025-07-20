"""Internal event handler support."""

from typing import Any, Dict, Type

from ..events import EventABC
from ..events import (
    DisconnectedEvent,
    GameEndedEvent,
    RoundStartedEvent,
    RoundEndedEvent,
    DeathEvent,
    HitBotEvent,
    HitWallEvent,
    BulletFiredEvent,
)
from .event_handler import EventHandler


class InternalEventHandlers:
    """
    Class used for instant event handling only used for updating the state of the API internals.
    """

    def __init__(self):
        """Initialize a new InternalEventHandlers instance."""
        self.on_disconnected: EventHandler[DisconnectedEvent] = EventHandler()

        self.on_game_ended: EventHandler[GameEndedEvent] = EventHandler()
        self.on_game_aborted: EventHandler[Any] = EventHandler()
        self.on_round_started: EventHandler[RoundStartedEvent] = EventHandler()
        self.on_round_ended: EventHandler[RoundEndedEvent] = EventHandler()

        self.on_death: EventHandler[DeathEvent] = EventHandler()
        self.on_hit_bot: EventHandler[HitBotEvent] = EventHandler()
        self.on_hit_wall: EventHandler[HitWallEvent] = EventHandler()
        self.on_bullet_fired: EventHandler[BulletFiredEvent] = EventHandler()

        # Virtual (fake) event handler
        self.on_next_turn: EventHandler[Any] = EventHandler()

        # Map event types to their handlers
        self._event_handler_map: Dict[Type[EventABC], EventHandler[Any]] = {}
        self._initialize_event_handlers()

    def _initialize_event_handlers(self) -> None:
        """Initialize the mapping between event types and their handlers."""

        self._event_handler_map[DisconnectedEvent] = self.on_disconnected

        self._event_handler_map[GameEndedEvent] = self.on_game_ended
        self._event_handler_map[self.GameAbortedEvent] = self.on_game_aborted
        self._event_handler_map[RoundStartedEvent] = self.on_round_started
        self._event_handler_map[RoundEndedEvent] = self.on_round_ended

        self._event_handler_map[DeathEvent] = self.on_death
        self._event_handler_map[HitBotEvent] = self.on_hit_bot
        self._event_handler_map[HitWallEvent] = self.on_hit_wall
        self._event_handler_map[BulletFiredEvent] = self.on_bullet_fired

        self._event_handler_map[self.NextTurnEvent] = self.on_next_turn

    async def fire_event(self, event: EventABC) -> None:
        """
        Fire an event to its registered handler.
        
        Args:
            event: The event to fire.
        
        Note:
            If there is no registered event handler for the event type,
            the event is ignored.
        """
        handler = self._event_handler_map.get(type(event))
        if handler is not None:
            await handler.publish(event)
        # ignore if there is no registered event handler

    # Virtual (fake) events:
    class GameAbortedEvent(EventABC):
        """Virtual event class representing a game aborted event."""
        pass

    class NextTurnEvent(EventABC):
        """Private virtual event class representing a next turn event."""
        pass
