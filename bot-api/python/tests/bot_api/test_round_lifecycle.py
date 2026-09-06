import threading
import time

import pytest

from robocode_tank_royale.bot_api.events.tick_event import TickEvent

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
def test_wait_for_next_turn_unwinds_when_no_bot_thread_owns_the_round():
    """A BaseBot with no run() loop owns no thread, so go() unwinds after sending the intent.

    This matches Java and .NET, where stopRogueThread() throws whenever the calling thread is
    not the round owner - including when no thread owns it at all.
    """
    internals = TestBot()._internals
    internals.set_running(True)
    internals.thread = None

    with pytest.raises(ThreadInterruptedException):
        internals._wait_for_next_turn(1)


@pytest.mark.BOT
def test_unexpected_error_from_run_still_drains_final_turn_events():
    """run() blowing up must not cost the bot its final-turn events.

    Mirrored by the Java, .NET and TypeScript lifecycle tests.
    """
    dispatched = []

    class ExplodingBot(TestBot):
        def run(self):
            raise RuntimeError("boom")

        def go(self):
            # End the post-run() pre-warm loop on the first go(), the way a stopped bot does.
            raise ThreadInterruptedException()

        def on_tick(self, event):
            dispatched.append(event)

    bot = ExplodingBot()
    internals = bot._internals
    tick = TickEvent(1, 1, None, [], [])
    internals.tick_event = tick
    internals.add_events_from_tick(tick)

    internals.start_thread(bot)
    deadline = time.monotonic() + 2.0
    while not dispatched and time.monotonic() < deadline:
        time.sleep(0.005)
    internals.stop_thread()

    assert len(dispatched) >= 1


@pytest.mark.BOT
def test_flush_final_turn_events_is_a_no_op_without_a_tick():
    """flush_final_turn_events runs on the WebSocket thread after ownership is released."""
    internals = TestBot()._internals
    internals.stop_thread()

    internals.flush_final_turn_events()  # no tick yet - must not raise
