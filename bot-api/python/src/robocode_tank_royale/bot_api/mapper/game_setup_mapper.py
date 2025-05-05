from robocode_tank_royale.bot_api.game_setup import GameSetup


class GameSetupMapper:
    """Utility class for mapping game setups."""

    @staticmethod
    def map(source):
        """Map a schema GameSetup to a bot-api GameSetup."""

        return GameSetup(
            source.game_type,
            source.arena_width,
            source.arena_height,
            source.number_of_rounds,
            source.gun_cooling_rate,
            source.max_inactivity_turns,
            source.turn_timeout,
            source.ready_timeout,
        )
