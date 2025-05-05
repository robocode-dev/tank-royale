from robocode_tank_royale.bot_api.bullet_state import BulletState
from robocode_tank_royale.schema import BulletState as SchemaBulletState


class BulletStateMapper:
    """Utility class for mapping bullet states."""

    @staticmethod
    def map(source: SchemaBulletState) -> BulletState | None:
        """Map a schema BulletState to a bot-api BulletState."""
        if source is None:
            return None

        if isinstance(source, list):
            return BulletStateMapper.map_list(source)

        return BulletState(
            source.bullet_id,
            source.owner_id,
            source.power,
            source.x,
            source.y,
            source.direction,
            source.color
        )

    @staticmethod
    def map_list(source_list) -> list[BulletState] | None:
        """Map a list of schema BulletStates to a list of bot-api BulletStates."""
        if source_list is None:
            return None

        mapped_list = []
        for source in source_list:
            mapped_list.append(BulletStateMapper.map(source))
        return mapped_list
