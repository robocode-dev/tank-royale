import unittest

from robocode_tank_royale.bot_api import BotInfo, Bot
from tests.test_utils.mocked_server import MockedServer


class TestCommandsRadar(unittest.IsolatedAsyncioTestCase):
    """TR-API-CMD-003 Radar/Scan commands"""

    async def asyncSetUp(self) -> None:
        self.server = MockedServer()
        self.server.start()

    async def asyncTearDown(self) -> None:
        self.server.stop()

    async def test_TR_API_CMD_003_rescan_intent(self):
        """TR-API-CMD-003 Radar commands: rescan flag is set in intent"""
        # Arrange
        bot = Bot(
            bot_info=BotInfo(name="RadarBot", version="1.0", authors=["Tester"]),
            server_url=self.server.server_url,
        )
        await bot.start()
        self.assertTrue(self.server.await_game_started(1000))

        # Act
        bot.rescan = True
        await bot.go()
        self.assertTrue(self.server.await_bot_intent(1000))

        # Assert
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertIsNotNone(intent)
        self.assertTrue(intent.rescan)

    async def test_TR_API_CMD_003_blocking_rescan(self):
        """TR-API-CMD-003 Radar commands: blocking rescan() method"""
        # Arrange
        class RescanTestBot(Bot):
            def run(self):
                # Do nothing, we will call rescan from outside
                pass

        bot = RescanTestBot(
            bot_info=BotInfo(name="RadarBot", version="1.0", authors=["Tester"]),
            server_url=self.server.server_url,
        )
        await bot.start()
        self.assertTrue(self.server.await_game_started(1000))

        # Act (rescan is blocking and calls go() internally)
        import asyncio
        task = asyncio.create_task(bot.rescan())

        # Wait a bit for rescan to trigger intent
        await asyncio.sleep(0.1)
        self.assertTrue(self.server.await_bot_intent(1000))

        # Assert
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertIsNotNone(intent)
        self.assertTrue(intent.rescan)

        # Cleanup
        await task

    async def test_TR_API_CMD_003_adjust_radar_body(self):
        """TR-API-CMD-003 Radar commands: adjust radar for body turn can be toggled"""
        # Arrange
        bot = Bot(
            bot_info=BotInfo(name="RadarBot", version="1.0", authors=["Tester"]),
            server_url=self.server.server_url,
        )
        await bot.start()
        self.assertTrue(self.server.await_game_started(1000))

        # Act - set to True
        bot.adjust_radar_for_body_turn = True
        await bot.go()
        self.assertTrue(self.server.await_bot_intent(1000))

        # Assert
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertIsNotNone(intent)
        self.assertTrue(intent.adjust_radar_for_body_turn)

        # Reset for next step
        self.server.reset_bot_intent_event()

        # Act - set to False
        bot.adjust_radar_for_body_turn = False
        await bot.go()
        self.assertTrue(self.server.await_bot_intent(1000))

        # Assert
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertFalse(intent.adjust_radar_for_body_turn)

    async def test_TR_API_CMD_003_adjust_radar_gun(self):
        """TR-API-CMD-003 Radar commands: adjust radar for gun turn and fire assist interaction"""
        # Arrange
        bot = Bot(
            bot_info=BotInfo(name="RadarBot", version="1.0", authors=["Tester"]),
            server_url=self.server.server_url,
        )
        await bot.start()
        self.assertTrue(self.server.await_game_started(1000))

        # Act - set to True
        bot.adjust_radar_for_gun_turn = True
        await bot.go()
        self.assertTrue(self.server.await_bot_intent(1000))

        # Assert - fire assist should be False when radar adjust is True
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertIsNotNone(intent)
        self.assertTrue(intent.adjust_radar_for_gun_turn)
        self.assertFalse(intent.fire_assist)

        # Reset for next step
        self.server.reset_bot_intent_event()

        # Act - set to False
        bot.adjust_radar_for_gun_turn = False
        await bot.go()
        self.assertTrue(self.server.await_bot_intent(1000))

        # Assert - fire assist should be True when radar adjust is False
        intent = self.server._bot_intent  # type: ignore[attr-defined]
        self.assertFalse(intent.adjust_radar_for_gun_turn)
        self.assertTrue(intent.fire_assist)


if __name__ == "__main__":
    unittest.main()

