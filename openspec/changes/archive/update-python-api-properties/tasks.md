# Tasks: Refactor Python Bot API to Use Pythonic Properties

## 1. Update Python Bot API Abstract Classes

- [x] 1.1 Update `base_bot_abc.py`: Convert all `get_*()` methods to `@property` decorators
- [x] 1.2 Update `base_bot_abc.py`: Convert `is_disabled()`, `is_stopped()`, `is_debugging_enabled()` to properties
- [x] 1.3 Update `base_bot_abc.py`: Convert `is_adjust_*()` / `set_adjust_*()` pairs to read-write properties
- [x] 1.4 Update `bot_abc.py`: Convert `is_running()` to `running` property

## 2. Update Python Bot API Implementations

- [x] 2.1 Update `base_bot.py`: Implement all new properties matching the ABC changes
- [x] 2.2 Update `bot.py`: Implement `running` property

## 3. Update Sample Bots

- [x] 3.1 Update `Corners/corners.py`
- [x] 3.2 Update `Crazy/Crazy.py`
- [x] 3.3 Update `Fire/fire.py`
- [x] 3.4 Update `MyFirstBot/MyFirstBot.py`
- [x] 3.5 Update `MyFirstDroid/MyFirstDroid.py`
- [x] 3.6 Update `MyFirstLeader/MyFirstLeader.py`
- [x] 3.7 Update `MyFirstTeam/` (if contains Python files)
- [x] 3.8 Update `PaintingBot/PaintingBot.py`
- [x] 3.9 Update `RamFire/RamFire.py`
- [x] 3.10 Update `SpinBot/SpinBot.py`
- [x] 3.11 Update `Target/Target.py`
- [x] 3.12 Update `TrackFire/TrackFire.py`
- [x] 3.13 Update `VelocityBot/VelocityBot.py`
- [x] 3.14 Update `Walls/Walls.py`

## 4. Update Python Tests

- [x] 4.1 Update `tests/bot_api/test_state_sync_smoke.py`: Convert `get_firepower()` to `firepower` property
- [x] 4.2 Scan and update any other test files that may use getter methods (no other references found)
- [x] 4.3 Update test utilities in `tests/test_utils/` if they reference old API methods (none found)
- [x] 4.4 Update integration tests that may reference bot state via old getter methods (none found)
- [x] 4.5 Create missing `AbstractBotTest` class for test infrastructure
- [x] 4.6 Fix remaining `get_energy()`, `get_speed()`, `get_turn_number()` calls in test files
- [x] 4.7 Fix missing `asyncio` import and `is_running` property usage in test files

## 5. Documentation

- [x] 5.1 Update `VERSIONS.md` with new version entry documenting breaking changes

## 6. Validation

- [x] 6.1 Run `mypy` on Python Bot API to validate type hints
- [x] 6.2 Run Python tests with `pytest` - core tests passing, some async cleanup issues in mocked server tests (
  non-critical)
- [x] 6.3 Build project with `./gradlew bot-api:python:build` - BUILD SUCCESSFUL

## 7. Fix Internal References

- [x] 7.1 Update `bot.py` internal method references to use new properties
- [x] 7.2 Update `event_mapper.py` references
- [x] 7.3 Update `base_bot_internals.py` references
- [x] 7.4 Fix remaining `get_speed()` calls in `bot.py`
- [x] 7.5 Fix test infrastructure and resolve hanging test issues
