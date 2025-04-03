from robocode_tank_royale.bot_api.events import BotEvent


class BotDeathEvent(BotEvent):
    """Event occurring when another bot has died."""

    def __init__(self, turn_number: int, victim_id: int):
        """Initializes a new instance of the BotDeathEvent class.

        Args:
            turn_number: The turn number when the bot died.
            victim_id: The id of the bot that has died.
        """
        super().__init__(turn_number)
        self.victim_id = victim_id

    def get_victim_id(self) -> int:
        """Returns the id of the bot that has died.

        Returns:
            The id of the bot that has died.
        """
        return self.victim_id