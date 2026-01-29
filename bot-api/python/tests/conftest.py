"""Pytest configuration for local test utilities import path and global timeout."""

from __future__ import annotations

import sys
from pathlib import Path

TESTS_DIR = Path(__file__).resolve().parent
if str(TESTS_DIR) not in sys.path:
    sys.path.insert(0, str(TESTS_DIR))

# Note: Global timeout of 10 seconds is configured in pyproject.toml
# [tool.pytest.ini_options] timeout = 10
# This prevents tests from hanging indefinitely.

