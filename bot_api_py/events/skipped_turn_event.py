from bot_api_py.events.bot_event import BotEvent


class SkippedTurnEvent(BotEvent):
    """Event occurring when the bot has skipped a turn."""

    def is_critical(self) -> bool:
        """
        Returns:
            True

        This event is critical.
        """
        return True