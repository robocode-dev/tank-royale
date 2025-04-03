from robocode_tank_royale.bot_api.events.bot_event import BotEvent


class WonRoundEvent(BotEvent):
    """Event occurring when a bot has won the round."""

    def is_critical(self) -> bool:
        """
        Returns:
            True

        This event is critical.
        """
        return True