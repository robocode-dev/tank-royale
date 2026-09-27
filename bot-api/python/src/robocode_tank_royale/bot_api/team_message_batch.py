from dataclasses import dataclass
from typing import Any, Iterable


@dataclass(frozen=True)
class TeamMessageBatch:
    """Immutable ordered payloads delivered as one team-message event on the next turn.

    A batch must contain at least one non-null value. Each entry counts toward the turn's logical-payload limit.
    """
    messages: tuple[Any, ...]

    def __init__(self, messages: Iterable[Any]):
        # Materialize first so a one-shot iterable (e.g. a generator) is validated and stored intact
        items = tuple(messages)
        if not items:
            raise ValueError("A team message batch must contain at least one message")
        if any(message is None for message in items):
            raise ValueError("A team message batch cannot contain null messages")
        object.__setattr__(self, "messages", items)
