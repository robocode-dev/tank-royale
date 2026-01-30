"""Debug script to understand test failures."""

import sys
import threading
import time
from pathlib import Path

# Add tests directory to path
TESTS_DIR = Path(__file__).resolve().parent / "tests"
sys.path.insert(0, str(TESTS_DIR))

from tests.test_utils.mocked_server import MockedServer
from tests.test_utils.test_bot_factory import TestBotBuilder

def test_on_tick_callback():
    """TestBotBuilder on_tick callback is invoked."""
    server = MockedServer()
    server.start()

    try:
        tick_called = threading.Event()

        bot = (TestBotBuilder()
            .on_tick(lambda e: tick_called.set())
            .build())

        print("Bot created, starting bot thread...")

        # Start bot in separate thread
        bot_thread = threading.Thread(target=bot.start, daemon=True)
        bot_thread.start()

        print("Waiting for bot ready...")
        if not server.await_bot_ready(2000):
            print("ERROR: Bot ready timed out")
            return False

        print("Bot is ready, waiting for tick callback...")
        if not tick_called.wait(timeout=2.0):
            print("ERROR: Tick callback was not called")
            return False

        print("SUCCESS: Tick callback was called")
        return True
    except Exception as e:
        print(f"ERROR: {e}")
        import traceback
        traceback.print_exc()
        return False
    finally:
        print("Stopping server...")
        server.stop()

if __name__ == "__main__":
    success = test_on_tick_callback()
    print(f"\nTest result: {'PASS' if success else 'FAIL'}")
    sys.exit(0 if success else 1)
