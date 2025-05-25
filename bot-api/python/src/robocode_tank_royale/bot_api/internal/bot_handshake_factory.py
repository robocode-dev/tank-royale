from robocode_tank_royale.bot_api import BotInfo
from robocode_tank_royale.bot_api.internal.env_vars import EnvVars
from robocode_tank_royale.bot_api.mapper import InitialPositionMapper
from robocode_tank_royale.schema import BotHandshake
from robocode_tank_royale.schema.message import Message

class BotHandshakeFactory:

    @staticmethod
    def create(session_id: str, bot_info: BotInfo, is_droid: bool, secret: str) -> BotHandshake:
        return BotHandshake(
            session_id = session_id,
            type = Message.Type.BOT_HANDSHAKE,
            name = bot_info.name,
            version = bot_info.version,
            authors = list(bot_info.authors),
            description = bot_info.description,
            homepage = bot_info.homepage,
            country_codes = list(bot_info.country_codes),
            game_types = list(bot_info.game_types),
            platform = bot_info.platform,
            programming_lang = bot_info.programming_lang,
            initial_position = InitialPositionMapper.map(bot_info.initial_position),
            team_id = EnvVars.get_team_id(),
            team_name = EnvVars.get_team_name(),
            team_version = EnvVars.get_team_version(),
            is_droid = is_droid,
            secret = secret
        )
