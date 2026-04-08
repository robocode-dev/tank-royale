import os
import sys
from ..bot_info import BotInfo
from ..mapper import InitialPositionMapper
from .env_vars import EnvVars

from robocode_tank_royale.schema import BotHandshake
from robocode_tank_royale.schema.message import Message


class BotHandshakeFactory:
    @staticmethod
    def create(
        session_id: str, bot_info: BotInfo, is_droid: bool, secret: "str | None"
    ) -> BotHandshake:
        handshake = BotHandshake(
            session_id=session_id,
            type=Message.Type.BOT_HANDSHAKE,  # type: ignore
            name=bot_info.name,
            version=bot_info.version,
            authors=list(bot_info.authors),
            description=bot_info.description,  # type: ignore
            homepage=bot_info.homepage,  # type: ignore
            country_codes=list(bot_info.country_codes),
            game_types=list(bot_info.game_types),
            platform=bot_info.platform,
            programming_lang=bot_info.programming_lang,  # type: ignore
            initial_position=InitialPositionMapper.map(bot_info.initial_position),  # type: ignore
            team_id=EnvVars.get_team_id(),  # type: ignore
            team_name=EnvVars.get_team_name(),  # type: ignore
            team_version=EnvVars.get_team_version(),  # type: ignore
            is_droid=is_droid,
            secret=secret,
        )

        # Set debugger_attached field (ADR-0035)
        debugger_attached = BotHandshakeFactory.is_debugger_attached()
        handshake.debugger_attached = debugger_attached

        # Log hint if debugger is detected
        if debugger_attached:
            print(
                "Debugger detected. Consider enabling breakpoint mode for this bot in the controller."
            )

        return handshake

    @staticmethod
    def is_debugger_attached() -> bool:
        """
        Detects if a debugger is attached to the process.
        @return true if a debugger is attached, false otherwise
        """
        # Check for ROBOCODE_DEBUG environment variable override
        env = os.environ.get("ROBOCODE_DEBUG", "").lower()
        if env == "true":
            return True
        if env == "false":
            return False

        # Check for trace function (pydevd, debugpy, etc.)
        if sys.gettrace() is not None:
            return True

        # Check for specific debugger modules
        return "debugpy" in sys.modules or "pydevd" in sys.modules
