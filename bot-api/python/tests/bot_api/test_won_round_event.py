import pytest
import threading
import unittest
from robocode_tank_royale.bot_api import BotInfo, Bot
from robocode_tank_royale.bot_api.events import WonRoundEvent
from tests.bot_api.abstract_bot_test import AbstractBotTest

@pytest.mark.TCK
@pytest.mark.TR_API_TCK_005
class TestWonRoundEvent(AbstractBotTest):
    def test_won_round_event_delivery(self):
        """TR-API-TCK-005 WonRoundEvent is delivered to on_won_round"""
        won_round_event_received = threading.Event()
        
        class TestBot(Bot):
            def on_won_round(self, won_round_event: WonRoundEvent):
                won_round_event_received.set()
        
        bot = TestBot(self.bot_info, server_url=self.server.server_url)
        
        # Use AbstractBotTest.start_bot to handle the handshake and first intent draining
        self.start_bot(bot)
        
        # Add WonRoundEvent to the next tick
        self.server.add_event({
            "type": "WonRoundEvent",
            "turnNumber": 2 # next turn
        })
        
        # Trigger next tick and drain intent
        self.server.set_bot_state_and_await_tick()
        self.server.continue_bot_intent()
        self.server.await_bot_intent(1000)
        
        # Verify event received
        assert won_round_event_received.wait(timeout=2.0), "on_won_round should be called"

if __name__ == "__main__":
    unittest.main()
