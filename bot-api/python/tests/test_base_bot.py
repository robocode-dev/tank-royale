import unittest
from unittest.mock import patch, AsyncMock
import json
import asyncio
import websockets
import collections

from robocode_tank_royale.bot_api import Bot, BotInfo

# The websockets library uses a named tuple for close information.
Close = collections.namedtuple("Close", ["code", "reason"])

class TestBaseBot(unittest.IsolatedAsyncioTestCase):

    @patch('websockets.connect', new_callable=AsyncMock)
    async def test_initialization_default(self, mock_connect):
        # Given
        server_handshake = {
            'type': 'ServerHandshake',
            'sessionId': 'test-session',
            'variant': 'RobocodeTankRoyale',
            'version': '1.0',
            'gameTypes': ['melee', '1v1'],
        }
        
        server_messages = [json.dumps(server_handshake)]

        async def message_iterator(*args):
            for msg in server_messages:
                yield msg
            # After yielding messages, simulate the server closing the connection.
            # This will raise ConnectionClosed in the `receive_messages` loop.
            raise websockets.exceptions.ConnectionClosed(
                rcvd=Close(code=1000, reason='Normal Closure'),
                sent=None
            )

        mock_ws_connection = AsyncMock()
        mock_ws_connection.__aiter__ = message_iterator
        mock_ws_connection.send = AsyncMock()
        mock_ws_connection.close = AsyncMock()

        mock_connect.return_value = mock_ws_connection

        # When
        b = Bot(bot_info=BotInfo(name="TestBot", version="0.42", authors=["Tester"]), server_secret='RECTjjm7ntrLpoYFh+kDuA/LHONbTYsLEnLMbuCnaU')
        self.assertIsNotNone(b)
        
        # The start() method should complete and not hang.
        await b.start()

        # Then
        mock_connect.assert_called_once_with('ws://localhost:7654')
        
        mock_ws_connection.send.assert_called_once()
        sent_handshake = json.loads(mock_ws_connection.send.call_args[0][0])
        self.assertEqual(sent_handshake['$type'], 'BotHandshake')
        self.assertEqual(sent_handshake['name'], 'TestBot')

if __name__ == "__main__":
    unittest.main()