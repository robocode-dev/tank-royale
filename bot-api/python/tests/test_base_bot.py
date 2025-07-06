import unittest

from robocode_tank_royale.bot_api import Bot, BotInfo

class TestBaseBot(unittest.IsolatedAsyncioTestCase):
    async def test_initialization_default(self):
        b = Bot(bot_info=BotInfo(name="TestBot", version="0.42", authors=["Tester"]), server_secret='RECTjjm7ntrLpoYFh+kDuA/LHONbTYsLEnLMbuCnaU')
        self.assertIsNotNone(b)
        await b.start()

if __name__ == "__main__":
    unittest.main()