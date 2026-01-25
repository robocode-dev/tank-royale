## 1. Internal Infrastructure

- [x] 1.1 Create `threading.Condition` for next-turn synchronization in `BaseBotInternals`
- [x] 1.2 Create WebSocket background thread with async event loop
- [x] 1.3 Implement `start_thread()` / `stop_thread()` matching Java's `BotInternals`
- [x] 1.4 Implement `execute()` with blocking wait (matches Java's `waitForNextTurn()`)
- [x] 1.5 Implement thread-safe event queue for cross-thread event dispatch
- [x] 1.6 Update `closed_event` to use `threading.Event` for `start()` blocking

## 2. Public API - BaseBot

- [x] 2.1 Convert `async def start()` → `def start()` (blocks until disconnect)
- [x] 2.2 Convert `async def go()` → `def go()` (dispatches events, sends intent, waits)
- [x] 2.3 Update all property getters to be synchronous (already sync, verify)
- [x] 2.4 Update docstrings to match Java's `IBaseBot` Javadoc

## 3. Public API - Bot

- [x] 3.1 Convert `async def run()` → `def run()`
- [x] 3.2 Convert `async def forward()` → `def forward()` (blocking)
- [x] 3.3 Convert `async def back()` → `def back()` (blocking)
- [x] 3.4 Convert `async def turn_left()` → `def turn_left()` (blocking)
- [x] 3.5 Convert `async def turn_right()` → `def turn_right()` (blocking)
- [x] 3.6 Convert `async def turn_gun_left()` → `def turn_gun_left()` (blocking)
- [x] 3.7 Convert `async def turn_gun_right()` → `def turn_gun_right()` (blocking)
- [x] 3.8 Convert `async def turn_radar_left()` → `def turn_radar_left()` (blocking)
- [x] 3.9 Convert `async def turn_radar_right()` → `def turn_radar_right()` (blocking)
- [x] 3.10 Convert `async def fire()` → `def fire()` (blocking)
- [x] 3.11 Convert `async def stop()` → `def stop()` (blocking)
- [x] 3.12 Convert `async def resume()` → `def resume()` (blocking)
- [x] 3.13 Convert `async def rescan()` → `def rescan()` (blocking)
- [x] 3.14 Convert `async def wait_for()` → `def wait_for()` (blocking)
- [x] 3.15 Update docstrings to match Java's `IBot` Javadoc

## 4. Public API - Abstract Base Classes

- [x] 4.1 Update `BaseBotABC` - remove async from method signatures
- [x] 4.2 Update `BotABC` - remove async from method signatures
- [x] 4.3 Update docstrings in ABCs to match Java interfaces

## 5. Event Handlers

- [x] 5.1 Convert all `async def on_*` handlers to `def on_*` in `BotEventHandlers`
- [x] 5.2 Update event dispatch to call handlers synchronously
- [x] 5.3 Verify interruptible event handling works with sync handlers

## 6. Sample Bots (14 bots)

- [x] 6.1 Update `Corners/Corners.py` - remove async/await
- [x] 6.2 Update `Crazy/Crazy.py` - remove async/await
- [x] 6.3 Update `Fire/Fire.py` - remove async/await
- [x] 6.4 Update `MyFirstBot/MyFirstBot.py` - remove async/await
- [x] 6.5 Update `MyFirstDroid/MyFirstDroid.py` - remove async/await
- [x] 6.6 Update `MyFirstLeader/MyFirstLeader.py` - remove async/await
- [x] 6.7 Update `MyFirstTeam/` (all team bots) - remove async/await
- [x] 6.8 Update `PaintingBot/PaintingBot.py` - remove async/await
- [x] 6.9 Update `RamFire/RamFire.py` - remove async/await
- [x] 6.10 Update `SpinBot/SpinBot.py` - remove async/await
- [x] 6.11 Update `Target/Target.py` - remove async/await
- [x] 6.12 Update `TrackFire/TrackFire.py` - remove async/await
- [x] 6.13 Update `VelocityBot/VelocityBot.py` - remove async/await
- [x] 6.14 Update `Walls/Walls.py` - remove async/await

## 7. Tests

- [x] 7.1 Update `MockedServer` for synchronous testing
- [x] 7.2 Convert `test_bot.py` from `IsolatedAsyncioTestCase` → `TestCase`
- [x] 7.3 Convert `test_base_bot_constructor.py` → synchronous
- [x] 7.4 Convert `test_base_bot_precedence.py` → synchronous
- [x] 7.5 Convert `test_base_bot_type_parsing.py` → synchronous
- [x] 7.6 Convert `test_bot_factory.py` → synchronous (no Python test present)
- [x] 7.7 Convert `test_commands_fire.py` → synchronous, remove skip annotations (no Python test present)
- [x] 7.8 Convert `test_commands_movement.py` → synchronous, remove skip annotations
- [x] 7.9 Convert `test_constants.py` → synchronous (already sync)
- [x] 7.10 Convert `test_team_message_realistic.py` → synchronous (already sync)
- [x] 7.11 Convert `test_team_message_serialization.py` → synchronous (already sync)
- [x] 7.12 Update any internal tests under `tests/bot_api/internal/` (already sync)

## 8. Documentation

- [x] 8.1 Update all docstrings in `base_bot.py` to match `IBaseBot.java` Javadoc
- [x] 8.2 Update all docstrings in `bot.py` to match `IBot.java` Javadoc
- [x] 8.3 Update all docstrings in `base_bot_abc.py` to match Java interfaces
- [x] 8.4 Update all docstrings in `bot_abc.py` to match Java interfaces
- [x] 8.5 Update `VERSIONS.md` with breaking change entry and migration guide

## 9. Validation

- [ ] 9.1 Run all tests: `pytest tests/`
- [ ] 9.2 Run sample bots against local server
- [ ] 9.3 Verify docfx documentation generates correctly
- [ ] 9.4 Run `./gradlew :bot-api:python:build`
