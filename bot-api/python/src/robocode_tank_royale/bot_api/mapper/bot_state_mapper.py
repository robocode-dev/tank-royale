from ..bot_state import BotState
from robocode_tank_royale.schema import BotState as SchemaBotState


class BotStateMapper:
    """Utility class for mapping bot states."""

    @staticmethod
    def map(source: SchemaBotState | None) -> BotState | None:
        """Map a schema BotState to a bot-api BotState."""
        if source is None:
            return None

        return BotState(
            source.is_droid,
            source.energy,
            source.x,
            source.y,
            source.direction,
            source.gun_direction,
            source.radar_direction,
            source.radar_sweep,
            source.speed,
            source.turn_rate,
            source.gun_turn_rate,
            source.radar_turn_rate,
            source.gun_heat,
            source.enemy_count,
            source.body_color,  # type: ignore
            source.turret_color,  # type: ignore
            source.radar_color,  # type: ignore
            source.bullet_color,  # type: ignore
            source.scan_color,  # type: ignore
            source.tracks_color,  # type: ignore
            source.gun_color,  # type: ignore
            source.is_debugging_enabled,
        )
