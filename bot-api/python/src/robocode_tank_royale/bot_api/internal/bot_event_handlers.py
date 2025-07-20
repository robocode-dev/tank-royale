"""
Class used for bot event handlers on the public API.

These handlers may or may not be triggered by the bot event queue, and might
not be handled immediately by the bot logic.
"""

from typing import Any, Dict, Type

from ..base_bot_abc import BaseBotABC
from ..events import (
    ConnectedEvent, DisconnectedEvent, ConnectionErrorEvent,
    GameStartedEvent, GameEndedEvent, RoundStartedEvent, RoundEndedEvent,
    TickEvent, SkippedTurnEvent, DeathEvent, BotDeathEvent,
    HitBotEvent, HitWallEvent, BulletFiredEvent, HitByBulletEvent,
    BulletHitBotEvent, BulletHitBulletEvent, BulletHitWallEvent,
    ScannedBotEvent, WonRoundEvent, CustomEvent, TeamMessageEvent
)
from ..events.bot_event import BotEvent
from ..events.event_abc import EventABC

from .event_handler import EventHandler


class BotEventHandlers:
    """
    Class used for bot event handlers on the public API.
    
    These handlers may or may not be triggered by the bot event queue, and might
    not be handled immediately by the bot logic.
    """

    def __init__(self, base_bot: BaseBotABC):
        """
        Initialize a new BotEventHandlers instance.
        
        Args:
            base_bot: The base bot that will handle the events.
        """
        self.on_connected = EventHandler[ConnectedEvent]()
        self.on_disconnected = EventHandler[DisconnectedEvent]()
        self.on_connection_error = EventHandler[ConnectionErrorEvent]()

        self.on_game_started = EventHandler[GameStartedEvent]()
        self.on_game_ended = EventHandler[GameEndedEvent]()
        self.on_game_aborted = EventHandler[Any]()
        self.on_round_started = EventHandler[RoundStartedEvent]()
        self.on_round_ended = EventHandler[RoundEndedEvent]()

        self.on_tick = EventHandler[TickEvent]()
        self.on_skipped_turn = EventHandler[SkippedTurnEvent]()
        self.on_death = EventHandler[DeathEvent]()
        self.on_bot_death = EventHandler[BotDeathEvent]()
        self.on_hit_bot = EventHandler[HitBotEvent]()
        self.on_hit_wall = EventHandler[HitWallEvent]()
        self.on_bullet_fired = EventHandler[BulletFiredEvent]()
        self.on_hit_by_bullet = EventHandler[HitByBulletEvent]()
        self.on_bullet_hit = EventHandler[BulletHitBotEvent]()
        self.on_bullet_hit_bullet = EventHandler[BulletHitBulletEvent]()
        self.on_bullet_hit_wall = EventHandler[BulletHitWallEvent]()
        self.on_scanned_bot = EventHandler[ScannedBotEvent]()
        self.on_won_round = EventHandler[WonRoundEvent]()
        self.on_custom_event = EventHandler[CustomEvent]()
        self.on_team_message = EventHandler[TeamMessageEvent]()

        self._event_handler_map: Dict[Type[EventABC], EventHandler[Any]] = {}

        self._initialize_event_handlers()
        self._subscribe_to_event_handlers(base_bot)

    def _initialize_event_handlers(self) -> None:
        """Initialize the mapping between event types and their handlers."""
        self._event_handler_map[ConnectedEvent] = self.on_connected
        self._event_handler_map[DisconnectedEvent] = self.on_disconnected
        self._event_handler_map[ConnectionErrorEvent] = self.on_connection_error

        self._event_handler_map[GameStartedEvent] = self.on_game_started
        self._event_handler_map[GameEndedEvent] = self.on_game_ended
        self._event_handler_map[self.GameAbortedEvent] = self.on_game_aborted
        self._event_handler_map[RoundStartedEvent] = self.on_round_started
        self._event_handler_map[RoundEndedEvent] = self.on_round_ended

        self._event_handler_map[TickEvent] = self.on_tick
        self._event_handler_map[SkippedTurnEvent] = self.on_skipped_turn
        self._event_handler_map[DeathEvent] = self.on_death
        self._event_handler_map[BotDeathEvent] = self.on_bot_death
        self._event_handler_map[HitBotEvent] = self.on_hit_bot
        self._event_handler_map[HitWallEvent] = self.on_hit_wall
        self._event_handler_map[BulletFiredEvent] = self.on_bullet_fired
        self._event_handler_map[HitByBulletEvent] = self.on_hit_by_bullet
        self._event_handler_map[BulletHitBotEvent] = self.on_bullet_hit
        self._event_handler_map[BulletHitBulletEvent] = self.on_bullet_hit_bullet
        self._event_handler_map[BulletHitWallEvent] = self.on_bullet_hit_wall
        self._event_handler_map[ScannedBotEvent] = self.on_scanned_bot
        self._event_handler_map[WonRoundEvent] = self.on_won_round
        self._event_handler_map[CustomEvent] = self.on_custom_event
        self._event_handler_map[TeamMessageEvent] = self.on_team_message

    def _subscribe_to_event_handlers(self, base_bot: BaseBotABC) -> None:
        """
        Subscribe the base bot to all event handlers.
        
        Args:
            base_bot: The base bot that will handle the events.
        """
        self.on_connected.subscribe(base_bot.on_connected)
        self.on_disconnected.subscribe(base_bot.on_disconnected)
        self.on_connection_error.subscribe(base_bot.on_connection_error)

        self.on_game_started.subscribe(base_bot.on_game_started)
        self.on_game_ended.subscribe(base_bot.on_game_ended)
        self.on_round_started.subscribe(base_bot.on_round_started)
        self.on_round_ended.subscribe(base_bot.on_round_ended)

        self.on_tick.subscribe(base_bot.on_tick)
        self.on_skipped_turn.subscribe(base_bot.on_skipped_turn)
        self.on_death.subscribe(base_bot.on_death)
        self.on_bot_death.subscribe(base_bot.on_bot_death)
        self.on_hit_bot.subscribe(base_bot.on_hit_bot)
        self.on_hit_wall.subscribe(base_bot.on_hit_wall)
        self.on_bullet_fired.subscribe(base_bot.on_bullet_fired)
        self.on_hit_by_bullet.subscribe(base_bot.on_hit_by_bullet)
        self.on_bullet_hit.subscribe(base_bot.on_bullet_hit)
        self.on_bullet_hit_bullet.subscribe(base_bot.on_bullet_hit_bullet)
        self.on_bullet_hit_wall.subscribe(base_bot.on_bullet_hit_wall)
        self.on_scanned_bot.subscribe(base_bot.on_scanned_bot)
        self.on_won_round.subscribe(base_bot.on_won_round)
        self.on_custom_event.subscribe(base_bot.on_custom_event)
        self.on_team_message.subscribe(base_bot.on_team_message)

    async def fire_event(self, event: BotEvent) -> None:
        """
        Fire an event to its registered handler.
        
        Args:
            event: The event to fire.
            
        Raises:
            IllegalStateException: If there is no handler for the event type.
        """
        event_type = type(event)
        handler = self._event_handler_map.get(event_type)

        if handler is not None:
            await handler.publish(event)
        else:
            raise RuntimeError(f"Unhandled event type: {event}")

    # Virtual (fake) event:
    class GameAbortedEvent(EventABC):
        """Virtual event class representing a game aborted event."""
        pass
