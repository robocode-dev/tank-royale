import os
import json
from unittest.mock import AsyncMock, patch

from robocode_tank_royale.bot_api.base_bot import BaseBot
from robocode_tank_royale.bot_api import BotInfo


def test_TR_API_BOT_001c_explicit_args_override_env_for_server_url_and_botinfo():
    """TR-API-BOT-001c Precedence: explicit args > ENV (Python has no Java system properties).

    With ENV providing one set of values and explicit constructor args providing another,
    the handshake must reflect the explicit args, and connection must use the explicit URL.
    """
    # Arrange ENV with bogus URL and different bot info
    os.environ["SERVER_URL"] = "ws://127.0.0.1:65535"  # bogus port so connection would fail if used
    os.environ["BOT_NAME"] = "EnvName"
    os.environ["BOT_VERSION"] = "9.9.9"
    os.environ["BOT_AUTHORS"] = "EnvAuthor"

    server_handshake = {
        "type": "ServerHandshake",
        "sessionId": "sess-42",
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

        # Explicit args
        explicit_info = BotInfo(name="ExplicitBot", version="1.0", authors=["Alice", "Bob"])
        explicit_url = "ws://localhost:7654"  # default good URL; explicit should be used over ENV

        bot = BaseBot(bot_info=explicit_info, server_url=explicit_url)
        bot.start()

        # Assert connect URL is explicit, not ENV
        mock_connect.assert_called_once_with(explicit_url)

        # Assert handshake reflects explicit BotInfo
        sent = json.loads(mock_ws.send.call_args[0][0])
        assert sent["type"] == "BotHandshake"
        assert sent["name"] == "ExplicitBot"
        assert sent["version"] == "1.0"
        assert sent["authors"] == ["Alice", "Bob"]
