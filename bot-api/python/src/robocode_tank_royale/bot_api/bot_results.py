from dataclasses import dataclass

@dataclass
class BotResults:
    """Represents individual bot results."""

    rank: int
    """Rank/placement of the bot, where 1 means 1st place, 4 means 4th place etc."""

    survival: float
    """Accumulated survival score. Every bot still alive scores 50 points every time another bot is defeated."""

    last_survivor_bonus: float
    """Last survivor score. The last bot alive scores 10 points or each bot that has been defeated."""

    bullet_damage: float
    """Bullet damage score. A bot scores 1 point for each point of damage they do to other bots."""

    bullet_kill_bonus: float
    """Bullet kill-bonus. When a bot kills another bot, it scores an additional 20% points of the total damage it did
    to that bot.
    """

    ram_damage: float
    """Ram damage score. Bots score 2 points for each point of damage inflicted by ramming an enemy bot. Ramming is the
    act deliberately driving forward (not backward) and hitting another bot.
    """

    ram_kill_bonus: float
    """Ram kill-bonus. When a bot kills another bot due to ramming, it scores an additional 30% of the total ramming
    damage it did to that bot.
    """

    total_score: float
    """Total score is the sum of all scores and determines the ranking."""

    first_places: int
    """Number of 1st places for the bot."""

    second_places: int
    """Number of 2nd places for the bot."""

    third_places: int
    """Number of 3rd places for the bot."""