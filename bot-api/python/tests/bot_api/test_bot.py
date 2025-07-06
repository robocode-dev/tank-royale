from typing import Any

import unittest
from unittest.mock import patch, AsyncMock
import json
import websockets

from robocode_tank_royale.bot_api import Bot, BotInfo


class TestBot(unittest.IsolatedAsyncioTestCase):

    @patch("websockets.connect", new_callable=AsyncMock)
    async def test_initialization_default(self, mock_connect: AsyncMock):
        # Given
        server_handshake: dict[str, Any] = {
            "type": "ServerHandshake",
            "sessionId": "test-session-42",
            "variant": "RobocodeTankRoyale",
            "version": "1.0",
            "gameTypes": ["melee", "1v1"],
        }

        server_messages = [json.dumps(server_handshake)]

        async def message_iterator(*_):
            for msg in server_messages:
                yield msg
            # After yielding messages, simulate the server closing the connection.
            # This will raise ConnectionClosed in the `receive_messages` loop.
            raise websockets.exceptions.ConnectionClosed(
                rcvd=websockets.frames.Close(code=1000, reason="Normal Closure"),
                sent=None,
            )

        mock_ws_connection = AsyncMock()
        mock_ws_connection.__aiter__ = message_iterator
        mock_ws_connection.send = AsyncMock()
        mock_ws_connection.close = AsyncMock()
        mock_connect.return_value = mock_ws_connection

        secret = "RECTjjm7ntrLpoYFh+kDuA/LHONbTYsLEnLMbuCnaU"
        b = Bot(
            bot_info=BotInfo(name="TestBot", version="0.42", authors=["Tester"]),
            server_secret=secret,
        )
        self.assertIsNotNone(b)

        # The start() method will connect to the mock server and perform a handshake before returning.
        await b.start()

        # Then
        mock_connect.assert_called_once_with("ws://localhost:7654")

        mock_ws_connection.send.assert_called_once()
        sent_handshake = json.loads(mock_ws_connection.send.call_args[0][0])
        self.assertEqual(sent_handshake["type"], "BotHandshake")
        self.assertEqual(sent_handshake["name"], "TestBot")
        self.assertEqual(sent_handshake["secret"], secret)
        self.assertEqual(sent_handshake["version"], "0.42")
        self.assertEqual(sent_handshake["authors"], ["Tester"])
        # Ensures that it received the server handshake and populated the session ID based on that.
        self.assertEqual(sent_handshake["session_id"], server_handshake["sessionId"])


if __name__ == "__main__":
    unittest.main()
