import threading

import pytest

from robocode_tank_royale.bot_api.bot_info import BotInfo
from robocode_tank_royale.bot_api.base_bot import BaseBot
from robocode_tank_royale.bot_api.internal.thread_interrupted_exception import ThreadInterruptedException


class TestBot(BaseBot):
    def __init__(self):
        super().__init__(BotInfo(name="LifecycleTest", version="1.0", authors=["Test"], game_types={"classic"}))

    def run(self):
        pass


@pytest.mark.BOT
def test_stale_thread_is_rejected_after_round_owner_changes():
    internals = TestBot()._internals
    internals.set_running(True)
    internals.thread = threading.Thread()

    with pytest.raises(ThreadInterruptedException):
        internals._stop_rogue_thread()
