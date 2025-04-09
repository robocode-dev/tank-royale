from robocode_tank_royale.bot_api.events import BotEvent


class DeathEvent(BotEvent):
    """
    Represents an event triggered when your bot has died.
    """

    def is_critical(self) -> bool:
        """
        Indicates whether this event is critical.

        Returns:
            bool: Always returns True, as this event is critical.
        """
        return True