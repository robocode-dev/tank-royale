from typing import Type
from ..default_event_priority import DefaultEventPriority as Priority
from ..events import *


class EventPriorities:
    """
    Manages priorities for bot events in the game.

    This class maintains a registry of event priorities that determine the order
    in which events are processed by the bot API.
    """

    def __init__(self):
        """
        Private constructor to prevent instantiation.
        """
        raise NotImplementedError("This class cannot be instantiated")

    @staticmethod
    def initialize_event_priorities() -> dict[Type[BotEvent], int]:
        """
        Initializes the default event priorities map.

        Returns:
            dict: A map containing default priority values for all supported event types.
        """
        return {
            WonRoundEvent: Priority.WON_ROUND,
            SkippedTurnEvent: Priority.SKIPPED_TURN,
            TickEvent: Priority.TICK,
            CustomEvent: Priority.CUSTOM,
            TeamMessageEvent: Priority.TEAM_MESSAGE,
            BotDeathEvent: Priority.BOT_DEATH,
            BulletHitWallEvent: Priority.BULLET_HIT_WALL,
            BulletHitBulletEvent: Priority.BULLET_HIT_BULLET,
            BulletHitBotEvent: Priority.BULLET_HIT_BOT,
            BulletFiredEvent: Priority.BULLET_FIRED,
            HitByBulletEvent: Priority.HIT_BY_BULLET,
            HitWallEvent: Priority.HIT_WALL,
            HitBotEvent: Priority.HIT_BOT,
            ScannedBotEvent: Priority.SCANNED_BOT,
            DeathEvent: Priority.DEATH,
        }

    EVENT_PRIORITIES = initialize_event_priorities()

    @staticmethod
    def set_priority(event_class: Type[BotEvent] | None, priority: int) -> None:
        """
        Sets the priority for a specific event class.

        Args:
            event_class (type): The event class to set priority for.
            priority (int): The priority value to assign.

        Raises:
            ValueError: If `event_class` is None.
        """
        if event_class is None:
            raise ValueError("Event class cannot be null")
        EventPriorities.EVENT_PRIORITIES[event_class] = priority

    @staticmethod
    def get_priority(event_class: Type[BotEvent] | None) -> int:
        """
        Gets the priority for a specific event class.

        Args:
            event_class (type): The event class to get priority for.

        Returns:
            int: The priority value for the specified event class.

        Raises:
            ValueError: If `event_class` is None.
            Exception: If no priority is defined for the event class.
        """
        if event_class is None:
            raise ValueError("Event class cannot be null")
        if event_class not in EventPriorities.EVENT_PRIORITIES:
            raise Exception(f"Could not get event priority for the class: {event_class.__name__}")
        return EventPriorities.EVENT_PRIORITIES[event_class]
