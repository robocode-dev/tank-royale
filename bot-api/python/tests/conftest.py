"""Pytest configuration for local test utilities import path and global timeout."""

from __future__ import annotations

import sys
import os
from pathlib import Path

import pytest

TESTS_DIR = Path(__file__).resolve().parent
if str(TESTS_DIR) not in sys.path:
    sys.path.insert(0, str(TESTS_DIR))


@pytest.fixture(scope="session", autouse=True)
def force_exit_after_tests():
    """
    Force Python to exit after tests complete.

    This is necessary because asyncio's default ThreadPoolExecutor and
    websockets library create threads that don't shut down cleanly,
    causing pytest to hang after all tests pass.

    Using os._exit() bypasses Python's normal shutdown sequence which
    waits for all non-daemon threads to complete.
    """
    yield
    # Force exit after all tests complete
    os._exit(0)


# Note: Global timeout of 10 seconds is configured in pyproject.toml
# [tool.pytest.ini_options] timeout = 10
# This prevents tests from hanging indefinitely.

