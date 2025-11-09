import asyncio
import json
import os
import pytest
from unittest.mock import AsyncMock, patch

from robocode_tank_royale.bot_api.base_bot import BaseBot
from robocode_tank_royale.bot_api.internal.env_vars import EnvVars


def test_TR_API_BOT_001d_type_parsing_normalization_team_id_and_initial_position():
    """TR-API-BOT-001d Type parsing/normalization: ints/bools parsed consistently; trimming/whitespace handling"""
    # Arrange required base env
    os.environ[EnvVars.ServerUrl] = "ws://127.0.0.1:12345"
    os.environ[EnvVars.BotName] = "PyTypeParse"
    os.environ[EnvVars.BotVersion] = "1.0"
    os.environ[EnvVars.BotAuthors] = "A,B"
    # Type/normalization cases
    os.environ[EnvVars.TeamId] = "  42  "
    os.environ[EnvVars.BotInitialPosition] = "  10, 20, 30  "

    server_handshake: dict[str, object] = {
        "type": "ServerHandshake",
        "sessionId": "sess-xyz",
        "variant": "RobocodeTankRoyale",
        "version": "1.0",
        "gameTypes": ["classic"],
    }

    async def message_iterator(*_):
        yield json.dumps(server_handshake)
        import websockets
        raise websockets.exceptions.ConnectionClosed(
            rcvd=websockets.frames.Close(code=1000, reason="Normal Closure"),
            sent=None,
        )

    with patch("websockets.connect", new_callable=AsyncMock) as mock_connect:
        mock_ws = AsyncMock()
        mock_ws.__aiter__ = message_iterator
        mock_ws.send = AsyncMock()
        mock_ws.close = AsyncMock()
        mock_connect.return_value = mock_ws

        # Act
        bot = BaseBot()
        asyncio.run(bot.start())

        # Assert handshake payload reflects parsed values
        sent_handshake = json.loads(mock_ws.send.call_args[0][0])
        assert sent_handshake["type"] == "BotHandshake"
        # Wire keys follow camelCase per schema
        assert sent_handshake.get("teamId") == 42
        assert sent_handshake.get("initialPosition") is not None
