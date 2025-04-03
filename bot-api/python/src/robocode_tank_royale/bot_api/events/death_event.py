from robocode_tank_royale.bot_api.events.bot_event import BotEvent

class DeathEvent(BotEvent):
    """Event occurring when your bot has died."""

    def is_critical(self) -> bool:
        """
        Returns:
            True

        This event is critical.
        """
        return True