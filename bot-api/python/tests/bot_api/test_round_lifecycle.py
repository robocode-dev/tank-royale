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
    """A thread that no longer owns the round is stopped by the blocking bot methods.

    _wait_for_next_turn is the production path every blocking bot method funnels through,
    so the guard is asserted there rather than on _stop_rogue_thread in isolation.
    """
    internals = TestBot()._internals
    internals.set_running(True)
    internals.thread = threading.Thread()  # the round is owned by some other thread

    with pytest.raises(ThreadInterruptedException):
        internals._wait_for_next_turn(1)


@pytest.mark.BOT
def test_wait_for_next_turn_returns_when_no_bot_thread_owns_the_round():
    """Test mode: with no bot thread, go() must not block - the intent was already sent."""
    internals = TestBot()._internals
    internals.set_running(True)
    internals.thread = None

    internals._wait_for_next_turn(1)  # must return rather than raise or hang


@pytest.mark.BOT
def test_flush_final_turn_events_is_a_no_op_without_a_tick():
    """flush_final_turn_events runs on the WebSocket thread after ownership is released."""
    internals = TestBot()._internals
    internals.stop_thread()

    internals.flush_final_turn_events()  # no tick yet - must not raise
