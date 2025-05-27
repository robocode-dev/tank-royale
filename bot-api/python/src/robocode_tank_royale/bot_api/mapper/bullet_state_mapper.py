from typing import Sequence, overload

from ..bullet_state import BulletState
from robocode_tank_royale.schema import BulletState as SchemaBulletState


def _map_bullet_state(
    source: SchemaBulletState | None,
) -> BulletState | None:
    """Map a schema BulletState to a bot-api BulletState."""
    if source is None:
        return None

    return BulletState(
        source.bullet_id,
        source.owner_id,
        source.power,
        source.x,
        source.y,
        source.direction,
        source.color  # type: ignore
    )


class BulletStateMapper:
    """Utility class for mapping bullet states."""

    @overload
    @staticmethod
    def map(source: SchemaBulletState) -> BulletState:
        """Map a schema BulletState to a bot-api BulletState."""
        ...

    @overload
    @staticmethod
    def map(source: Sequence[SchemaBulletState | None]) -> Sequence[BulletState | None]:
        """Map a sequence of schema BulletState to a sequence of bot-api BulletState."""
        ...

    @overload
    @staticmethod
    def map(source: None) -> None:
        """Map a sequence of schema BulletState to a sequence of bot-api BulletState."""
        ...

    @staticmethod
    def map(
        source: SchemaBulletState | Sequence[SchemaBulletState | None] | None,
    ) -> BulletState | Sequence[BulletState | None] | None:
        """Map a schema BulletState to a bot-api BulletState."""
        if source is None:
            return None

        if isinstance(source, Sequence):
            return [_map_bullet_state(s) for s in source]
        return _map_bullet_state(source)
