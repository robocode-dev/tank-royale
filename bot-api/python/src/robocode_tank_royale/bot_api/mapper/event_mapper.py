import json
from typing import Any, Dict, Union

from ..base_bot_abc import BaseBotABC
from ..bot_exception import BotException
from ..events import (
    BotDeathEvent,
    BulletFiredEvent,
    BulletHitBotEvent,
    BulletHitBulletEvent,
    BulletHitWallEvent,
    DeathEvent,
    HitBotEvent,
    HitByBulletEvent,
    HitWallEvent,
    ScannedBotEvent,
    SkippedTurnEvent,
    TeamMessageEvent,
    TickEvent,
    WonRoundEvent, BotEvent
)
from robocode_tank_royale.schema import (
    BotDeathEvent as SchemaBotDeathEvent,
    BotHitBotEvent as SchemaBotHitBotEvent,
    BotHitWallEvent as SchemaBotHitWallEvent,
    BulletFiredEvent as SchemaBulletFiredEvent,
    BulletHitBotEvent as SchemaBulletHitBotEvent,
    BulletHitBulletEvent as SchemaBulletHitBulletEvent,
    BulletHitWallEvent as SchemaBulletHitWallEvent,
    Event as SchemaEvent,
    ScannedBotEvent as SchemaScannedBotEvent,
    SkippedTurnEvent as SchemaSkippedTurnEvent,
    TeamMessageEvent as SchemaTeamMessageEvent,
    TickEventForBot as SchemaTickEvent,
    WonRoundEvent as SchemaWonRoundEvent
)
from .bot_state_mapper import BotStateMapper
from .bullet_state_mapper import BulletStateMapper


