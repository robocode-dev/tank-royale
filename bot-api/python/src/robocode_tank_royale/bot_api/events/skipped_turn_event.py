from robocode_tank_royale.bot_api.events import BotEvent


class SkippedTurnEvent(BotEvent):
    """
    Represents an event triggered when the bot has skipped a turn.

    This event occurs when the bot fails to take its turn within the allotted time frame.
    """

    def is_critical(self) -> bool:
        """
        Indicates whether this event is critical.

        Returns:
            bool: Always returns True, as skipping a turn is considered a critical event.
        """
        return True