from ..bot_results import BotResults
from robocode_tank_royale.schema.results_for_bot import ResultsForBot


class ResultsMapper:
    """Utility class for mapping results."""

    @staticmethod
    def map(source: ResultsForBot) -> BotResults:
        """Map schema ResultsForBot to a bot-api ResultsForBot."""
        return BotResults(
            source.rank,
            source.survival,
            source.last_survivor_bonus,
            source.bullet_damage,
            source.bullet_kill_bonus,
            source.ram_damage,
            source.ram_kill_bonus,
            source.total_score,
            source.first_places,
            source.second_places,
            source.third_places
        )
