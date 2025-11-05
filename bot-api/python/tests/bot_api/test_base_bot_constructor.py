import asyncio
import json
import os
from typing import Any

import pytest
from unittest.mock import AsyncMock, patch

from robocode_tank_royale.bot_api.base_bot import BaseBot
from robocode_tank_royale.bot_api.internal.env_vars import EnvVars
from robocode_tank_royale.bot_api import BotInfo
from tests.test_utils.mocked_server import MockedServer


def test_TR_API_BOT_001a_env_read_and_defaults_handshake_fields_from_env():
    """TR-API-BOT-001a ENV read & defaults: with required vars present, constructor reads values
    and sends a handshake reflecting ENV; with SERVER_URL set, bot connects to that URL."""
    # Arrange environment
    # Provide a specific server URL and mock the websocket connection
    os.environ[EnvVars.ServerUrl] = "ws://127.0.0.1:12345"
    os.environ[EnvVars.BotName] = "PyTestBot"
    os.environ[EnvVars.BotVersion] = "1.2.3"
    os.environ[EnvVars.BotAuthors] = "Alice, Bob"
    os.environ[EnvVars.BotDescription] = "Some description"
    os.environ[EnvVars.BotHomepage] = "https://example.org"
    os.environ[EnvVars.BotCountryCodes] = "gb, US"
    os.environ[EnvVars.BotGameTypes] = "classic, 1v1, melee"
    os.environ[EnvVars.BotPlatform] = "CPython"
    os.environ[EnvVars.BotProgrammingLang] = "Python"

    try:
        # Act
        bot = BaseBot()
        asyncio.run(bot.start())

        # Wait for handshake to be received by mocked server
        assert server.await_bot_handshake(3000), "Did not receive BotHandshake in time"
        hs = server.handshake
        assert hs is not None

        # Assert handshake reflects environment variables
        assert hs.name == os.environ[EnvVars.BotName]
        assert hs.version == os.environ[EnvVars.BotVersion]
        assert list(hs.authors) == ["Alice", "Bob"]
        # country codes are preserved (case-insensitive comparison)
        cc_env = [c.strip().lower() for c in os.environ[EnvVars.BotCountryCodes].split(",")]
        assert [c.lower() for c in list(hs.country_codes)] == cc_env
        assert hs.description == os.environ[EnvVars.BotDescription]
        assert hs.homepage == os.environ[EnvVars.BotHomepage]
        assert hs.platform == os.environ[EnvVars.BotPlatform]
        assert hs.programming_lang == os.environ[EnvVars.BotProgrammingLang]
    finally:
        server.stop()


def test_TR_API_BOT_001a_default_server_url_is_used_when_env_absent():
    """TR-API-BOT-001a ENV read & defaults: when SERVER_URL is absent, default ws://localhost:7654 is used."""
    # Arrange required bot info env (no SERVER_URL)
    os.environ.pop(EnvVars.ServerUrl, None)
    os.environ[EnvVars.BotName] = "PyTestBot"
    os.environ[EnvVars.BotVersion] = "0.1"
    os.environ[EnvVars.BotAuthors] = "Tester"

    server_handshake: dict[str, Any] = {
        "type": "ServerHandshake",
        "sessionId": "sess-1",
        "variant": "RobocodeTankRoyale",
        "version": "1.0",
        "gameTypes": ["classic"],
    }

    server_messages = [json.dumps(server_handshake)]

    async def message_iterator(*_):
        for msg in server_messages:
            yield msg
        # simulate close after handshake
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

        # Assert default URL used
        mock_connect.assert_called_once_with("ws://localhost:7654")
        # Sent handshake contains env-based info
        sent = json.loads(mock_ws.send.call_args[0][0])
        assert sent["type"] == "BotHandshake"
        assert sent["name"] == os.environ[EnvVars.BotName]
        assert sent["version"] == os.environ[EnvVars.BotVersion]
        assert sent["authors"] == ["Tester"]


def test_TR_API_BOT_001a_missing_required_env_raises_exception():
    """TR-API-BOT-001a ENV read & defaults: missing required vars cause a clear error."""
    # Ensure missing required BOT_NAME
    os.environ.pop(EnvVars.BotName, None)
    os.environ[EnvVars.BotVersion] = "1.0"
    os.environ[EnvVars.BotAuthors] = "A"

    with pytest.raises(Exception) as exc:
        # BaseBot will attempt to read env-based BotInfo via BaseBotInternals
        _ = BaseBot()
    assert EnvVars.MissingEnvValue in str(exc.value)
    assert EnvVars.BotName in str(exc.value)
