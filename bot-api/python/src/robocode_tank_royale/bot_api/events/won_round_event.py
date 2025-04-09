from robocode_tank_royale.bot_api.events import BotEvent


class WonRoundEvent(BotEvent):
    """
    Represents an event triggered when a bot has won the round.
    """

    def is_critical(self) -> bool:
        """
        Indicates whether this event is critical.

        Returns:
            bool: Always returns True, as this event is considered critical.
        """
        return True