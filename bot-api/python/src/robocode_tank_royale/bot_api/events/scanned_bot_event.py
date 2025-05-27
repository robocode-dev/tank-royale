from dataclasses import dataclass

from .bot_event import BotEvent


@dataclass(frozen=True, repr=True)
class ScannedBotEvent(BotEvent):
    """
    Event triggered when a bot scans another bot.

    Attributes:
        scanned_by_bot_id (int): The ID of the bot that performed the scanning.
        scanned_bot_id (int): The ID of the bot that was scanned.
        energy (float): The energy level of the scanned bot.
        x (float): The X-coordinate of the scanned bot.
        y (float): The Y-coordinate of the scanned bot.
        direction (float): The direction (in degrees) of the scanned bot.
        speed (float): The speed (in units per turn) of the scanned bot.
    """

    scanned_by_bot_id: int
    scanned_bot_id: int
    energy: float
    x: float
    y: float
    direction: float
    speed: float
