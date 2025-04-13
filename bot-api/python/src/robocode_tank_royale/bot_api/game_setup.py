from dataclasses import dataclass


@dataclass(frozen=True)
class GameSetup:
    """Game setup retrieved when game is started."""

    game_type: str
    """Game type, e.g. "melee"."""

    arena_width: int
    """Width of the arena measured in units."""

    arena_height: int
    """Height of the arena measured in units."""

    number_of_rounds: int
    """Number of rounds in a battle."""

    gun_cooling_rate: float
    """Gun cooling rate. The gun needs to cool down to a gun heat of zero before the gun is able to fire."""

    max_inactivity_turns: int
    """Maximum number of inactive turns allowed, where a bot does not take any action before it is zapped by the game."""

    turn_timeout: int
    """Timeout in milliseconds for sending intent after having received 'tick' message."""

    ready_timeout: int
    """Time limit in milliseconds for sending ready message after having received 'new battle' message."""