class EventMapper:
    """Utility class for mapping events."""

    @staticmethod
    def map_tick_event(event: SchemaTickEvent, base_bot: BaseBotABC) -> TickEvent:
        """Map a schema TickEventForBot to a bot-api TickEvent."""
        mapped_events = None
        assert event.events is not None
        non_null_events: list[SchemaEvent] = []
        for e in event.events:
            assert e is not None
            non_null_events.append(e)
        mapped_events = EventMapper._map_events(non_null_events, base_bot)
        assert mapped_events is not None
        return TickEvent(
            event.turn_number,
            event.round_number,
            BotStateMapper.map(event.bot_state),
            BulletStateMapper.map(event.bullet_states),
            mapped_events,
        )

    @staticmethod
    def _map_events(events: list[SchemaEvent], base_bot: BaseBotABC) -> list[BotEvent] | None:
        """Map a collection of schema events to bot-api events."""
        game_bot_events: list[BotEvent] = list()
        for event in events:
            game_bot_events.append(EventMapper.map_event(event, base_bot))
        return game_bot_events

    @staticmethod
    def map_event(event: SchemaEvent, base_bot: BaseBotABC) -> BotEvent:
        """Map a schema event to a bot-api event."""
        event_type = type(event).__name__

        if isinstance(event, SchemaBotDeathEvent):
            return EventMapper._map_bot_death_event(event, base_bot.get_my_id())
        if isinstance(event, SchemaBotHitBotEvent):
            return EventMapper._map_hit_bot_event(event)
        if isinstance(event, SchemaBotHitWallEvent):
            return EventMapper._map_hit_wall_event(event)
        if isinstance(event, SchemaBulletFiredEvent):
            return EventMapper._map_bullet_fired_event(event)
        if isinstance(event, SchemaBulletHitBotEvent):
            return EventMapper._map_bullet_hit_bot_event(event, base_bot.get_my_id())
        if isinstance(event, SchemaBulletHitBulletEvent):
            return EventMapper._map_bullet_hit_bullet_event(event)
        if isinstance(event, SchemaBulletHitWallEvent):
            return EventMapper._map_bullet_hit_wall_event(event)
        if isinstance(event, SchemaScannedBotEvent):
            return EventMapper._map_scanned_bot_event(event)
        if isinstance(event, SchemaSkippedTurnEvent):
            return EventMapper.map_skipped_turn_event(event)
        if isinstance(event, SchemaWonRoundEvent):
            return EventMapper._map_won_round_event(event)
        if isinstance(event, SchemaTeamMessageEvent):
            return EventMapper._map_team_message_event(event)

        raise BotException(f"No mapping exists for event type: {event_type}")

    @staticmethod
    def _map_bot_death_event(source: SchemaBotDeathEvent, my_bot_id: int) -> Union[DeathEvent, BotDeathEvent]:
        """Map a schema BotDeathEvent to a bot-api event."""
        if source.victim_id == my_bot_id:
            return DeathEvent(source.turn_number)
        return BotDeathEvent(source.turn_number, source.victim_id)

    @staticmethod
    def _map_hit_bot_event(source: SchemaBotHitBotEvent) -> HitBotEvent:
        """Map a schema BotHitBotEvent to a bot-api HitBotEvent."""
        return HitBotEvent(
            source.turn_number,
            source.victim_id,
            source.energy,
            source.x,
            source.y,
            source.rammed
        )

    @staticmethod
    def _map_hit_wall_event(source: SchemaBotHitWallEvent) -> HitWallEvent:
        """Map a schema BotHitWallEvent to a bot-api HitWallEvent."""
        return HitWallEvent(source.turn_number)

    @staticmethod
    def _map_bullet_fired_event(source: SchemaBulletFiredEvent) -> BulletFiredEvent:
        """Map a schema BulletFiredEvent to a bot-api BulletFiredEvent."""
        return BulletFiredEvent(
            source.turn_number,
            BulletStateMapper.map(source.bullet)
        )

    @staticmethod
    def _map_bullet_hit_bot_event(source: SchemaBulletHitBotEvent, my_bot_id: int) -> Union[
        HitByBulletEvent, BulletHitBotEvent]:
        """Map a schema BulletHitBotEvent to a bot-api event."""
        bullet = BulletStateMapper.map(source.bullet)
        if source.victim_id == my_bot_id:
            return HitByBulletEvent(
                source.turn_number,
                bullet,
                source.damage,
                source.energy
            )
        return BulletHitBotEvent(
            source.turn_number,
            source.victim_id,
            bullet,
            source.damage,
            source.energy
        )

    @staticmethod
    def _map_bullet_hit_bullet_event(source: SchemaBulletHitBulletEvent) -> BulletHitBulletEvent:
        """Map a schema BulletHitBulletEvent to a bot-api BulletHitBulletEvent."""
        return BulletHitBulletEvent(
            source.turn_number,
            BulletStateMapper.map(source.bullet),
            BulletStateMapper.map(source.hit_bullet)
        )

    @staticmethod
    def _map_bullet_hit_wall_event(source: SchemaBulletHitWallEvent) -> BulletHitWallEvent:
        """Map a schema BulletHitWallEvent to a bot-api BulletHitWallEvent."""
        return BulletHitWallEvent(
            source.turn_number,
            BulletStateMapper.map(source.bullet)
        )

    @staticmethod
    def _map_scanned_bot_event(source: SchemaScannedBotEvent) -> ScannedBotEvent:
        """Map a schema ScannedBotEvent to a bot-api ScannedBotEvent."""
        return ScannedBotEvent(
            source.turn_number,
            source.scanned_by_bot_id,
            source.scanned_bot_id,
            source.energy,
            source.x,
            source.y,
            source.direction,
            source.speed
        )

    @staticmethod
    def map_skipped_turn_event(source: SchemaSkippedTurnEvent) -> SkippedTurnEvent:
        """Map a schema SkippedTurnEvent to a bot-api SkippedTurnEvent."""
        return SkippedTurnEvent(source.turn_number)

    @staticmethod
    def _map_won_round_event(source: SchemaWonRoundEvent) -> WonRoundEvent:
        """Map a schema WonRoundEvent to a bot-api WonRoundEvent."""
        return WonRoundEvent(source.turn_number)

    @staticmethod
    def _map_team_message_event(source: SchemaTeamMessageEvent) -> TeamMessageEvent:
        """Map a schema TeamMessageEvent to a bot-api TeamMessageEvent."""
        message = source.message
        if message is None:  # type: ignore
            raise BotException("message in TeamMessageEvent is None")

        try:
            message_object: Dict[str, Any] = json.loads(message)
            return TeamMessageEvent(source.turn_number, message_object, source.sender_id)
        except json.JSONDecodeError as e:
            raise BotException(f"Could not parse team message: {str(e)}")
