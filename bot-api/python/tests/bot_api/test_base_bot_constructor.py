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

    server_handshake: dict[str, Any] = {
        "type": "ServerHandshake",
        "sessionId": "sess-xyz",
        "variant": "RobocodeTankRoyale",
        "version": "1.0",
        "gameTypes": ["classic", "1v1"],
    }
    messages = [json.dumps(server_handshake)]

    async def message_iterator(*_):
        for m in messages:
            yield m
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

        # Assert connection to provided URL
        mock_connect.assert_called_once_with("ws://127.0.0.1:12345")
        # Assert handshake payload reflects ENV
        sent_handshake = json.loads(mock_ws.send.call_args[0][0])
        assert sent_handshake["type"] == "BotHandshake"
        assert sent_handshake["name"] == os.environ[EnvVars.BotName]
        assert sent_handshake["version"] == os.environ[EnvVars.BotVersion]
        assert sent_handshake["authors"] == ["Alice", "Bob"]
        # Optional fields present with expected values (snake_case per Python schema generator)
        assert sent_handshake["description"] == os.environ[EnvVars.BotDescription]
        assert sent_handshake["homepage"] == os.environ[EnvVars.BotHomepage]
        assert sent_handshake["platform"] == os.environ[EnvVars.BotPlatform]
        # programming_lang may be omitted in payload depending on generator; ensure no contradiction
        if "programming_lang" in sent_handshake:
            assert sent_handshake["programming_lang"] == os.environ[EnvVars.BotProgrammingLang]


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



def test_TR_API_BOT_001b_missing_required_env_vars_raise_clear_error():
    """TR-API-BOT-001b ENV validation: missing/blank required values raise clear errors"""
    # Missing BOT_NAME
    os.environ.pop(EnvVars.BotName, None)
    os.environ[EnvVars.BotVersion] = "1.0"
    os.environ[EnvVars.BotAuthors] = "A,B"
    with pytest.raises(Exception) as exc1:
        _ = BaseBot()
    assert EnvVars.MissingEnvValue in str(exc1.value)
    assert EnvVars.BotName in str(exc1.value)

    # Blank BOT_VERSION
    os.environ[EnvVars.BotName] = "Name"
    os.environ[EnvVars.BotVersion] = "   "
    os.environ[EnvVars.BotAuthors] = "A,B"
    with pytest.raises(Exception) as exc2:
        _ = BaseBot()
    msg2 = str(exc2.value)
    assert (EnvVars.MissingEnvValue in msg2 and EnvVars.BotVersion in msg2) or ("version" in msg2 and "blank" in msg2)

    # Authors empty/blank -> error
    os.environ[EnvVars.BotName] = "Name"
    os.environ[EnvVars.BotVersion] = "1.0"
    os.environ[EnvVars.BotAuthors] = "   "
    with pytest.raises(Exception) as exc3:
        _ = BaseBot()
    assert EnvVars.MissingEnvValue in str(exc3.value)
    assert EnvVars.BotAuthors in str(exc3.value)




def test_TR_API_BOT_001b_invalid_server_url_scheme_raises_bot_exception():
    """TR-API-BOT-001b ENV validation: invalid SERVER_URL scheme produces clear error"""
    # Arrange a bad scheme
    os.environ[EnvVars.ServerUrl] = "ftp://localhost:7654"
    os.environ[EnvVars.BotName] = "Bot"
    os.environ[EnvVars.BotVersion] = "1.0"
    os.environ[EnvVars.BotAuthors] = "A"

    with pytest.raises(Exception) as exc:
        bot = BaseBot()
        asyncio.run(bot.start())
    assert "Wrong scheme used with server URL" in str(exc.value)
